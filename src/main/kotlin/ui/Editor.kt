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
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.focusProperties
import kotlinx.coroutines.launch

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor

import androidx.compose.ui.platform.LocalDensity

@Composable
fun Editor(code: String, onCodeChange: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
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
    val horizontalScrollState = rememberScrollState()
    
    // Text Layout Result for Click Handling
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var dragStartOffset by remember { mutableStateOf(0) }
    
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
        // Layouts & Containers
        "Box", "Row", "Column", "Spacer", "Surface", "Scaffold", "LazyColumn", "LazyRow", "Card", "CardDefaults", "Divider", "HorizontalDivider", "VerticalDivider",
        
        // UI Elements
        "Text", "Button", "Image", "AsyncImage", "Icon", "Icons", "CircularProgressIndicator", "LinearProgressIndicator", "Checkbox", "Switch", "RadioButton", "TextField", "OutlinedTextField",
        
        // Modifiers
        "Modifier", "padding", "fillMaxSize", "fillMaxWidth", "fillMaxHeight", "wrapContentSize", "background", "clickable", "pointerInput", "size", "width", "height", "aspectRatio", "clip", "shadow", "alpha", "offset", "border", "zIndex", "drawBehind", "graphicsLayer",
        
        // Arrangement & Alignment
        "Arrangement", "Alignment", "Center", "Start", "End", "Top", "Bottom", "CenterVertically", "CenterHorizontally", "SpaceAround", "SpaceBetween", "SpaceEvenly",
        
        // State & Action
        "remember", "mutableStateOf", "getValue", "setValue", "onClick", "onValueChange", "LaunchedEffect", "DisposableEffect", "SideEffect", "rememberCoroutineScope", "withFrameNanos",
        
        // Typography & Style
        "Color", "TextStyle", "MaterialTheme", "fontSize", "fontWeight", "fontStyle", "fontFamily", "AnnotatedString", "SpanStyle", "ParagraphStyle", "LocalTextStyle", "TextAlign", "TextOverflow", "TextDecoration", "shape", "elevation",
        
        // Graphics & Animation
        "painterResource", "ImageVector", "rememberAsyncImagePainter", "animateFloatAsState", "animateColorAsState", "rememberInfiniteTransition", "animateValueAsState",
        
        // Kotlin Keywords
        "val", "var", "fun", "for", "if", "else", "true", "false", "return", "when", "class", "object", "interface", "package", "import", "try", "catch", "is", "as", "in"
    )
    
    // Autocomplete Snippets (Keywords with prefilled arguments)
    // '$' is used as a cursor marker
    val snippets = mapOf(
        "Text" to "Text(text = \"$\")",
        "Button" to "Button(onClick = { $ }) {\n    Text(\"Click\")\n}",
        "Box" to "Box(modifier = Modifier.$) {\n    \n}",
        "Row" to "Row(modifier = Modifier.$, verticalAlignment = Alignment.CenterVertically) {\n    \n}",
        "Column" to "Column(modifier = Modifier.$, horizontalAlignment = Alignment.CenterHorizontally) {\n    \n}",
        "LazyColumn" to "LazyColumn(modifier = Modifier.$) {\n    item {\n        \n    }\n}",
        "LazyRow" to "LazyRow(modifier = Modifier.$) {\n    item {\n        \n    }\n}",
        "Image" to "Image(painter = $, contentDescription = null)",
        "AsyncImage" to "AsyncImage(model = \"$\", contentDescription = null)",
        "Icon" to "Icon(Icons.Default.$, contentDescription = null)",
        "Spacer" to "Spacer(modifier = Modifier.width($))",
        "Card" to "Card(modifier = Modifier.$, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {\n    \n}",
        "Divider" to "Divider(modifier = Modifier.$, color = Color.Gray, thickness = 1.dp)",
        "HorizontalDivider" to "HorizontalDivider(modifier = Modifier.$, color = Color.Gray, thickness = 1.dp)",
        "VerticalDivider" to "VerticalDivider(modifier = Modifier.$, color = Color.Gray, thickness = 1.dp)",
        "CircularProgressIndicator" to "CircularProgressIndicator(modifier = Modifier.size($))",
        "LinearProgressIndicator" to "LinearProgressIndicator(modifier = Modifier.$)",
        "IconButton" to "IconButton(onClick = { $ }) {\n    Icon(Icons.Default.Favorite, contentDescription = null)\n}",
        "TextField" to "TextField(value = $, onValueChange = {  })",
        "OutlinedTextField" to "OutlinedTextField(value = $, onValueChange = {  })",
        "remember" to "remember { mutableStateOf($) }",
        "LaunchedEffect" to "LaunchedEffect(Unit) {\n    $\n}"
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
            val template = snippets[suggestion] ?: suggestion
            val hasMarker = template.contains("$")
            val cleanSnippet = template.replace("$", "")
            val markerIndex = if (hasMarker) template.indexOf("$") else cleanSnippet.length
            
            val newText = text.replaceRange(prefixMatch.range.first, cursor, cleanSnippet)
            val newCursor = prefixMatch.range.first + markerIndex
            
            onCodeChange(newText)
            textFieldValue = TextFieldValue(text = newText, selection = TextRange(newCursor))
            suggestions = emptyList()
            selectedIndex = 0
        }
    }
    
    // Function to Jump to Match
    fun saveCode(currentCode: String) {
        val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Save Code", java.awt.FileDialog.SAVE)
        dialog.file = "Sample.kt"
        dialog.isVisible = true
        val directory = dialog.directory
        val file = dialog.file
        if (directory != null && file != null) {
            try {
                val dest = java.io.File(directory, file)
                dest.writeText(currentCode)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun jumpToMatch(index: Int) {
        if (index in searchResults.indices) {
            currentMatchIndex = index
            val range = searchResults[index]
            // Move cursor to match
            textFieldValue = textFieldValue.copy(selection = TextRange(range.first, range.last + 1))
            
            // Scroll to Match Line
            val text = textFieldValue.text
            var lineIndex = 0
            var offset = 0
            for ((i, line) in text.lines().withIndex()) {
                 val lineLen = line.length + 1
                 if (range.first < offset + lineLen) {
                     lineIndex = i
                     break
                 }
                 offset += lineLen
            }
            
            val scrollPos = with(density) { lineIndex * 20.dp.toPx() }.toInt()
            scope.launch { 
                scrollState.animateScrollTo(scrollPos)
            }
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
    
    // Syntax Highlighting + Code Folding + Search
    val visualTransformation = remember(code, searchResults, currentMatchIndex, matchedBrackets, foldedLines.toList()) {
        VisualTransformation { text ->
            val rawText = text.text
            
            // 1. Calculate Fold Segments
            val collapsedRanges = foldedLines.mapNotNull { startLine ->
                foldRanges[startLine]?.let { endLine ->
                    // Convert line numbers to char offsets
                    val lines = rawText.lines()
                    if (startLine < lines.size && endLine < lines.size) {
                        val startLineEndOffset = rawText.lineEnd(startLine)
                        val endLineStartOffset = rawText.lineStart(endLine)
                        
                        if (startLineEndOffset < endLineStartOffset) {
                            TextRange(startLineEndOffset, endLineStartOffset)
                        } else null
                    } else null
                }
            }.sortedBy { it.start }

            // 2. Build Transformed Text & Offset Map
            val builder = AnnotatedString.Builder()
            val offsetMap = mutableListOf<Pair<Int, Int>>() // Original -> Transformed
            
            var originalOffset = 0
            var transformedOffset = 0
            
            fun appendText(content: String) {
                builder.append(content)
                val len = content.length
                for (i in 0 until len) {
                    offsetMap.add(originalOffset + i to transformedOffset + i)
                }
                originalOffset += len
                transformedOffset += len
            }

            for (range in collapsedRanges) {
                if (range.start > originalOffset) {
                    val chunk = rawText.substring(originalOffset, range.start)
                    appendText(chunk)
                }
                
                // Append Placeholder
                val placeholder = " ... "
                builder.pushStyle(SpanStyle(color = Color.Gray, background = Color(0xFFEEEEEE), fontSize = 12.sp))
                builder.append(placeholder)
                builder.pop()
                
                // Map the entire folded range to the start of the placeholder
                val foldLength = range.length
                for (i in 0 until foldLength) {
                    offsetMap.add(originalOffset + i to transformedOffset)
                }
                
                originalOffset += foldLength
                transformedOffset += placeholder.length
            }
            
            // Remaining text
            if (originalOffset < rawText.length) {
                val chunk = rawText.substring(originalOffset)
                appendText(chunk)
            }
            
            // Final Offset Mapping
            val mapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset >= offsetMap.size) return transformedOffset
                    return offsetMap[offset].second
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val found = offsetMap.find { it.second == offset }
                    return found?.first ?: offsetMap.lastOrNull()?.first ?: rawText.length
                }
            }

            val transformedText = builder.toAnnotatedString()
            val textToHighlight = transformedText.text

            // 3. Apply Syntax Highlighting (on Transformed Text)
            val highlightedBuilder = AnnotatedString.Builder(transformedText)
            
            // Find String & Comment Ranges first (Masks)
            val stringMatches = Regex("\"(\\\\.|[^\"\\\\])*\"").findAll(textToHighlight).map { it.range }.toList()
            val commentMatches = Regex("//.*").findAll(textToHighlight).map { it.range }.toList()
            
            // Helper to check if range overlaps with mask
            fun isMasked(range: IntRange): Boolean {
                return stringMatches.any { it.intersect(range).isNotEmpty() } || 
                       commentMatches.any { it.intersect(range).isNotEmpty() }
            }
            
            // Re-run highlighting logic on *visible* text (Skipping masks)
            Regex("\\b\\d+\\.?\\d*\\w*\\b").findAll(textToHighlight).forEach { match ->
                 if (!isMasked(match.range)) {
                     highlightedBuilder.addStyle(SpanStyle(color = numberColor), match.range.first, match.range.last + 1)
                 }
            }
            Regex("\\b(" + syntaxKeywords.joinToString("|") + ")\\b").findAll(textToHighlight).forEach { match ->
                 if (!isMasked(match.range)) {
                     highlightedBuilder.addStyle(SpanStyle(color = keywordColor, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
                 }
            }
             Regex("\\b[A-Z][a-zA-Z0-9_]*\\b").findAll(textToHighlight).forEach { match ->
                if (!syntaxKeywords.contains(match.value) && !isMasked(match.range)) {
                     highlightedBuilder.addStyle(SpanStyle(color = classColor), match.range.first, match.range.last + 1)
                }
            }
            Regex("\\b([a-z][A-Za-z0-9_]*)\\s*\\(").findAll(textToHighlight).forEach { match ->
                val range = match.groups[1]?.range
                if (range != null && match.groups[1]?.value !in syntaxKeywords && !isMasked(range)) {
                     highlightedBuilder.addStyle(SpanStyle(color = functionColor), range.first, range.last + 1)
                }
            }
            Regex("@[A-Za-z0-9_]+").findAll(textToHighlight).forEach { match ->
                 if (!isMasked(match.range)) {
                     highlightedBuilder.addStyle(SpanStyle(color = Color(0xFF9E880D)), match.range.first, match.range.last + 1)
                 }
            }
            
            // Apply String & Comment Styles (The Mask styles)
            stringMatches.forEach { range ->
                highlightedBuilder.addStyle(SpanStyle(color = stringColor), range.first, range.last + 1)
            }
            commentMatches.forEach { range ->
                highlightedBuilder.addStyle(SpanStyle(color = commentColor, fontStyle = FontStyle.Italic), range.first, range.last + 1)
            }
            
            // Search Highlighting
             if (isSearchVisible && searchResults.isNotEmpty()) {
                searchResults.forEachIndexed { index, range ->
                     val startTransformed = mapping.originalToTransformed(range.first)
                     val endTransformed = mapping.originalToTransformed(range.last + 1)
                     if (startTransformed < endTransformed) {
                         val color = if (index == currentMatchIndex) Color(0xFFFFB74D) else Color(0xFFFFF9C4)
                         highlightedBuilder.addStyle(SpanStyle(background = color), startTransformed, endTransformed)
                     }
                }
             }

            // Bracket Highlighting
            matchedBrackets?.let { (start, end) ->
                val startTransformed = mapping.originalToTransformed(start)
                val endTransformed = mapping.originalToTransformed(end)

                // Highlight First Bracket
                if (startTransformed < transformedText.length) {
                    highlightedBuilder.addStyle(
                        SpanStyle(background = bracketHighlightBg, color = bracketHighlightColor, fontWeight = FontWeight.Bold), 
                        startTransformed, 
                        startTransformed + 1
                    )
                }

                // Highlight Second Bracket
                 if (endTransformed < transformedText.length) {
                    highlightedBuilder.addStyle(
                        SpanStyle(background = bracketHighlightBg, color = bracketHighlightColor, fontWeight = FontWeight.Bold), 
                        endTransformed, 
                        endTransformed + 1
                    )
                }
            }

            TransformedText(highlightedBuilder.toAnnotatedString(), mapping)
        }
    }


    
    // Main UI
    Column(modifier = Modifier.fillMaxSize()) {
        
        // Docked Search Bar
        if (isSearchVisible) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f), // Ensure shadow shows
                elevation = 4.dp,
                color = Color(0xFFF2F2F2),
                shape = androidx.compose.ui.graphics.RectangleShape // Docked style
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LaunchedEffect(Unit) {
                        searchFocusRequester.requestFocus()
                    }
                    
                    // Search Input
                    Box(modifier = Modifier.weight(1f).height(32.dp).background(Color.White, RoundedCornerShape(2.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(2.dp))) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .focusRequester(searchFocusRequester)
                                .onKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyUp) {
                                        when (event.key) {
                                            Key.Enter -> {
                                                 if (event.isShiftPressed) prevMatch() else nextMatch()
                                                 true
                                            }
                                            Key.DirectionDown -> { nextMatch(); true }
                                            Key.DirectionUp -> { prevMatch(); true }
                                            else -> false
                                        }
                                    } else false
                                },
                            textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                            singleLine = true,
                            cursorBrush = SolidColor(Color.Black),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Find...", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    innerTextField()
                                }
                            }
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
                    IconButton(onClick = { saveCode(textFieldValue.text) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Gray)
                    }
                    IconButton(onClick = { isSearchVisible = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            }
        }

        // Editor Content Area
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val boxMaxHeight = maxHeight
            
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
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { 
                    detectTapGestures(onTap = { 
                        val prevScroll = scrollState.value
                        editorFocusRequester.requestFocus() 
                        textFieldValue = textFieldValue.copy(selection = TextRange(textFieldValue.text.length))
                        scope.launch { scrollState.scrollTo(prevScroll) }
                    }) 
                }
            ) {
                
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = if (boxMaxHeight != androidx.compose.ui.unit.Dp.Infinity && boxMaxHeight.value.isFinite()) boxMaxHeight else 0.dp)
                ) {
                    
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .focusProperties { canFocus = false } // Prevent Box from stealing focus
                        .pointerInput(Unit) { 
                            detectTapGestures(
                                onTap = { offset ->
                                    layoutResult?.let { layout ->
                                        val adjustedOffset = offset.copy(x = offset.x + horizontalScrollState.value.toFloat())
                                        val position = layout.getOffsetForPosition(adjustedOffset)
                                        textFieldValue = textFieldValue.copy(selection = TextRange(position))
                                        // Request focus AFTER updating selection to avoid scroll jump to old position
                                        editorFocusRequester.requestFocus()
                                    }
                                },
                                onDoubleTap = { offset ->
                                    layoutResult?.let { layout ->
                                        val adjustedOffset = offset.copy(x = offset.x + horizontalScrollState.value.toFloat())
                                        val position = layout.getOffsetForPosition(adjustedOffset)
                                        val wordRange = layout.getWordBoundary(position)
                                        textFieldValue = textFieldValue.copy(selection = wordRange)
                                        editorFocusRequester.requestFocus()
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    layoutResult?.let { layout ->
                                        // Find closest offset to click
                                        val adjustedOffset = offset.copy(x = offset.x + horizontalScrollState.value.toFloat())
                                        val position = layout.getOffsetForPosition(adjustedOffset)
                                        dragStartOffset = position
                                        textFieldValue = textFieldValue.copy(selection = TextRange(position))
                                        editorFocusRequester.requestFocus()
                                    }
                                },
                                onDrag = { change, _ ->
                                    layoutResult?.let { layout ->
                                        val adjustedOffset = change.position.copy(x = change.position.x + horizontalScrollState.value.toFloat())
                                        val position = layout.getOffsetForPosition(adjustedOffset)
                                        textFieldValue = textFieldValue.copy(selection = TextRange(dragStartOffset, position))
                                    }
                                }
                            )
                        }
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
                            onTextLayout = { layoutResult = it },
                            modifier = Modifier
                                .widthIn(min = 100.dp) // Minimum width
                                .focusRequester(editorFocusRequester)
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown) {
                                        // Save Shortcut
                                        if ((event.isMetaPressed || event.isCtrlPressed) && event.key == Key.S) {
                                            saveCode(textFieldValue.text)
                                            return@onPreviewKeyEvent true
                                        }

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
                                        
                                        // Auto-indentation on Enter
                                        if (event.key == Key.Enter && suggestions.isEmpty()) {
                                            val text = textFieldValue.text
                                            val cursor = textFieldValue.selection.start
                                            
                                            // Find current line start
                                            val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
                                            val currentLine = text.substring(lineStart, cursor)
                                            
                                            // Calculate indentation (count leading spaces/tabs)
                                            val indentation = currentLine.takeWhile { it == ' ' || it == '\t' }
                                            
                                            // Check if previous line ends with opening brace
                                            val trimmedLine = currentLine.trim()
                                            val extraIndent = if (trimmedLine.endsWith("{")) "    " else ""
                                            
                                            // Insert newline with indentation
                                            val newText = text.substring(0, cursor) + "\n" + indentation + extraIndent + text.substring(cursor)
                                            val newCursor = cursor + 1 + indentation.length + extraIndent.length
                                            
                                            textFieldValue = TextFieldValue(
                                                text = newText,
                                                selection = TextRange(newCursor)
                                            )
                                            onCodeChange(newText)
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
                    // VerticalScrollbar (inside BoxWithConstraints for .align() scope)
                    // (VerticalScrollbar is already below, removing comment if duplicate)
                } // End Editor Area
            } // End Row
            
        } // End Scrollable Content Layer
        
        // VerticalScrollbar
        // Guard against infinite height
        val vScrollModifier = if (boxMaxHeight != androidx.compose.ui.unit.Dp.Infinity && boxMaxHeight.value.isFinite()) {
            Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        } else {
             Modifier.align(Alignment.CenterEnd).height(100.dp) // Fallback
        }
        
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = vScrollModifier,
            style = androidx.compose.foundation.defaultScrollbarStyle().copy(
                thickness = 8.dp,
                hoverDurationMillis = 300
            )
        )
        
        // HorizontalScrollbar
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(horizontalScrollState),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 60.dp)
                .fillMaxWidth(),
            style = androidx.compose.foundation.defaultScrollbarStyle().copy(
                thickness = 8.dp,
                hoverDurationMillis = 300
            )
        )
    }
}
}

// Helper to find line end offset
fun String.lineEnd(lineIndex: Int): Int {
    var count = 0
    var offset = 0
    for (line in lines()) {
        if (count == lineIndex) return offset + line.length
        offset += line.length + 1 // +1 for newline
        count++
    }
    return length
}

// Helper to find line start offset
fun String.lineStart(lineIndex: Int): Int {
    var count = 0
    var offset = 0
    for (line in lines()) {
        if (count == lineIndex) return offset
        offset += line.length + 1
        count++
    }
    return length
}
