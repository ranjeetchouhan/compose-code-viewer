package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import common.ViewerContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.clickable
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.layout.size

@Composable
fun Previewer(content: ViewerContent?, errorMessage: String? = null, warnings: List<String> = emptyList()) {
    var isWarningsExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (errorMessage != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Text(
                    "Compilation Messages", 
                    color = Color.DarkGray, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 14.sp
                )
                Divider(color = Color.LightGray)
                
                // Console Output
                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        errorMessage.lines().forEach { line ->
                             val annotatedString = buildAnnotatedString {
                                 // Regex to match format: [3:30] ERROR: Message
                                 val regex = Regex("^\\[(\\d+:\\d+)\\]\\s+(ERROR|WARNING):?(.*)")
                                 val match = regex.find(line)
                                 
                                 if (match != null) {
                                     val (loc, level, msg) = match.destructured
                                     
                                     // Location [3:30]
                                     pushStyle(SpanStyle(color = Color.Gray))
                                     append("[$loc] ")
                                     pop()
                                     
                                     // Level
                                     val levelColor = if (level.contains("ERROR")) Color(0xFFCC0000) else Color(0xFF856404)
                                     pushStyle(SpanStyle(color = levelColor, fontWeight = FontWeight.Bold))
                                     append(level)
                                     pop()
                                     
                                     // Rest of line
                                     append(":$msg")
                                 } else {
                                     // Fallback highlighting
                                     val lower = line.lowercase()
                                     val color = when {
                                         lower.contains("error") -> Color(0xFFCC0000)
                                         lower.contains("unresolved reference") -> Color(0xFFCC0000)
                                         lower.contains("warning") -> Color(0xFF856404)
                                         else -> Color.Black
                                     }
                                     pushStyle(SpanStyle(color = color))
                                     append(line)
                                 }
                             }
                             
                             Text(
                                 text = annotatedString,
                                 fontSize = 13.sp,
                                 fontFamily = FontFamily.Monospace,
                                 modifier = Modifier.padding(bottom = 4.dp),
                                 lineHeight = 18.sp
                             )
                        }
                        
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        } else {
             androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                 // Warnings Section (Collapsible)
                 if (warnings.isNotEmpty()) {
                     Column(
                         modifier = Modifier
                             .fillMaxWidth()
                             .background(Color(0xFFFFF3CD)) // Light Yellow
                     ) {
                         Row(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .clickable { isWarningsExpanded = !isWarningsExpanded }
                                 .padding(8.dp),
                             verticalAlignment = Alignment.CenterVertically
                         ) {
                             androidx.compose.material.Icon(
                                 if (isWarningsExpanded) androidx.compose.material.icons.Icons.Default.KeyboardArrowUp else androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                 contentDescription = if (isWarningsExpanded) "Collapse" else "Expand",
                                 tint = Color(0xFF856404),
                                 modifier = Modifier.size(20.dp)
                             )
                             Spacer(Modifier.width(8.dp))
                             Text(
                                 "Warnings (${warnings.size})", 
                                 fontWeight = FontWeight.Bold, 
                                 color = Color(0xFF856404), 
                                 fontSize = 12.sp
                             )
                         }
                         
                         if (isWarningsExpanded) {
                             Divider(color = Color(0xFF856404).copy(alpha = 0.2f))
                             SelectionContainer {
                                 Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 4.dp)) {
                                     warnings.forEach { warning ->
                                         Text(warning, color = Color(0xFF856404), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                         Spacer(Modifier.height(4.dp))
                                     }
                                 }
                             }
                         }
                     }
                 }
                 
                 // Content Section
                 Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                     if (content != null) {
                        content.Content()
                         

                     } else {
                         Text("Preview Area - Type valid code to render", color = Color.Gray)
                     }
                 }
             }
        }
    }
}
