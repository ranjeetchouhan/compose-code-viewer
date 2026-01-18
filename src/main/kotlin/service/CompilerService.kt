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
    fun compileAndLoad(code: String): common.CompilationResult? {
        val uniqueId = System.nanoTime()
        val className = "ViewerContentImpl_$uniqueId"
        val tempDir = java.nio.file.Files.createTempDirectory("compose_compile").toFile()
        val srcFile = File(tempDir, "$className.kt")

        val functionalityMatch = Regex("fun\\s+([A-Za-z0-9_]+)\\s*\\(").find(code)
        val functionName = functionalityMatch?.groupValues?.get(1) ?: "Example"
        
        val isM3 = code.contains("CardDefaults") || code.contains("MaterialTheme.colorScheme")
        val materialImport = if (isM3) {
            "import androidx.compose.material3.*"
        } else {
            "import androidx.compose.material.*"
        }
        
        val themeStart = if (isM3) 
            "androidx.compose.material3.MaterialTheme { androidx.compose.material3.Surface(color = androidx.compose.material3.MaterialTheme.colorScheme.background, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {" 
        else 
            "androidx.compose.material.MaterialTheme { androidx.compose.material.Surface(color = androidx.compose.material.MaterialTheme.colors.background, modifier = androidx.compose.ui.Modifier.fillMaxSize()) {"
            
        val themeEnd = "}}"

        val source = """
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.*
            import androidx.compose.ui.unit.*
            import androidx.compose.ui.graphics.*
            import androidx.compose.ui.layout.*
            import androidx.compose.foundation.layout.*
            import androidx.compose.foundation.*
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
            import androidx.compose.runtime.getValue
            import androidx.compose.runtime.setValue
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.mutableStateOf
            import androidx.compose.runtime.LaunchedEffect
            import androidx.compose.runtime.rememberCoroutineScope
            import kotlinx.coroutines.delay
            import kotlinx.coroutines.launch
            import androidx.compose.ui.platform.LocalDensity
            import androidx.compose.ui.graphics.drawscope.*
            import androidx.compose.ui.graphics.vector.*
            import androidx.compose.ui.draw.*
            import androidx.compose.ui.geometry.Offset
            import androidx.compose.ui.graphics.Path
            import androidx.compose.ui.graphics.PathMeasure
            import common.ViewerContent

            // Material Design Imports (Dynamic)
            $materialImport

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

            $code

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

        val pluginPath = System.getProperty("compose.compiler.path") ?: run {
            println("WARNING: compose.compiler.path not set")
            return null
        }

        val args = K2JVMCompilerArguments().apply {
            freeArgs = listOf(srcFile.absolutePath)
            destination = tempDir.absolutePath
            classpath = System.getProperty("java.class.path")
            noStdlib = true
            noReflect = true
            pluginClasspaths = arrayOf(pluginPath)
        }

        val compiler = K2JVMCompiler()
        val errorBuilder = StringBuilder()
        val warningBuilder = StringBuilder()
        
        // Dynamic line offset calculation
        val codeStart = source.indexOf(code)
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
