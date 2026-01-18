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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
    
    // Unified Status State
    var isWorking by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Ready") }

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
                    // Format Button
                    androidx.compose.material.Button(
                        onClick = { formatCode() },
                        enabled = !isWorking,
                        colors = androidx.compose.material.ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4E5254), // Darker gray button
                            contentColor = Color.White,
                            disabledBackgroundColor = Color(0xFF3C3F41)
                        ),
                        modifier = Modifier.padding(end = 8.dp),
                        elevation = androidx.compose.material.ButtonDefaults.elevation(0.dp)
                    ) {
                        Text("Format", fontSize = 12.sp)
                    }
                    
                    // Run Button
                    androidx.compose.material.Button(
                        onClick = { compile() },
                        enabled = !isWorking,
                        colors = androidx.compose.material.ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF3574F0), // IntelliJ Blue/Action
                            contentColor = Color.White,
                            disabledBackgroundColor = Color(0xFF4E5254)
                        ),
                        elevation = androidx.compose.material.ButtonDefaults.elevation(0.dp)
                    ) {
                        Text(if (isWorking && statusMessage == "Compiling...") "Running..." else "Run", fontSize = 12.sp)
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
                        Previewer(content, errorMessage, warnings)
                    }
                }
            }
            
            // Bottom Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color(0xFF2B2D30)) // Match sidebar/dark theme
                    .padding(horizontal = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = statusMessage, 
                    fontSize = 12.sp, 
                    color = Color(0xFFAAAAAA),
                    fontFamily = FontFamily.Monospace
                )
                
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                
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

