package service

import common.ViewerContent
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.net.URLClassLoader

object CompilerService {
    // Cache compiler and paths to avoid initialization overhead
    private val compiler = K2JVMCompiler()
    private val tempDir by lazy { 
        java.nio.file.Files.createTempDirectory("compose_compile").toFile().apply {
            deleteOnExit()
        }
    }
    
    // Cache classpath calculation
    private val classpath by lazy { System.getProperty("java.class.path") }
    private val pluginPath by lazy { 
        System.getProperty("compose.compiler.path") ?: run {
            // Fallback: Try to load from resources (for packaged app)
            val resource = CompilerService::class.java.getResource("/compose-compiler.jar")
            if (resource != null) {
                val tempPlugin = File(tempDir, "compose-compiler.jar")
                tempPlugin.writeBytes(resource.readBytes())
                tempPlugin.absolutePath
            } else {
                println("WARNING: compose.compiler.path not set and resource not found")
                null
            }
        }
    }

    fun compileAndLoad(code: String): common.CompilationResult? {
        val uniqueId = System.nanoTime()
        val className = "ViewerContentImpl_$uniqueId"
        val srcFile = File(tempDir, "$className.kt")

        // Extract all external types from imports to mock them
        val rawImports = code.lines().filter { it.trim().startsWith("import ") }
        val externalTypes = rawImports.mapNotNull { line ->
            // Match last part of import: import a.b.Type or import a.b.Type as Alias
            val match = Regex("import\\s+.*?\\.([A-Za-z0-9_]+)(?:\\s+as\\s+([A-Za-z0-9_]+))?\\s*$").find(line)
            match?.let { it.groupValues[2].ifEmpty { it.groupValues[1] } }
        }.filter { type ->
            // Don't mock standard things we already provide
            !type.contains("Composable") && !type.contains("Modifier") && !type.contains("Alignment") && 
            !type.contains("Arrangement") && !type.contains("Color") && !type.contains("Font") &&
            !type.contains("Text") && !type.contains("Button") && !type.contains("Material")
        }.distinct()

        val mockBlock = externalTypes.joinToString("\n") { type ->
            """
            typealias $type = Any
            @Composable fun $type(vararg args: Any?, content: @Composable () -> Unit = {}) { content() }
            val $type: Any get() = object {
                override fun toString() = "$type"
                val APPLICATION = this
                val NETWORK = this
                val any = this
                operator fun get(key: String) = this
            }
            """.trimIndent()
        }

        // Clean up code: remove package declarations, imports
        var cleanCode = code.lines()
            .filter { !it.trim().startsWith("package ") }
            .filter { !it.trim().startsWith("import ") }
            .joinToString("\n")
            
        // Sanitize @Preview: Remove arguments like (device = ..., ...) as Desktop Preview doesn't support them
        // Using a more conservative regex to avoid over-matching
        cleanCode = cleanCode.replace(Regex("@Preview\\s*\\(([^()]*|\\([^()]*\\))*\\)", RegexOption.DOT_MATCHES_ALL), "@Preview")

        // Priority 1: @Composable function with NO parameters (like a Preview)
        // Priority 2: Any @Composable function
        // Priority 3: First plain function
        // Fallback: "Example"
        val noArgComposableMatch = Regex("@Composable\\s+(?:override\\s+)?(?:internal\\s+|private\\s+|public\\s+)?fun\\s+([A-Za-z0-9_]+)\\s*\\(\\s*\\)").find(cleanCode)
        val anyComposableMatch = Regex("@Composable\\s+(?:override\\s+)?(?:internal\\s+|private\\s+|public\\s+)?fun\\s+([A-Za-z0-9_]+)").find(cleanCode)
        val plainFunctionMatch = Regex("fun\\s+([A-Za-z0-9_]+)\\s*\\(").find(cleanCode)
        
        val functionName = (noArgComposableMatch ?: anyComposableMatch ?: plainFunctionMatch)?.groupValues?.get(1) ?: "Example"
        
        val isM3 = cleanCode.contains("CardDefaults") || cleanCode.contains("MaterialTheme.colorScheme")
        val materialImport = if (isM3) {
            val base = "import androidx.compose.material3.*"
            if (code.contains("BottomNavigation")) {
                "$base\nimport androidx.compose.material.BottomNavigation\nimport androidx.compose.material.BottomNavigationItem"
            } else {
                base
            }
        } else {
            "import androidx.compose.material.*"
        }
        
        val themeStart = if (isM3) 
            "androidx.compose.material3.MaterialTheme { androidx.compose.material3.Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {" 
        else 
            "androidx.compose.material.MaterialTheme { androidx.compose.material.Surface(color = androidx.compose.material.MaterialTheme.colors.background, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {"
            
        val themeEnd = "}}"

        val source = """
            import androidx.compose.runtime.*
            import androidx.compose.ui.*
            import androidx.compose.ui.unit.*
            import androidx.compose.ui.graphics.*
            import androidx.compose.ui.layout.*
            import androidx.compose.foundation.layout.*
            import androidx.compose.foundation.*
            import androidx.compose.foundation.lazy.*
            import androidx.compose.ui.geometry.*
            import androidx.compose.ui.text.font.*
            import androidx.compose.foundation.shape.*
            import androidx.compose.material.icons.*
            import androidx.compose.material.icons.filled.*
            import androidx.compose.material.icons.outlined.*
            import coil3.compose.AsyncImage
            import coil3.compose.rememberAsyncImagePainter
            import androidx.compose.animation.*
            import androidx.compose.animation.core.*
            import kotlin.math.*
            import kotlinx.coroutines.delay
            import kotlinx.coroutines.launch
            import androidx.compose.ui.platform.LocalDensity
            import androidx.compose.desktop.ui.tooling.preview.Preview
            $materialImport
            import androidx.compose.ui.graphics.drawscope.*
            import androidx.compose.ui.graphics.vector.*
            import androidx.compose.ui.draw.*
            import androidx.compose.ui.geometry.Offset
            import androidx.compose.ui.graphics.Path
            import androidx.compose.ui.graphics.PathMeasure
            import androidx.compose.ui.text.*
            import androidx.compose.ui.text.style.*
            import androidx.compose.ui.text.style.TextAlign
            import androidx.compose.ui.text.style.TextOverflow
            import androidx.compose.ui.text.style.TextDecoration
            import kotlin.random.Random
            import kotlin.collections.*
            import coil3.request.ImageRequest
            import coil3.request.crossfade
            import coil3.compose.LocalPlatformContext
            import common.ViewerContent

            // Material Design Imports (Dynamic)
            $materialImport

            // Compatibility for Android LocalContext
            val LocalContext get() = LocalPlatformContext

            // Math Compatibility (Double/Float/Dp)
            operator fun Double.times(f: Float): Float = (this * f).toFloat()
            operator fun Float.times(d: Double): Float = (this * d).toFloat()
            operator fun Double.div(f: Float): Float = (this / f).toFloat()
            operator fun Float.div(d: Double): Float = (this / d).toFloat()
            operator fun Dp.times(d: Double): Dp = this * d.toFloat()
            operator fun Double.times(dp: Dp): Dp = dp * this.toFloat()

            // Density & Canvas Helpers
            @Composable fun Dp.toPx(): Float = with(LocalDensity.current) { this@toPx.toPx() }
            fun Canvas.scale(scale: Float) = this.scale(scale, scale)

            // Float Math wrappers
            fun sin(f: Float): Float = kotlin.math.sin(f.toDouble()).toFloat()
            fun cos(f: Float): Float = kotlin.math.cos(f.toDouble()).toFloat()
            fun max(a: Float, b: Float): Float = kotlin.math.max(a, b)
            fun min(a: Float, b: Float): Float = kotlin.math.min(a, b)

            // Lerp (Linear Interpolation) functions
            fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
            fun lerp(start: Int, stop: Int, fraction: Float): Int = (start + (stop - start) * fraction).toInt()
            fun lerp(start: Color, stop: Color, fraction: Float): Color {
                val red = lerp(start.red, stop.red, fraction)
                val green = lerp(start.green, stop.green, fraction)
                val blue = lerp(start.blue, stop.blue, fraction)
                val alpha = lerp(start.alpha, stop.alpha, fraction)
                return Color(red, green, blue, alpha)
            }
            fun lerp(start: Offset, stop: Offset, fraction: Float): Offset {
                return Offset(
                    lerp(start.x, stop.x, fraction),
                    lerp(start.y, stop.y, fraction)
                )
            }

            // Android Compatibility Helpers
            fun PathMeasure.getPosTan(distance: Float, pos: FloatArray?, tan: FloatArray?): Boolean {
                if (distance < 0 || distance > length) return false
                if (pos != null && pos.size >= 2) {
                    val p = getPosition(distance)
                    pos[0] = p.x
                    pos[1] = p.y
                }
                if (tan != null && tan.size >= 2) {
                    val t = getTangent(distance)
                    tan[0] = t.x
                    tan[1] = t.y
                }
                return true
            }

            // Global Helpers for Width/Height
            val maxWidth: Dp get() = 1000.dp
            val maxHeight: Dp get() = 1000.dp
            
            // BoxScope Helpers (for code that mistakenly uses Box instead of BoxWithConstraints)
            val BoxScope.maxWidth: Dp get() = 1000.dp
            val BoxScope.maxHeight: Dp get() = 1000.dp

            // Android Compatibility Mocks
            fun stringResource(id: Int): String = "Mock String"
            fun stringResource(id: Int, vararg args: Any): String = "Mock String"
            object R {
                object string { val any = 0; operator fun get(name: String) = 0 }
                object drawable { val any = 0; operator fun get(name: String) = 0 }
                object color { val any = 0; operator fun get(name: String) = 0 }
                object dimen { val any = 0; operator fun get(name: String) = 0 }
                object bool { val any = 0; operator fun get(name: String) = 0; val application_type = 0; val network_type = 0 }
                object integer { val any = 0; operator fun get(name: String) = 0 }
                val interceptor_type = 0
                val application_type = 0
                val network_type = 0
                val do_http_activity = 0
                val do_graphql_activity = 0
                val launch_chucker_directly = 0
                val export_to_file = 0
                val export_to_file_har = 0
            }
            object Devices { val PIXEL_4 = "Pixel 4"; val NEXUS_10 = "Nexus 10"; val AUTOMOTIVE_1024p = "Auto" }
            object Configuration { val UI_MODE_TYPE_NORMAL = 0; val UI_MODE_NIGHT_YES = 1 }
            fun Modifier.testTag(tag: String) = this
            fun Modifier.clearAndSetSemantics(properties: Any.() -> Unit) = this
            var Any.contentDescription: String 
                get() = ""
                set(value) {}
            
            // External Type Mocks
            $mockBlock

            $cleanCode

            class $className : ViewerContent {
                @Composable
                override fun Content() {
                    androidx.compose.foundation.layout.Box(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize()
                    ) {
                        $themeStart
                            $functionName()
                        $themeEnd
                    }
                }
            }
        """.trimIndent()
        
        srcFile.writeText(source)

        if (pluginPath == null) return null

        val args = K2JVMCompilerArguments().apply {
            freeArgs = listOf(srcFile.absolutePath)
            destination = tempDir.absolutePath
            classpath = this@CompilerService.classpath
            noStdlib = true
            noReflect = true
            pluginClasspaths = arrayOf(pluginPath!!)
        }
        val errorBuilder = StringBuilder()
        val warningBuilder = StringBuilder()
        
        // Dynamic line offset calculation
        val codeStart = source.indexOf(cleanCode)
        val headerOffset = if (codeStart != -1) source.substring(0, codeStart).lines().size - 1 else 0

        val collector = object : MessageCollector {
            override fun clear() {}
            override fun hasErrors(): Boolean = errorBuilder.isNotEmpty()
            override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
                val line = if (location != null) location.line - headerOffset else -1
                val loc = if (line > 0) "[$line:${location!!.column}] " else ""
                
                if (severity == CompilerMessageSeverity.ERROR || severity == CompilerMessageSeverity.EXCEPTION) {
                    errorBuilder.append("$loc${severity.name}: $message\n")
                } else if (severity == CompilerMessageSeverity.WARNING) {
                    warningBuilder.append("$loc${severity.name}: $message\n")
                }
            }
        }

        val exitCode = compiler.exec(collector, Services.EMPTY, args)

        if (exitCode.code != 0 || errorBuilder.isNotEmpty()) {
            return common.CompilationResult.Error(errorBuilder.toString())
        }

        val classLoader = URLClassLoader(arrayOf(tempDir.toURI().toURL()), this::class.java.classLoader)
        return try {
            val cls = classLoader.loadClass(className)
            val content = cls.getConstructor().newInstance() as ViewerContent
            val warnings = if (warningBuilder.isNotEmpty()) warningBuilder.lines().filter { it.isNotBlank() } else emptyList()
            common.CompilationResult.Success(content, warnings)
        } catch (t: Throwable) {
            t.printStackTrace()
            common.CompilationResult.Error("Runtime Loading Error: ${t.message}")
        }
    }
}
