package com.example.alleycat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alleycat.ui.theme.AlleyCatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.init()
        HapticFeedback.init(this)
        setContent {
            AlleyCatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    GameScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        // Clean up audio and haptic resources to prevent memory leaks
        SoundManager.release()
        HapticFeedback.release()
        super.onDestroy()
    }
}

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.gameState.collectAsState()

    if (state.isLoading) {
        LoadingScreen()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Game Canvas
            GameCanvas(state)

            // --- Control Zones (Left and Right for horizontal movement) ---
            if (state.isGameStarted && !state.isGameOver && !state.showInstructions && !state.showLevelComplete) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val screenWidth = size.width

                                    // Handle press/hold for movement, release to stop
                                    event.changes.forEach { change ->
                                        if (change.pressed) {
                                            val x = change.position.x
                                            when {
                                                x < screenWidth * 0.3f -> {
                                                    viewModel.stopMoveRight()
                                                    viewModel.moveLeft()
                                                }
                                                x > screenWidth * 0.7f -> {
                                                    viewModel.stopMoveLeft()
                                                    viewModel.moveRight()
                                                }
                                                else -> {
                                                    // Center zone - jump on press
                                                    if (change.changedToDown()) {
                                                        viewModel.jump()
                                                    }
                                                }
                                            }
                                        } else {
                                            // Finger lifted - stop all movement
                                            viewModel.stopMoveLeft()
                                            viewModel.stopMoveRight()
                                        }
                                        change.consume()
                                    }
                                }
                            }
                        }
                )
            }

            // --- HUD ---
            if (state.isGameStarted && !state.isGameOver) {
                ScoreHud(state.score, state.streak, state.lives, state.currentLevel)
                
                // Pause button
                Button(
                    onClick = { viewModel.togglePause() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF444444)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                    contentPadding = PaddingValues(12.dp, 8.dp)
                ) {
                    Text(if (state.isPaused) "RESUME" else "PAUSE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // --- Pause Overlay ---
            if (state.isPaused && state.isGameStarted && !state.isGameOver) {
                PauseOverlay(
                    onResume = { viewModel.resume() },
                    onHome = { viewModel.resetToHome() }
                )
            }

            // --- Splash Screen Overlay ---
            if (!state.isGameStarted && !state.showInstructions) {
                SplashScreen(
                    onStart = { viewModel.startGame() },
                    onShowInstructions = { viewModel.showInstructions() }
                )
            }

            // --- Instructions Overlay ---
            if (state.showInstructions) {
                InstructionsOverlay(onClose = { viewModel.hideInstructions() })
            }

            // --- Level Complete Overlay ---
            if (state.showLevelComplete) {
                val levelData = LevelSystem.getLevelData(state.currentLevel)
                val nextLevel = state.currentLevel + 1
                val isLastMainLevel = state.currentLevel >= LevelSystem.getTotalLevels()
                LevelCompleteOverlay(
                    currentLevel = state.currentLevel,
                    levelName = levelData.name,
                    score = state.score,
                    hasNextLevel = true,  // Always allow continuing (infinite levels)
                    isLastMainLevel = isLastMainLevel,
                    onContinue = { viewModel.startNextLevel() },
                    onHome = { viewModel.resetToHome() }
                )
            }

            // --- Game Over Overlay ---
            if (state.isGameOver) {
                GameOverOverlay(
                    score = state.score,
                    highScore = state.highScore,
                    onHome = { viewModel.resetToHome() },
                    onRestart = { viewModel.startGame() }
                )
            }
        }
    }
}

@Composable
fun GameCanvas(state: GameState) {
    // Cache image resources to prevent reloading on every recomposition
    val catIdle = ImageBitmap.imageResource(id = R.drawable.cat_idle)
    val catJump = ImageBitmap.imageResource(id = R.drawable.cat_jump)
    val dustbinImg = ImageBitmap.imageResource(id = R.drawable.dustbin)
    val hazardDogImg = ImageBitmap.imageResource(id = R.drawable.hazard_dog)
    val hazardCatImg = ImageBitmap.imageResource(id = R.drawable.hazard_crazy_cat)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val logicalHeight = LOGICAL_HEIGHT
        val canvasScale = size.height / logicalHeight
        
        // --- Parallax Background (drawn directly for performance) ---
        // Sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A1A),
                    Color(0xFF0D1B2A),
                    Color(0xFF1B2838),
                    Color(0xFF1A1A2E)
                )
            )
        )
        
        // Scrolling city silhouette effect
        val parallaxOffset = (state.distanceTraveled * PARALLAX_SPEED_FACTOR * canvasScale) % size.width
        val buildingColor = Color(0xFF0F1520)
        val buildingY = size.height * 0.35f
        
        // Simple building silhouettes that scroll
        for (i in 0..12) {
            val baseX = (i * size.width / 6f) - parallaxOffset
            val adjustedX = if (baseX < -size.width / 6f) baseX + size.width * 2f else baseX
            val bHeight = size.height * (0.15f + (i % 4) * 0.05f)
            val bWidth = size.width / 14f
            drawRect(
                color = buildingColor,
                topLeft = Offset(adjustedX, buildingY + (size.height * 0.3f - bHeight)),
                size = Size(bWidth, bHeight + size.height * 0.5f)
            )
        }
        
        // Ground area
        val groundLineY = GROUND_Y * canvasScale
        drawRect(
            color = Color(0xFF1A1A2E),
            topLeft = Offset(0f, groundLineY),
            size = Size(size.width, size.height - groundLineY)
        )

        // --- Ground Line ---
        drawLine(
            color = Color.DarkGray.copy(alpha = GROUND_LINE_ALPHA),
            start = Offset(0f, groundLineY),
            end = Offset(size.width, groundLineY),
            strokeWidth = GROUND_LINE_WIDTH
        )

        // --- Dustbins & Hazards ---
        state.dustbins.forEach { bin ->
            val scaledX = bin.x * canvasScale
            val scaledY = DUSTBIN_TOP_Y * canvasScale
            val scaledWidth = bin.width * canvasScale
            val binHeight = DUSTBIN_HEIGHT * canvasScale

            // Glow to soften the "boxiness"
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Cyan.copy(alpha = DUST_BIN_GLOW_ALPHA), Color.Transparent),
                    center = Offset(scaledX + scaledWidth / 2, scaledY + binHeight / 2),
                    radius = scaledWidth * 0.8f
                ),
                center = Offset(scaledX + scaledWidth / 2, scaledY + binHeight / 2),
                radius = scaledWidth * 0.8f
            )

            // Hazard pop up
            if (bin.hasHazard && bin.hazardType != HazardType.NONE) {
                val hazardImg = if (bin.hazardType == HazardType.DOG) hazardDogImg else hazardCatImg
                val hzHeight = HAZARD_HEIGHT * canvasScale
                val hzWidth = HAZARD_WIDTH * canvasScale
                val hxX = scaledX + (scaledWidth / 2) - (hzWidth / 2)
                val hzBaseY = scaledY + (100f * canvasScale)
                val currentY = hzBaseY - (bin.hazardYOffset * hzHeight)
                
                drawImage(
                    image = hazardImg,
                    dstOffset = IntOffset(hxX.toInt(), currentY.toInt()),
                    dstSize = IntSize(hzWidth.toInt(), hzHeight.toInt())
                )
            }

            // Dustbin
            drawImage(
                image = dustbinImg,
                dstOffset = IntOffset(scaledX.toInt(), scaledY.toInt()),
                dstSize = IntSize(scaledWidth.toInt(), binHeight.toInt())
            )
        }

        // --- The Cat ---
        val catSize = 120f * canvasScale
        val currentCatImg = when (state.catState) {
            CatState.JUMPING -> catJump
            CatState.FALLING -> catJump  // Use jump image for falling too (or could have separate falling image)
            CatState.DEAD -> catIdle  // Use idle for dead (faded out would be better)
            else -> catIdle
        }
        val catDrawX = state.catX * canvasScale
        val catDrawY = state.catY * canvasScale - catSize
        
        // Neon glow for the cat
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Magenta.copy(alpha = CAT_GLOW_ALPHA), Color.Transparent),
                center = Offset(catDrawX + catSize / 2, catDrawY + catSize / 2),
                radius = catSize
            ),
            center = Offset(catDrawX + catSize / 2, catDrawY + catSize / 2),
            radius = catSize
        )

        drawImage(
            image = currentCatImg,
            dstOffset = IntOffset(catDrawX.toInt(), catDrawY.toInt()),
            dstSize = IntSize(catSize.toInt(), catSize.toInt())
        )
        
        // Draw state indicator for debugging/feedback
        if (state.catState == CatState.DEAD) {
            drawCircle(
                color = Color.Red.copy(alpha = 0.3f),
                radius = catSize,
                center = Offset(catDrawX + catSize / 2, catDrawY + catSize / 2)
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    // Pulsing glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Cat eyes blinking - simple pulse
    val eyeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, delayMillis = 2800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )
    
    // Paw print trail animation
    val pawProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "paws"
    )

    // Loading dots animation
    val dotsAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600), repeatMode = RepeatMode.Reverse
        ), label = "dot1"
    )
    val dotsAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200), repeatMode = RepeatMode.Reverse
        ), label = "dot2"
    )
    val dotsAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400), repeatMode = RepeatMode.Reverse
        ), label = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A1A),
                        Color(0xFF0D1B2A),
                        Color(0xFF1A1A2E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Paw print trail at the bottom
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            val pawSize = 12f
            val spacing = size.width / 6
            for (i in 0..5) {
                val alpha = ((pawProgress * 6 - i).coerceIn(0f, 1f)) * 0.6f
                val offsetY = if (i % 2 == 0) 0f else 15f
                // Main pad
                drawCircle(
                    color = Color.Cyan.copy(alpha = alpha),
                    radius = pawSize,
                    center = Offset(spacing * i + spacing / 2, size.height / 2 + offsetY)
                )
                // Toe beans
                drawCircle(
                    color = Color.Cyan.copy(alpha = alpha * 0.8f),
                    radius = pawSize * 0.4f,
                    center = Offset(spacing * i + spacing / 2 - 8f, size.height / 2 + offsetY - 14f)
                )
                drawCircle(
                    color = Color.Cyan.copy(alpha = alpha * 0.8f),
                    radius = pawSize * 0.4f,
                    center = Offset(spacing * i + spacing / 2 + 8f, size.height / 2 + offsetY - 14f)
                )
                drawCircle(
                    color = Color.Cyan.copy(alpha = alpha * 0.8f),
                    radius = pawSize * 0.35f,
                    center = Offset(spacing * i + spacing / 2, size.height / 2 + offsetY - 18f)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Cat face icon with glowing eyes
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Outer glow ring
                Canvas(modifier = Modifier.size(160.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Magenta.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }
                
                // Cat face
                Canvas(modifier = Modifier.size(120.dp)) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    
                    // Head
                    drawCircle(
                        color = Color(0xFFFFA500),
                        radius = 45f,
                        center = Offset(cx, cy + 5f)
                    )
                    
                    // Left ear
                    drawPath(
                        path = Path().apply {
                            moveTo(cx - 30f, cy - 20f)
                            lineTo(cx - 38f, cy - 55f)
                            lineTo(cx - 12f, cy - 30f)
                            close()
                        },
                        color = Color(0xFFFF8C00)
                    )
                    // Right ear
                    drawPath(
                        path = Path().apply {
                            moveTo(cx + 30f, cy - 20f)
                            lineTo(cx + 38f, cy - 55f)
                            lineTo(cx + 12f, cy - 30f)
                            close()
                        },
                        color = Color(0xFFFF8C00)
                    )
                    
                    // Inner ears
                    drawPath(
                        path = Path().apply {
                            moveTo(cx - 28f, cy - 22f)
                            lineTo(cx - 34f, cy - 48f)
                            lineTo(cx - 15f, cy - 28f)
                            close()
                        },
                        color = Color(0xFFFFB6C1)
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(cx + 28f, cy - 22f)
                            lineTo(cx + 34f, cy - 48f)
                            lineTo(cx + 15f, cy - 28f)
                            close()
                        },
                        color = Color(0xFFFFB6C1)
                    )
                    
                    // Eyes with glow
                    val eyeColor = Color(0xFF00FF00).copy(alpha = glowAlpha)
                    drawCircle(color = eyeColor, radius = 10f * eyeScale.coerceAtLeast(0.3f), center = Offset(cx - 15f, cy))
                    drawCircle(color = eyeColor, radius = 10f * eyeScale.coerceAtLeast(0.3f), center = Offset(cx + 15f, cy))
                    
                    // Pupils
                    if (eyeScale > 0.5f) {
                        drawCircle(color = Color.Black, radius = 5f, center = Offset(cx - 15f, cy))
                        drawCircle(color = Color.Black, radius = 5f, center = Offset(cx + 15f, cy))
                    }
                    
                    // Nose
                    drawPath(
                        path = Path().apply {
                            moveTo(cx - 4f, cy + 14f)
                            lineTo(cx, cy + 10f)
                            lineTo(cx + 4f, cy + 14f)
                            close()
                        },
                        color = Color(0xFFFF69B4)
                    )
                    
                    // Whiskers
                    drawLine(Color.Gray, Offset(cx - 45f, cy + 8f), Offset(cx - 18f, cy + 10f), strokeWidth = 1.5f)
                    drawLine(Color.Gray, Offset(cx - 45f, cy + 14f), Offset(cx - 18f, cy + 14f), strokeWidth = 1.5f)
                    drawLine(Color.Gray, Offset(cx + 18f, cy + 10f), Offset(cx + 45f, cy + 8f), strokeWidth = 1.5f)
                    drawLine(Color.Gray, Offset(cx + 18f, cy + 14f), Offset(cx + 45f, cy + 14f), strokeWidth = 1.5f)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Game title
            Text(
                text = "ALLEY CAT",
                color = Color(0xFFFF00FF),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Cursive,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(color = Color(0xAADD00DD), blurRadius = 30f)
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "RETRO ARCADE SURVIVAL",
                color = Color.Cyan.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Loading indicator with animated dots
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "LOADING",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(".", color = Color.Cyan.copy(alpha = dotsAlpha1), fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(".", color = Color.Cyan.copy(alpha = dotsAlpha2), fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(".", color = Color.Cyan.copy(alpha = dotsAlpha3), fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SplashScreen(onStart: () -> Unit, onShowInstructions: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Floating animation for title
    val titleOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleFloat"
    )
    
    // Button pulse
    val buttonGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonGlow"
    )
    
    // Star twinkle
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stars"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A1A),
                        Color(0xFF110033),
                        Color(0xFF0D1B2A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            val starPositions = listOf(
                Offset(size.width * 0.1f, size.height * 0.08f),
                Offset(size.width * 0.25f, size.height * 0.15f),
                Offset(size.width * 0.4f, size.height * 0.05f),
                Offset(size.width * 0.6f, size.height * 0.12f),
                Offset(size.width * 0.75f, size.height * 0.07f),
                Offset(size.width * 0.9f, size.height * 0.18f),
                Offset(size.width * 0.15f, size.height * 0.25f),
                Offset(size.width * 0.85f, size.height * 0.3f),
                Offset(size.width * 0.5f, size.height * 0.22f),
                Offset(size.width * 0.35f, size.height * 0.88f),
                Offset(size.width * 0.7f, size.height * 0.85f),
                Offset(size.width * 0.92f, size.height * 0.9f),
            )
            starPositions.forEachIndexed { index, pos ->
                val alpha = if (index % 2 == 0) starAlpha else 1f - starAlpha + 0.3f
                drawCircle(
                    color = Color.White.copy(alpha = alpha.coerceIn(0.1f, 0.9f)),
                    radius = if (index % 3 == 0) 2f else 1.5f,
                    center = pos
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Cat emoji with glow
            Text(
                text = "🐱",
                fontSize = 64.sp,
                modifier = Modifier.graphicsLayer { translationY = titleOffset * 2 }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main title with float animation
            Text(
                text = "ALLEY CAT",
                color = Color(0xFFFF00FF),
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Cursive,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(color = Color(0xAADD00DD), blurRadius = 40f)
                ),
                modifier = Modifier.graphicsLayer { translationY = titleOffset }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "RETRO ARCADE SURVIVAL",
                color = Color.Cyan,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tagline
            Text(
                text = "Jump. Dodge. Survive the alley.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Start button with animated glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(240.dp)
                    .height(60.dp)
            ) {
                // Glow behind button
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00FFFF).copy(alpha = buttonGlow * 0.4f),
                                Color(0xFF0088FF).copy(alpha = buttonGlow * 0.4f)
                            )
                        ),
                        cornerRadius = CornerRadius(32f, 32f),
                        size = Size(size.width + 8f, size.height + 8f),
                        topLeft = Offset(-4f, -4f)
                    )
                }
                
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00FFFF), Color(0xFF0088FF))
                            )
                        )
                ) {
                    Text(
                        "▶  START GAME",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = onShowInstructions) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "How to play", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("HOW TO PLAY", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Version info at bottom
            Text(
                text = "v1.0 • Classic Arcade Reimagined",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LevelCompleteOverlay(
    currentLevel: Int,
    levelName: String,
    score: Int,
    hasNextLevel: Boolean,
    isLastMainLevel: Boolean = false,
    onContinue: () -> Unit,
    onHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2D5016), Color(0xFF0D2F0F))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(0xFF1A1A1A), RoundedCornerShape(28.dp))
                .border(3.dp, Color.Green.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                .padding(40.dp)
        ) {
            Text(
                text = "🎉",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "LEVEL COMPLETE!",
                color = Color.Green,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(color = Color.Green, blurRadius = 30f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = levelName,
                color = Color.Cyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "SCORE: $score",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (isLastMainLevel) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "LEVEL ${currentLevel + 1}: MYSTERY ALLEYS UNLOCKED!",
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (hasNextLevel) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onHome,
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("MENU", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onContinue,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("NEXT LEVEL", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("YOU WIN!", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun InstructionsOverlay(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClose() })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 450.dp)
                .padding(24.dp)
                .background(Color(0xFF121212), RoundedCornerShape(28.dp))
                .border(2.dp, Color.Cyan.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAME MANUAL",
                color = Color.Cyan,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            InstructionRow("⬅️➡️", "TAP LEFT/RIGHT SIDES to move. The cat can dodge left and right to avoid hazards!")
            InstructionRow("👆", "TAP CENTER to Jump. Timing is everything!")
            InstructionRow("🗑️", "STAY ON THE BINS. If you miss a bin and fall into the alley, you lose a life.")
            InstructionRow("🐕", "DANGER! Dogs and crazy cats pop out of bins. Dodge or jump over them!")
            InstructionRow("🔥", "STREAKS: Land on different bins for huge score multipliers!")
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF00FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LET'S GO!", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun InstructionRow(icon: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 28.sp)
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            color = Color.LightGray,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ScoreHud(score: Int, streak: Int, lives: Int, currentLevel: Int) {
    val levelData = LevelSystem.getLevelData(currentLevel)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, end = 24.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = levelData.name,
            color = Color.Cyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        
        Text(
            text = "$score",
            color = Color.White,
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(color = Color.Cyan, blurRadius = 20f)
            )
        )
        if (streak >= 5) {
            Text(
                text = "🔥 $streak STREAK",
                color = Color.Yellow,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(color = Color.Red, blurRadius = 10f)
                )
            )
        }
    }

    // Lives counter at top-left
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 24.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(lives) {
            Text(
                text = "❤️",
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Text(
            text = if (lives == 1) "$lives LIFE" else "$lives LIVES",
            color = Color.Red,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GameOverOverlay(score: Int, highScore: Int, onHome: () -> Unit, onRestart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(0xFF1A1A1A), RoundedCornerShape(24.dp))
                .border(2.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(40.dp)
        ) {
            Text(
                text = "GAME OVER",
                color = Color.Red,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(color = Color.Red, blurRadius = 40f)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "SCORE: $score", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(text = "BEST: $highScore", color = Color.Gray, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onHome,
                    border = BorderStroke(1.dp, Color.Gray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("MENU", color = Color.White)
                }
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("PLAY AGAIN", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/**
 * Pause overlay displayed when the game is paused.
 */
@Composable
fun PauseOverlay(onResume: () -> Unit, onHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color(0xFF1A1A1A), RoundedCornerShape(24.dp))
                .border(3.dp, Color(0xFFFFFF00).copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .padding(40.dp)
        ) {
            Text(
                text = "⏸️",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "GAME PAUSED",
                color = Color(0xFFFFFF00),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(color = Color(0xFFFFFF00), blurRadius = 20f)
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onHome,
                    border = BorderStroke(1.dp, Color.Gray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("MENU", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("RESUME", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
