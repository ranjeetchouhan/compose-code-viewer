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
            title = "Finance Dashboard",
            description = "Dashboard with charts and bottom navigation (Hybrid M2/M3)",
            category = "Layouts",
            code = """@Composable
fun FinanceDashboardUI() {

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {

        // ---------- TOP BAR ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Good evening",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Ranjeet",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        // ---------- BALANCE CARD ----------
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text("Total Balance", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text(
                    "₹1,24,560",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Income", color = Color.Gray, fontSize = 12.sp)
                        Text("₹45,000", fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("Expenses", color = Color.Gray, fontSize = 12.sp)
                        Text("₹18,400", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------- QUICK ACTIONS ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickAction("Send", Icons.Default.Send)
            QuickAction("Request", Icons.Default.CallReceived)
            QuickAction("Scan", Icons.Default.QrCode)
            QuickAction("Bills", Icons.Default.Receipt)
        }

        Spacer(Modifier.height(24.dp))

        // ---------- TRANSACTIONS ----------
        Text(
            text = "Recent Transactions",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            TransactionItem("Swiggy", "Food", "-₹420")
            TransactionItem("Uber", "Travel", "-₹280")
            TransactionItem("Salary", "Income", "+₹45,000")
        }

        Spacer(Modifier.weight(1f))

        // ---------- BOTTOM NAV ----------
        BottomNavigation(
            backgroundColor = Color.White
        ) {
            BottomNavigationItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.Home, null) },
                label = { Text("Home") },
                selectedContentColor = Color(0xFF6200EE),
                unselectedContentColor = Color.Gray
            )
            BottomNavigationItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.PieChart, null) },
                label = { Text("Stats") },
                selectedContentColor = Color(0xFF6200EE),
                unselectedContentColor = Color.Gray
            )
            BottomNavigationItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.Person, null) },
                label = { Text("Profile") },
                selectedContentColor = Color(0xFF6200EE),
                unselectedContentColor = Color.Gray
            )
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6200EE))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun TransactionItem(title: String, subtitle: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Text(
            amount,
            fontWeight = FontWeight.SemiBold,
            color = if (amount.startsWith("+")) Color(0xFF2E7D32) else Color.Black
        )
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
            description = "Counter with visual slide transitions",
            category = "Animation",
            code = """@Composable
fun AnimatedCounter() {
    var count by remember { mutableStateOf(0) }
    
    // Gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F7FA), Color(0xFFC3CFE2))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card for the counter display
            Card(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 48.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = count,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                            } else {
                                (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> height } + fadeOut())
                            }.using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { targetCount ->
                        Text(
                            text = "${'$'}targetCount",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6200EE),
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0x33000000),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                FloatingActionButton(
                    onClick = { count-- },
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF6200EE),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(32.dp))
                }
                
                FloatingActionButton(
                    onClick = { count++ },
                    backgroundColor = Color(0xFF6200EE),
                    contentColor = Color.White,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(32.dp))
                }
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
            title = "Enhanced Network Image",
            description = "Interactive image with spring animation and Coil crossfade",
            category = "Advanced",
            code = """@Composable
fun EnhancedNetworkImageExample() {
    var isExpanded by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F6))
            .padding(20.dp)
    ) {

        Text(
            text = "Network Image",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Loaded with Coil",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = 10.dp,
            modifier = Modifier
                .scale(scale)
                .clickable { isExpanded = !isExpanded }
        ) {
            Box {

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://picsum.photos/600/400")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Random image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(320.dp, 220.dp)
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.45f)
                                )
                            )
                        )
                )

                // Caption
                Text(
                    text = "Tap to expand",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isExpanded)
                "Image expanded with spring animation"
            else
                "Tap image for interaction",
            fontSize = 14.sp,
            color = Color.DarkGray
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
        ),
        CodeSample(
            title = "Neural Network Pulse",
            description = "Animated network with pulsing connections",
            category = "Animation",
            code = """@Composable
fun NeuralNetworkPulseDemo() {

    val infinite = rememberInfiniteTransition(label = "neural")

    val time by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "time"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            val center = Offset(size.width / 2, size.height / 2)
            val nodes = 14
            val radius = size.minDimension / 3.2f

            val points = List(nodes) { i ->
                val angle = (2 * Math.PI / nodes * i).toFloat()
                Offset(
                    center.x + cos(angle) * radius,
                    center.y + sin(angle) * radius
                )
            }

            // Connections
            points.forEachIndexed { i, a ->
                points.forEachIndexed { j, b ->
                    if (i != j && j % 3 == 0) {
                        drawLine(
                            color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                            start = a,
                            end = b,
                            strokeWidth = 1.5f
                        )
                    }
                }
            }

            // Pulses
            points.forEachIndexed { i, start ->

                val target = points[(i + 3) % nodes]
                val pulse = (time + i * 0.12f) % 1f

                val x = lerp(start.x, target.x, pulse)
                val y = lerp(start.y, target.y, pulse)

                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 4f,
                    center = Offset(x, y)
                )
            }

            // Nodes
            points.forEach {
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 6f,
                    center = it
                )
            }
        }
    }
}"""
        ),
        CodeSample(
            title = "Force Directed Graph",
            description = "Physics-based node simulation with spring forces",
            category = "Animation",
            code = """@Composable
fun ForceDirectedGraph() {

    data class Node(
        var position: Offset,
        var velocity: Offset = Offset.Zero
    )

    val nodes = remember {
        List(22) {
            Node(
                position = Offset(
                    Random.nextFloat() * 800f,
                    Random.nextFloat() * 800f
                )
            )
        }
    }

    val edges = remember {
        nodes.flatMapIndexed { i, _ ->
            List(2) { j -> i to Random.nextInt(nodes.size) }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)

            val repulsion = 2800f
            val spring = 0.0018f
            val damping = 0.88f

            // Repulsive forces
            for (i in nodes.indices) {
                for (j in nodes.indices) {
                    if (i == j) continue

                    val delta = nodes[i].position - nodes[j].position
                    val distance = delta.getDistance().coerceAtLeast(24f)
                    val force = delta / distance * (repulsion / (distance * distance))

                    nodes[i].velocity += force * 0.016f
                }
            }

            // Spring forces
            edges.forEach { (a, b) ->
                val delta = nodes[b].position - nodes[a].position
                val distance = delta.getDistance()
                val force = delta * spring * (distance - 140f)

                nodes[a].velocity += force * 0.016f
                nodes[b].velocity -= force * 0.016f
            }

            // Integrate
            nodes.forEach {
                it.velocity *= damping
                it.position += it.velocity
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            // Edges
            edges.forEach { (a, b) ->
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.18f),
                    start = nodes[a].position,
                    end = nodes[b].position,
                    strokeWidth = 1.5f
                )
            }

            // Nodes
            nodes.forEach {
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 6f,
                    center = it.position
                )
            }
        }
    }
}"""
        ),
        CodeSample(
            title = "Indian Ashoka Chakra",
            description = "Animated tricolour waves with rotating Chakra",
            category = "Animation",
            code = """@Composable
fun IndianAshokaChakraAnimation() {

    var rotation by remember { mutableStateOf(0f) }
    var wave by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                rotation = (rotation + 0.6f) % 360f
                wave += 0.02f
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {

            val w = size.width
            val h = size.height
            val center = Offset(w / 2, h / 2)

            // 🇮🇳 Tricolour waves
            val stripeHeight = h / 3

            listOf(
                Color(0xFFFF9933), // Saffron
                Color.White,
                Color(0xFF138808)  // Green
            ).forEachIndexed { index, color ->

                val path = Path()
                val yOffset = index * stripeHeight

                path.moveTo(0f, yOffset)

                var x = 0f
                while (x <= w) {
                    val y =
                        yOffset +
                        stripeHeight / 2 +
                        sin(x * 0.015f + wave + index).toFloat() * 12f
                    path.lineTo(x, y)
                    x += 8f
                }

                path.lineTo(w, yOffset + stripeHeight)
                path.lineTo(0f, yOffset + stripeHeight)
                path.close()

                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.9f)
                )
            }

            // Ashoka Chakra
            val chakraRadius = 64f
            val spokes = 24

            drawCircle(
                color = Color(0xFF000080),
                radius = chakraRadius,
                center = center,
                style = Stroke(width = 4f)
            )

            // Rotating spokes
            rotate(rotation, center) {
                repeat(spokes) { i ->
                    val angle = (2 * Math.PI / spokes * i).toFloat()

                    val start = Offset(
                        center.x + cos(angle) * 10f,
                        center.y + sin(angle) * 10f
                    )

                    val end = Offset(
                        center.x + cos(angle) * chakraRadius,
                        center.y + sin(angle) * chakraRadius
                    )

                    drawLine(
                        color = Color(0xFF000080),
                        start = start,
                        end = end,
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}"""
        )
    )
    
    val categories = samples.map { it.category }.distinct().sorted()
    
    fun getSamplesByCategory(category: String) = samples.filter { it.category == category }
}
