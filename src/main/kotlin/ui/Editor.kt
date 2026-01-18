package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import kotlinx.coroutines.delay

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.AnnotatedString

@Composable
fun Editor(code: String, onCodeChange: (String) -> Unit) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(code)) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf(0) }
    
    // Focus Requesters
    val searchFocusRequester = remember { FocusRequester() }
    val editorFocusRequester = remember { FocusRequester() }
    
    // Search State
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<IntRange>>(emptyList()) }
    var currentMatchIndex by remember { mutableStateOf(0) }
    var matchedBrackets by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    // Code Folding State
    val foldedLines = remember { mutableStateListOf<Int>() }
    
    // Calculate Fold Ranges (StartLine -> EndLine)
    val foldRanges = remember(code) {
        val ranges = mutableMapOf<Int, Int>()
        val stack = mutableListOf<Int>() // Stack of Start Lines
        var currentLine = 0
        
        // Simple parser ignoring strings/comments for now
        code.forEach { c ->
            if (c == '{') {
                stack.add(currentLine)
            } else if (c == '}') {
                if (stack.isNotEmpty()) {
                    val startLine = stack.removeLast()
                    if (startLine != currentLine) {
                         ranges[startLine] = currentLine
                    }
                }
            } else if (c == '\n') {
                currentLine++
            }
        }
        ranges
    }
    
    // Scroll State for Sync
    val scrollState = rememberScrollState()
    
    // Light Theme Colors (White BG, Black Text)
    val editorBg = Color.White
    val textColor = Color.Black
    val lineNumberBg = Color(0xFFF0F0F0)
    val lineNumberColor = Color.Gray
    val cursorColor = Color.Black
    val selectionColor = Color(0xFFCCE8FF) // Light Blue
    val bracketHighlightBg = Color(0xFFDDDDDD)
    val bracketHighlightColor = Color(0xFF990000)
    
    // Syntax Highlighting Colors
    val keywordColor = Color(0xFF0033B3) // IntelliJ Blue
    val classColor = Color(0xFF660E7A) // Purple for Classes/Constants
    val functionColor = Color(0xFF00627A) // Teal for functions
    val stringColor = Color(0xFF067D17)
    val numberColor = Color(0xFF1750EB)
    val commentColor = Color(0xFF8C8C8C)
    // val annotationColor = Color(0xFF9E880D) // Used inline

    // Autocomplete Keywords (rich list for suggestions)
    val keywords = listOf(
        "Text", "Button", "Image", "Box", "Row", "Column", "Modifier", 
        "padding", "fillMaxSize", "background", "Color", "Alignment",
        "onClick", "fontSize", "fontWeight", "contentAlignment", 
        "horizontalArrangement", "verticalAlignment", "shape", "elevation",
        "Arrangement", "Center", "Start", "End", "Top", "Bottom",
        "Card", "CardDefaults", "AsyncImage", "Icon", "Icons", "Spacer", "width", "height",
        "val", "var", "fun", "for", "if", "else", "true", "false", "return", "when", "class", "object"
    )
    
    // Strict Kotlin Keywords for syntax highlighting (Blue)
    val syntaxKeywords = listOf(
        "val", "var", "fun", "for", "if", "else", "true", "false", "null", 
        "object", "class", "interface", "return", "when", "while", "do", 
        "package", "import", "try", "catch", "is", "as", "in", "override", "private", "public", "protected"
    )

    // Sync code prop to textFieldValue
    LaunchedEffect(code) {
        if (code != textFieldValue.text) {
            val newSelection = if (code.length >= textFieldValue.selection.max) {
                textFieldValue.selection
            } else {
                TextRange(code.length)
            }
            textFieldValue = textFieldValue.copy(text = code, selection = newSelection)
        }
    }
    
    // Auto-focus Search
    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) {
            delay(50)
            searchFocusRequester.requestFocus()
        }
    }
    
    // Search Logic
    LaunchedEffect(searchQuery, code) {
        if (searchQuery.isNotEmpty()) {
            val matches = Regex(Regex.escape(searchQuery), RegexOption.IGNORE_CASE).findAll(code)
            searchResults = matches.map { it.range }.toList()
            if (searchResults.isNotEmpty()) {
                currentMatchIndex = 0
            } else {
                currentMatchIndex = -1
            }
        } else {
            searchResults = emptyList()
            currentMatchIndex = -1
        }
    }

    // Bracket Matching Logic & Fold Expansion
    LaunchedEffect(textFieldValue.selection, code) {
        val cursor = textFieldValue.selection.start
        if (cursor >= 0 && cursor <= code.length) {
            
            // 1. Fold Expansion Logic
            // If cursor is placed on a line that is folded, expand it.
            var scan = 0
            var cursorLine = 0
            val lines = code.lines()
            for ((i, line) in lines.withIndex()) {
                val len = line.length + 1
                if (cursor < scan + len) {
                    cursorLine = i
                    break
                }
                scan += len
            }
            
            if (foldedLines.contains(cursorLine)) {
                foldedLines.remove(cursorLine)
            }
            
            // 2. Bracket Matching Logic
            val charBefore = code.getOrNull(cursor - 1)
            val charAfter = code.getOrNull(cursor)
            
            val targetIndex = when {
                charBefore in listOf('{', '}', '(', ')', '[', ']') -> cursor - 1
                charAfter in listOf('{', '}', '(', ')', '[', ']') -> cursor
                else -> -1
            }
            
            if (targetIndex != -1) {
                val char = code[targetIndex]
                val (pair, step) = when (char) {
                    '{' -> '}' to 1
                    '}' -> '{' to -1
                    '(' -> ')' to 1
                    ')' -> '(' to -1
                    '[' -> ']' to 1
                    ']' -> '[' to -1
                     else -> ' ' to 0
                }
                
                var depth = 1
                var found = -1
                var i = targetIndex + step
                while (i in code.indices) {
                    val c = code[i]
                    if (c == char) depth++
                    else if (c == pair) {
                        depth--
                        if (depth == 0) {
                            found = i
                            break
                        }
                    }
                    i += step
                }
                matchedBrackets = if (found != -1) (targetIndex to found) else null
            } else {
                matchedBrackets = null
            }
        } else {
            matchedBrackets = null
        }
    }

    // Function to apply suggestion
    fun applySuggestion(suggestion: String) {
        val text = textFieldValue.text
        val cursor = textFieldValue.selection.start
        val lastWordRegex = Regex("\\w+\$")
        val prefixMatch = lastWordRegex.find(text.substring(0, cursor))
        
        if (prefixMatch != null) {
            val newText = text.replaceRange(prefixMatch.range.first, cursor, suggestion)
            val newCursor = prefixMatch.range.first + suggestion.length
            
            onCodeChange(newText)
            textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor))
            suggestions = emptyList()
            selectedIndex = 0
        }
    }
    
    // Function to Jump to Match
    fun jumpToMatch(index: Int) {
        if (index in searchResults.indices) {
            currentMatchIndex = index
            val range = searchResults[index]
            // Move cursor to match
            textFieldValue = textFieldValue.copy(selection = TextRange(range.first, range.last + 1))
        }
    }
    
    fun nextMatch() {
        if (searchResults.isNotEmpty()) {
            val nextIndex = (currentMatchIndex + 1) % searchResults.size
            jumpToMatch(nextIndex)
        }
    }

    fun prevMatch() {
        if (searchResults.isNotEmpty()) {
            val prevIndex = if (currentMatchIndex - 1 < 0) searchResults.size - 1 else currentMatchIndex - 1
            jumpToMatch(prevIndex)
        }
    }
    
    // Syntax Highlighting + Search Highlighting
    val visualTransformation = remember(searchResults, currentMatchIndex, matchedBrackets) {
        VisualTransformation { text ->
            val rawText = text.text
            val annotatedString = buildAnnotatedString {
                append(rawText)
                
                val textToHighlight = rawText
                
                // 1. Numbers (Blue)
                Regex("\\b\\d+\\.?\\d*\\w*\\b").findAll(textToHighlight).forEach { match ->
                    addStyle(SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
                }
                
                // 2. Strict Keywords
                Regex("\\b(" + syntaxKeywords.joinToString("|") + ")\\b").findAll(textToHighlight).forEach { match ->
                    addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                }
                
                 // 3. Classes
                Regex("\\b[A-Z][a-zA-Z0-9_]*\\b").findAll(textToHighlight).forEach { match ->
                    if (!syntaxKeywords.contains(match.value)) {
                         addStyle(SpanStyle(color = classColor), match.range.first, match.range.last + 1)
                    }
                }
                
                // 4. Function Calls
                Regex("\\b([a-z][A-Za-z0-9_]*)\\s*\\(").findAll(textToHighlight).forEach { match ->
                    val range = match.groups[1]?.range
                    if (range != null && match.groups[1]?.value !in syntaxKeywords) {
                         addStyle(SpanStyle(color = functionColor), range.first, range.last + 1)
                    }
                }
                
                // 5. Annotations
                Regex("@[A-Za-z0-9_]+").findAll(textToHighlight).forEach { match ->
                     addStyle(SpanStyle(color = Color(0xFF9E880D)), match.range.first, match.range.last + 1)
                }
                
                // 6. Strings
                Regex("\"[^\"]*\"").findAll(textToHighlight).forEach { match ->
                    addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
                }
                
                // 7. Comments
                Regex("//.*").findAll(textToHighlight).forEach { match ->
                    addStyle(SpanStyle(color = commentColor, fontStyle = FontStyle.Italic), match.range.first, match.range.last + 1)
                }
                
                // 8. Fold Placeholder Highlight
                Regex(" \\{ ... \\} ").findAll(textToHighlight).forEach { match ->
                    addStyle(SpanStyle(color = Color.Gray, background = Color(0xFFEEEEEE)), match.range.first, match.range.last + 1)
                }

                // 9. Bracket Matching (Both Brackets)
                matchedBrackets?.let { (first, second) ->
                    // Direct highlighting without offset mapping
                    if (first < textToHighlight.length) {
                        addStyle(SpanStyle(background = bracketHighlightBg, color = bracketHighlightColor, fontWeight = FontWeight.Bold), first, first + 1)
                    }
                    if (second < textToHighlight.length) {
                        addStyle(SpanStyle(background = bracketHighlightBg, color = bracketHighlightColor, fontWeight = FontWeight.Bold), second, second + 1)
                    }
                }

                // 10. Search Highlighting
                if (isSearchVisible && searchResults.isNotEmpty()) {
                    searchResults.forEachIndexed { index, range ->
                        val color = if (index == currentMatchIndex) Color(0xFFFFB74D) else Color(0xFFFFF9C4)
                        val start = range.first
                        val end = range.last + 1
                        
                        // Direct highlighting without offset mapping
                        if (start < textToHighlight.length && end <= textToHighlight.length) {
                            addStyle(SpanStyle(background = color), start, end)
                        }
                    }
                }
            }
            
            TransformedText(annotatedString, OffsetMapping.Identity)
        }
    }
    
    // Main UI
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Static Background Layer (Stays fixed / Fills screen)
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .background(lineNumberBg)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(editorBg)
            )
        }
        
        // 2. Scrollable Content Layer
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val viewportHeight = maxHeight
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
            // Line Numbers Sidebar
            Column(
                modifier = Modifier
                    .width(60.dp) // Slightly wider for icon
                    .padding(vertical = 0.dp), // Check alignment
                horizontalAlignment = Alignment.End
            ) {
                val lines = textFieldValue.text.lines()
                lines.forEachIndexed { index, _ ->
                    val lineNum = index
                    val isFoldStart = foldRanges.containsKey(lineNum)
                    val isFolded = foldedLines.contains(lineNum)

                    Row(
                        modifier = Modifier
                            .height(20.dp) // Match Editor LineHeight
                            .fillMaxWidth()
                            .padding(end = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFoldStart) {
                            Icon(
                                if (isFolded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Fold",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        if (isFolded) foldedLines.remove(lineNum) else foldedLines.add(lineNum)
                                    },
                                tint = Color.Gray
                            )
                        } else {
                            Spacer(Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = (index + 1).toString(),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                color = lineNumberColor,
                                lineHeight = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        )
                    }
                }
            }
            
            // Editor Area
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val minEditorWidth = maxWidth
                val horizontalScrollState = androidx.compose.foundation.rememberScrollState()
                
                Box(modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onCodeChange(newValue.text)
                            
                            // Suggestions logic
                            val text = newValue.text
                            val cursor = newValue.selection.start
                            if (cursor > 0) {
                                 try {
                                     val lastWordRegex = Regex("\\w+$")
                                     val prefixMatch = lastWordRegex.find(text.substring(0, cursor))
                                     if (prefixMatch != null && prefixMatch.value.isNotEmpty()) {
                                          val prefix = prefixMatch.value
                                          suggestions = keywords.filter { it.startsWith(prefix, ignoreCase = false) && it != prefix }
                                          selectedIndex = 0
                                     } else {
                                         suggestions = emptyList()
                                     }
                                 } catch (e: Exception) { suggestions = emptyList() }
                            } else { suggestions = emptyList() }
                        },
                        modifier = Modifier
                            .heightIn(min = viewportHeight)
                            .widthIn(min = minEditorWidth)
                            .focusRequester(editorFocusRequester)
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown) {
                                    // Search Shortcuts
                                    if (event.isMetaPressed && event.key == Key.F) {
                                        isSearchVisible = !isSearchVisible
                                        return@onPreviewKeyEvent true
                                    }
                                    if (isSearchVisible && event.key == Key.Escape) {
                                      isSearchVisible = false
                                      return@onPreviewKeyEvent true
                                    }
                                    
                                    // Search Navigation
                                    if (isSearchVisible && event.key == Key.Enter) {
                                        if (event.isShiftPressed) prevMatch() else nextMatch()
                                        return@onPreviewKeyEvent true
                                    }
                                    
                                    // Autocomplete Navigation
                                    if (suggestions.isNotEmpty()) {
                                        when (event.key) {
                                            Key.DirectionDown -> {
                                                selectedIndex = (selectedIndex + 1) % suggestions.size
                                                true
                                            }
                                            Key.DirectionUp -> {
                                                selectedIndex = if (selectedIndex - 1 < 0) suggestions.size - 1 else selectedIndex - 1
                                                true
                                            }
                                            Key.Tab, Key.Enter -> {
                                                applySuggestion(suggestions[selectedIndex])
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                } else false
                            },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = textColor,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(cursorColor),
                        visualTransformation = visualTransformation,
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                innerTextField()
                            }
                        }
                    )
                }
                
                // Autocomplete Popup
                if (suggestions.isNotEmpty()) {
                     Surface(
                         modifier = Modifier
                             .align(Alignment.TopStart)
                             .padding(top = (textFieldValue.selection.start * 0.1).dp + 40.dp, start = 10.dp) 
                             .padding(top = 24.dp)
                             .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
                         elevation = 4.dp,
                         shape = RoundedCornerShape(4.dp),
                         color = Color.White
                     ) {
                         Column {
                             suggestions.forEachIndexed { index, suggestion ->
                                 val isSelected = index == selectedIndex
                                 Text(
                                     text = suggestion,
                                     modifier = Modifier
                                         .clickable { applySuggestion(suggestion) }
                                         .background(if (isSelected) selectionColor else Color.Transparent)
                                         .padding(horizontal = 8.dp, vertical = 4.dp)
                                         .fillMaxWidth(),
                                     color = keywordColor,
                                     fontWeight = FontWeight.Medium,
                                     style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                 )
                             }
                         }
                     }
                }

        }
    }

    // VerticalScrollbar (Moved out of Row)
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        style = androidx.compose.foundation.defaultScrollbarStyle().copy(
            thickness = 8.dp,
            hoverDurationMillis = 300
        )
    )
    
    // Search Bar Overlay (needs Box scope for alignment)
    Box(modifier = Modifier.fillMaxSize()) {
        // Search Bar Overlay
        if (isSearchVisible) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .width(300.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
                elevation = 8.dp,
                color = Color(0xFFF2F2F2),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LaunchedEffect(Unit) {
                        searchFocusRequester.requestFocus()
                    }
                    // Search Input
                    Box(modifier = Modifier.weight(1f).height(32.dp).background(Color.White, RoundedCornerShape(2.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(2.dp))) {
                         TextField(
                             value = searchQuery,
                             onValueChange = { searchQuery = it },
                             modifier = Modifier
                                 .fillMaxSize()
                                 .focusRequester(searchFocusRequester),
                             textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                             singleLine = true,
                             colors = TextFieldDefaults.textFieldColors(
                                 textColor = Color.Black,
                                 backgroundColor = Color.Transparent,
                                 focusedIndicatorColor = Color.Transparent,
                                 unfocusedIndicatorColor = Color.Transparent,
                                 cursorColor = Color.Black
                             ),
                             placeholder = { Text("Find...", fontSize = 12.sp, color = Color.Gray) }
                         )
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    // Match Count
                    Text(
                        text = if (searchResults.isNotEmpty()) "${currentMatchIndex + 1}/${searchResults.size}" else "0/0",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(Modifier.width(4.dp))
                    
                    // Navigation Buttons
                    IconButton(onClick = { prevMatch() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Prev", tint = Color.Gray)
                    }
                    IconButton(onClick = { nextMatch() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next", tint = Color.Gray)
                    }
                    IconButton(onClick = { isSearchVisible = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            }
        }
    }
}
}
}
