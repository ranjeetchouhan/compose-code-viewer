package samples

data class CodeSample(
    val title: String,
    val description: String,
    val code: String,
    val category: String
)

object SampleLibrary {
    val samples = listOf(
        CodeSample(
            title = "Hello World",
            description = "Simple text display",
            category = "Basics",
            code = """@Composable
fun HelloWorld() {
    Text(
        text = "Hello, Compose!",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Blue
    )
}"""
        ),
        CodeSample(
            title = "Button Click",
            description = "Interactive button with state",
            category = "Basics",
            code = """@Composable
fun ButtonExample() {
    var count by remember { mutableStateOf(0) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Clicked: ${'$'}count times",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { count++ }) {
            Text("Click Me!")
        }
    }
}"""
        ),
        CodeSample(
            title = "Card Layout",
            description = "Material card with content",
            category = "Layouts",
            code = """@Composable
fun CardExample() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 8.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Card Title",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This is a card with some content inside.",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}"""
        ),
        CodeSample(
            title = "LazyColumn List",
            description = "Scrollable list of items",
            category = "Layouts",
            code = """@Composable
fun ListExample() {
    val items = (1..20).map { "Item #${'$'}it" }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 4.dp
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp
                )
            }
        }
    }
}"""
        ),
        CodeSample(
            title = "Animated Counter",
            description = "Counter with smooth animation",
            category = "Animation",
            code = """@Composable
fun AnimatedCounter() {
    var count by remember { mutableStateOf(0) }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(durationMillis = 500)
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "${'$'}animatedCount",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EE)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row {
            Button(onClick = { count-- }) {
                Text("-")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { count++ }) {
                Text("+")
            }
        }
    }
}"""
        ),
        CodeSample(
            title = "Color Gradient",
            description = "Box with gradient background",
            category = "Graphics",
            code = """@Composable
fun GradientExample() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Gradient Background",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}"""
        ),
        CodeSample(
            title = "Network Image",
            description = "Load image from URL using Coil",
            category = "Advanced",
            code = """@Composable
fun NetworkImageExample() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Network Image",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        AsyncImage(
            model = "https://picsum.photos/300/200",
            contentDescription = "Random image",
            modifier = Modifier
                .size(300.dp, 200.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}"""
        ),
        CodeSample(
            title = "Progress Indicators",
            description = "Multiple progress bar styles",
            category = "Advanced",
            code = """@Composable
fun ProgressExample() {
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(50)
            progress += 0.01f
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Circular Progress
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(80.dp),
            strokeWidth = 8.dp,
            color = Color(0xFF6200EE)
        )
        
        Text(
            text = "${'$'}{(progress * 100).toInt()}%",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Linear Progress
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Linear Progress", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50)
            )
        }
        
        // Custom Styled Progress
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Custom Style", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF667eea),
                                    Color(0xFF764ba2)
                                )
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            }
        }
    }
}"""
        ),
        CodeSample(
            title = "Kinetic Radar",
            description = "Animated radar with sweep and pulse effects",
            category = "Animation",
            code = """@Composable
fun KineticRadarDemo() {

    val infinite = rememberInfiniteTransition(label = "radar")

    val sweepAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "sweep"
    )

    val pulse by infinite.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing)
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.size(280.dp)
        ) {

            val radius = size.minDimension / 2
            val center = Offset(radius, radius)

            // Grid circles
            repeat(4) { i ->
                drawCircle(
                    color = Color(0xFF1EFF6A).copy(alpha = 0.15f),
                    radius = radius * (i + 1) / 4,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }

            // Cross lines
            drawLine(
                Color(0xFF1EFF6A).copy(alpha = 0.2f),
                Offset(center.x, 0f),
                Offset(center.x, size.height),
                strokeWidth = 2f
            )
            drawLine(
                Color(0xFF1EFF6A).copy(alpha = 0.2f),
                Offset(0f, center.y),
                Offset(size.width, center.y),
                strokeWidth = 2f
            )

            // Pulse ring
            drawCircle(
                color = Color(0xFF1EFF6A).copy(alpha = 1f - pulse),
                radius = radius * pulse,
                center = center,
                style = Stroke(width = 4f)
            )

            // Sweep beam
            rotate(sweepAngle, center) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1EFF6A).copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    size = Size(radius, radius * 2),
                    topLeft = Offset(center.x, 0f)
                )
            }

            // Core glow
            drawCircle(
                color = Color(0xFF1EFF6A),
                radius = 6f,
                center = center
            )
        }
    }
}"""
        ),
        CodeSample(
            title = "Quantum Wave Field",
            description = "Flowing wave animation with layered effects",
            category = "Animation",
            code = """@Composable
fun QuantumWaveField() {

    val infinite = rememberInfiniteTransition(label = "wave")

    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "phase"
    )

    val pulse by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {

            val centerY = size.height / 2
            val width = size.width

            repeat(6) { layer ->

                val amplitude = 18f + layer * 8f
                val frequency = 0.008f + layer * 0.002f
                val speed = phase * (1f + layer * 0.15f)

                val path = Path()
                path.moveTo(0f, centerY)

                var x = 0f
                while (x <= width) {
                    val y =
                        centerY +
                        sin(x * frequency + speed).toFloat() *
                        amplitude *
                        pulse

                    path.lineTo(x, y)
                    x += 6f
                }

                drawPath(
                    path = path,
                    color = Color(0xFF00E5FF).copy(alpha = 0.08f + layer * 0.04f),
                    style = Stroke(
                        width = 2.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}"""
        ),
        CodeSample(
            title = "Live Stock Market",
            description = "Animated candlestick chart with price wave",
            category = "Advanced",
            code = """@Composable
fun LiveStockMarketDemo() {

    val infinite = rememberInfiniteTransition(label = "market")

    val time by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing)
        ),
        label = "time"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {

            val candleCount = 28
            val candleWidth = size.width / candleCount
            val centerY = size.height / 2

            // -------- PRICE WAVE (trend) --------
            val wavePath = Path()
            wavePath.moveTo(0f, centerY)

            for (i in 0..size.width.toInt() step 8) {
                val y =
                    centerY +
                    sin(i * 0.02f + time * 6f).toFloat() * 28f
                wavePath.lineTo(i.toFloat(), y)
            }

            drawPath(
                path = wavePath,
                color = Color(0xFF00E5FF),
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round
                )
            )

            // -------- CANDLESTICKS --------
            repeat(candleCount) { index ->

                val seed = index + time * 10f
                val open = centerY + sin(seed).toFloat() * 40f
                val close = centerY + sin(seed + 0.6f).toFloat() * 40f

                val high = max(open, close) + 18f
                val low = min(open, close) - 18f

                val x = index * candleWidth + candleWidth / 2
                val color =
                    if (close > open) Color(0xFF4CAF50)
                    else Color(0xFFFF5252)

                // Wick
                drawLine(
                    color = color,
                    start = Offset(x, high),
                    end = Offset(x, low),
                    strokeWidth = 3f
                )

                // Body
                drawRect(
                    color = color,
                    topLeft = Offset(
                        x - candleWidth * 0.25f,
                        min(open, close)
                    ),
                    size = Size(
                        candleWidth * 0.5f,
                        abs(close - open).coerceAtLeast(6f)
                    )
                )
            }
        }
    }
}"""
        )
    )
    
    val categories = samples.map { it.category }.distinct().sorted()
    
    fun getSamplesByCategory(category: String) = samples.filter { it.category == category }
}
