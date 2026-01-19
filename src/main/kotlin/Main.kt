import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Code
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.PointerIcon
import service.CompilerService
import service.UpdateChecker
import samples.SampleLibrary
import common.ViewerContent
import ui.Editor
import ui.Previewer

@Composable
@Preview
fun App() {
    var code by remember { mutableStateOf("@Composable\nfun Example() {\n    Button(onClick = {}) { Text(\"Hello World\") }\n}") }
    var content by remember { mutableStateOf<ViewerContent?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var warnings by remember { mutableStateOf<List<String>>(emptyList()) }
    var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
    var showSamplesMenu by remember { mutableStateOf(false) }
    
    // Unified Status State
    var statusMessage by remember { mutableStateOf("Ready") }
    var isWorking by remember { mutableStateOf(false) }
    
    // Check for updates on startup
    LaunchedEffect(Unit) {
        updateInfo = UpdateChecker.checkForUpdates()
    }
    var previewKey by remember { mutableStateOf(0) }

    // Sync Global Errors to UI
    LaunchedEffect(GlobalErrorState.hasError) {
        if (GlobalErrorState.hasError) {
            errorMessage = "Runtime Error: ${GlobalErrorState.message}"
            content = null
            GlobalErrorState.clear()
        }
    }

    fun compile() {
        if (isWorking) return
        isWorking = true
        statusMessage = "Compiling..."
        
        Thread {
            try {
                val result = CompilerService.compileAndLoad(code)
                javax.swing.SwingUtilities.invokeLater {
                    isWorking = false
                    if (result is common.CompilationResult.Success) {
                        content = result.content
                        errorMessage = null
                        warnings = result.warnings
                        statusMessage = if (result.warnings.isNotEmpty()) "Compiled with Warnings" else "Compilation Success"
                    } else if (result is common.CompilationResult.Error) {
                        errorMessage = result.message
                        content = null
                        warnings = emptyList()
                        statusMessage = "Compilation Failed"
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                javax.swing.SwingUtilities.invokeLater {
                    isWorking = false
                    errorMessage = "System Error: ${t.message}"
                     statusMessage = "System Error"
                }
            }
        }.start()
    }

    fun formatCode() {
        if (isWorking) return
        isWorking = true
        statusMessage = "Formatting..."
        
        Thread {
            try {
                 val formatted = com.facebook.ktfmt.format.Formatter.format(code)
                 javax.swing.SwingUtilities.invokeLater {
                     code = formatted
                     statusMessage = "Formatted (ktfmt)"
                     isWorking = false
                 }
            } catch (e: Exception) {
                // Fallback
                try {
                     val sb = StringBuilder()
                     var indent = 0
                     code.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
                         if (line.startsWith("}") || line.startsWith(")")) indent = maxOf(0, indent - 1)
                         sb.append("    ".repeat(indent)).append(line).append("\n")
                         if (line.endsWith("{") || line.endsWith("(")) indent++
                     }
                     val fallbackCode = sb.toString()
                     javax.swing.SwingUtilities.invokeLater {
                         code = fallbackCode
                         statusMessage = "Formatted (Fallback)"
                         isWorking = false
                     }
                } catch (fallbackEx: Exception) {
                     javax.swing.SwingUtilities.invokeLater {
                         statusMessage = "Format Failed"
                         isWorking = false
                     }
                }
            }
        }.start()
    }

    MaterialTheme {

        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
            // Professional Dark Top Bar
            androidx.compose.material.TopAppBar(
                backgroundColor = Color(0xFF2B2D30), // IntelliJ Dark
                contentColor = Color.White,
                elevation = 0.dp,
                title = { 
                    Text(
                        "Compose Viewer", 
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFFAAAAAA)
                    ) 
                },
                actions = {
                    // Samples Menu
                    Box {
                        androidx.compose.material.Button(
                            onClick = { showSamplesMenu = !showSamplesMenu },
                            colors = androidx.compose.material.ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF3C3F41),
                                contentColor = Color(0xFFBBBBBB)
                            ),
                            modifier = Modifier.padding(end = 8.dp),
                            elevation = androidx.compose.material.ButtonDefaults.elevation(0.dp, 0.dp, 0.dp)
                        ) {
                            androidx.compose.foundation.layout.Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                            ) {
                                Text("📚", fontSize = 14.sp)
                                Text("Samples", fontSize = 12.sp)
                            }
                        }
                        
                        androidx.compose.material.DropdownMenu(
                            expanded = showSamplesMenu,
                            onDismissRequest = { showSamplesMenu = false },
                            modifier = Modifier
                                .width(320.dp)
                                .background(Color(0xFF2B2D30))
                        ) {
                            SampleLibrary.categories.forEach { category ->
                                // Category Header
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF313335))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = category.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF6897BB),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                
                                // Sample Items
                                SampleLibrary.getSamplesByCategory(category).forEach { sample ->
                                    androidx.compose.material.DropdownMenuItem(
                                        onClick = {
                                            code = sample.code
                                            showSamplesMenu = false
                                        },
                                        modifier = Modifier.background(Color(0xFF2B2D30))
                                    ) {
                                        androidx.compose.foundation.layout.Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                        ) {
                                            // Icon indicator
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(
                                                        Color(0xFF6897BB),
                                                        shape = androidx.compose.foundation.shape.CircleShape
                                                    )
                                            )
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            androidx.compose.foundation.layout.Column {
                                                Text(
                                                    text = sample.title,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFFCCCCCC),
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = sample.description,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF808080),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                if (category != SampleLibrary.categories.last()) {
                                    androidx.compose.material.Divider(
                                        color = Color(0xFF3C3F41),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                    
                    // Run Button (Green Play Icon)
                    androidx.compose.material.IconButton(
                        onClick = { compile() },
                        enabled = !isWorking
                    ) {
                        androidx.compose.material.Icon(
                             Icons.Default.PlayArrow,
                             contentDescription = "Run",
                             tint = if (isWorking) Color.Gray else Color(0xFF4CAF50),
                             modifier = Modifier.size(28.dp)
                        )
                    }

                    // Format Button
                    androidx.compose.material.IconButton(
                        onClick = { formatCode() },
                        enabled = !isWorking
                    ) {
                        androidx.compose.material.Icon(
                            Icons.Default.Code,
                            contentDescription = "Format",
                            tint = if (isWorking) Color.Gray else Color(0xFFBBBBBB)
                        )
                    }
                    
                    // Reload Button
                    androidx.compose.material.IconButton(onClick = { previewKey++ }) {
                         androidx.compose.material.Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.LightGray)
                    }
                }
            )
            
            
            // Main Content with Resizable Split
            var splitRatio by remember { mutableStateOf(0.5f) }
            
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxSize()) {
                val containerWidth = maxWidth
                
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth(splitRatio).fillMaxHeight()) {
                        Editor(code) { code = it }
                    }
                    
                    // Draggable Divider
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(4.dp)
                            .background(Color(0xFF3C3F41))
                            .hoverable(
                                interactionSource = remember { MutableInteractionSource() }
                            )
                            .pointerInput(containerWidth) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val containerWidthPx = containerWidth.toPx()
                                    val newRatio = (splitRatio + dragAmount.x / containerWidthPx).coerceIn(0.1f, 0.9f)
                                    splitRatio = newRatio
                                }
                            }
                            .pointerHoverIcon(
                                PointerIcon(java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR))
                            )
                    )
                    
                    Box(Modifier.fillMaxSize()) {
                        androidx.compose.runtime.key(previewKey) {
                            Previewer(content, errorMessage, warnings)
                        }
                    }
                }
            }
            
            // Bottom Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color(0xFF2B2D30)) // Match sidebar/dark theme
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                // Version and status
                Text(
                    text = "v${UpdateChecker.CURRENT_VERSION}",
                    fontSize = 11.sp,
                    color = Color(0xFF888888),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                Text(
                    text = statusMessage, 
                    fontSize = 12.sp, 
                    color = Color(0xFFAAAAAA),
                    fontFamily = FontFamily.Monospace
                )
                
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                
                // Update notification
                updateInfo?.let { info ->
                    if (info.isUpdateAvailable) {
                        androidx.compose.material.TextButton(
                            onClick = {
                                try {
                                    java.awt.Desktop.getDesktop().browse(java.net.URI(info.downloadUrl))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            colors = androidx.compose.material.ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF4CAF50)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Update Available: ${info.latestVersion}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
                
                // Search Hint
                val os = remember { System.getProperty("os.name").lowercase() }
                val searchHint = if (os.contains("mac")) "Cmd + F" else "Ctrl + F"
                Text(
                    text = "Search: $searchHint", 
                    fontSize = 11.sp, 
                    color = Color(0xFF666666),
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                if (isWorking) {
                    androidx.compose.material.LinearProgressIndicator(
                        modifier = Modifier.width(150.dp).height(4.dp),
                        color = Color(0xFF3574F0),
                        backgroundColor = Color(0xFF4E5254)
                    )
                }
            }
        }
    }
}

object GlobalErrorState {
    var hasError by mutableStateOf(false)
    var message by mutableStateOf("")

    fun report(t: Throwable) {
        message = t.message ?: t.toString()
        hasError = true
        try {
            java.io.File("error.log").appendText("${java.time.LocalDateTime.now()}: ${t.stackTraceToString()}\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun clear() {
        hasError = false
        message = ""
    }
}

fun main() {
    System.setProperty("apple.awt.application.name", "Compose Viewer")
    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        // Catch exceptions on the EDT (Event Dispatch Thread) or others to prevent crash
        javax.swing.SwingUtilities.invokeLater {
            GlobalErrorState.report(throwable)
        }
    }
    
    application {
        Window(onCloseRequest = ::exitApplication, title = "Compose Instant Viewer") {
            App()
        }
    }
}

