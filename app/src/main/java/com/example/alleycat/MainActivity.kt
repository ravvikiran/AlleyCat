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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alleycat.ui.theme.AlleyCatTheme

// === DESIGN PALETTE: Sunset Alley + Graffiti Street Art ===
private val SkyTop = Color(0xFFFF6B35)       // Warm sunset orange
private val SkyMid = Color(0xFF6B2FA0)       // Deep purple
private val SkyBottom = Color(0xFF1A0533)    // Night purple
private val BuildingDark = Color(0xFF1A0A2E) // Dark silhouette
private val BuildingMid = Color(0xFF2D1B4E)  // Mid building
private val GroundColor = Color(0xFF2D1B4E)  // Alley floor
private val BinColor = Color(0xFF4A4A5A)     // Concrete bin
private val BinLid = Color(0xFF6A6A7A)       // Lighter lid
private val GraffitiPink = Color(0xFFFF2D7B) // Graffiti accent
private val GraffitiBlue = Color(0xFF00B4D8) // Graffiti accent
private val GraffitiLime = Color(0xFFB5FF3D) // Graffiti accent
private val GraffitiYellow = Color(0xFFFFE03D)
private val WarmGold = Color(0xFFFFB347)     // Warm gold
private val CatOrangeColor = Color(0xFFFF8C00)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.init()
        HapticFeedback.init(this)
        setContent {
            AlleyCatTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = SkyBottom) {
                    GameScreen()
                }
            }
        }
    }

    override fun onDestroy() {
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
        Box(modifier = Modifier.fillMaxSize().background(SkyBottom)) {
            GameCanvas(state)

            // Tutorial overlay hints
            if (state.isTutorial && state.tutorialStep in 1..3) {
                TutorialHint(step = state.tutorialStep, onNext = { viewModel.advanceTutorial() })
            }

            // Control Zones
            if (state.isGameStarted && !state.isGameOver && !state.showLevelComplete && !state.isPaused) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f).align(Alignment.CenterStart)
                    .pointerInput(Unit) { detectTapGestures(onPress = { viewModel.moveLeft(); if (state.isTutorial && state.tutorialStep == 1) viewModel.advanceTutorial(); tryAwaitRelease(); viewModel.stopMoveLeft() }) })
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.4f).align(Alignment.Center)
                    .pointerInput(Unit) { detectTapGestures(onTap = { viewModel.jump(); if (state.isTutorial && state.tutorialStep == 2) viewModel.advanceTutorial() }) })
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f).align(Alignment.CenterEnd)
                    .pointerInput(Unit) { detectTapGestures(onPress = { viewModel.moveRight(); if (state.isTutorial && state.tutorialStep == 1) viewModel.advanceTutorial(); tryAwaitRelease(); viewModel.stopMoveRight() }) })
            }

            // HUD
            if (state.isGameStarted && !state.isGameOver && !state.isTutorial) {
                ScoreHud(state.score, state.streak, state.lives, state.currentLevel)
                Button(onClick = { viewModel.togglePause() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0x88000000)),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp), contentPadding = PaddingValues(12.dp, 8.dp)
                ) { Text(if (state.isPaused) "▶" else "⏸", color = WarmGold, fontSize = 18.sp) }
            }

            // Tutorial complete
            if (state.isTutorial && state.tutorialStep >= 4) {
                TutorialComplete(onDone = { viewModel.advanceTutorial() })
            }

            // Pause
            if (state.isPaused && state.isGameStarted && !state.isGameOver) {
                PauseOverlay(onResume = { viewModel.resume() }, onHome = { viewModel.resetToHome() })
            }

            // Splash
            if (!state.isGameStarted && !state.showInstructions) {
                SplashScreen(
                    onStart = { viewModel.startGame() },
                    onTutorial = { viewModel.startTutorial() },
                    showTutorialButton = !state.tutorialCompleted
                )
            }

            // Instructions
            if (state.showInstructions) { InstructionsOverlay(onClose = { viewModel.hideInstructions() }) }

            // Level Complete
            if (state.showLevelComplete) {
                val levelData = LevelSystem.getLevelData(state.currentLevel)
                LevelCompleteOverlay(currentLevel = state.currentLevel, levelName = levelData.name, score = state.score,
                    hasNextLevel = true, isLastMainLevel = state.currentLevel >= LevelSystem.getTotalLevels(),
                    onContinue = { viewModel.startNextLevel() }, onHome = { viewModel.resetToHome() })
            }

            // Game Over
            if (state.isGameOver) {
                GameOverOverlay(score = state.score, highScore = state.highScore, onHome = { viewModel.resetToHome() }, onRestart = { viewModel.startGame() })
            }
        }
    }
}

@Composable
fun GameCanvas(state: GameState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasScale = size.height / LOGICAL_HEIGHT

        // === SUNSET SKY GRADIENT ===
        drawRect(brush = Brush.verticalGradient(listOf(SkyTop, Color(0xFFFF3864), SkyMid, SkyBottom)))

        // Sun glow at top
        drawCircle(brush = Brush.radialGradient(listOf(Color(0x66FFB347), Color.Transparent)), radius = size.width * 0.4f, center = Offset(size.width * 0.7f, size.height * 0.08f))

        // === BUILDING SILHOUETTES ===
        val parallaxOffset = (state.distanceTraveled * PARALLAX_SPEED_FACTOR * canvasScale) % size.width
        // Far buildings (darker, slower)
        for (i in 0..8) {
            val baseX = (i * size.width / 4f) - parallaxOffset * 0.5f
            val adjustedX = if (baseX < -size.width / 4f) baseX + size.width * 2.5f else baseX
            val bHeight = size.height * (0.2f + (i % 3) * 0.08f)
            drawRect(color = BuildingDark, topLeft = Offset(adjustedX, size.height * 0.55f - bHeight), size = Size(size.width / 8f, bHeight + size.height * 0.6f))
        }
        // Near buildings (lighter, faster)
        for (i in 0..12) {
            val baseX = (i * size.width / 6f) - parallaxOffset
            val adjustedX = if (baseX < -size.width / 6f) baseX + size.width * 2.2f else baseX
            val bHeight = size.height * (0.12f + (i % 4) * 0.06f)
            drawRect(color = BuildingMid, topLeft = Offset(adjustedX, size.height * 0.7f - bHeight), size = Size(size.width / 12f, bHeight + size.height * 0.4f))
            // Graffiti splashes on some buildings
            if (i % 3 == 0) {
                val splashColor = listOf(GraffitiPink, GraffitiBlue, GraffitiLime)[i % 3]
                drawCircle(color = splashColor.copy(alpha = 0.3f), radius = size.width / 30f, center = Offset(adjustedX + size.width / 24f, size.height * 0.7f - bHeight * 0.3f))
            }
        }

        // === GROUND ===
        val groundLineY = GROUND_Y * canvasScale
        drawRect(color = GroundColor, topLeft = Offset(0f, groundLineY), size = Size(size.width, size.height - groundLineY))
        // Ground line with warm glow
        drawLine(color = WarmGold.copy(alpha = 0.4f), start = Offset(0f, groundLineY), end = Offset(size.width, groundLineY), strokeWidth = 3f)

        // === DUSTBINS ===
        state.dustbins.forEach { bin ->
            val scaledX = bin.x * canvasScale
            val scaledY = DUSTBIN_TOP_Y * canvasScale
            val scaledWidth = bin.width * canvasScale
            val binHeight = DUSTBIN_HEIGHT * canvasScale

            // Hazard
            if (bin.hasHazard && bin.hazardType != HazardType.NONE) {
                val hzHeight = HAZARD_HEIGHT * canvasScale
                val hzWidth = HAZARD_WIDTH * canvasScale
                val hxX = scaledX + (scaledWidth / 2) - (hzWidth / 2)
                val hzBaseY = scaledY + (100f * canvasScale)
                val currentY = hzBaseY - (bin.hazardYOffset * hzHeight)
                val hazardColor = if (bin.hazardType == HazardType.DOG) Color(0xFFCC4400) else GraffitiPink
                // Body
                drawOval(color = hazardColor, topLeft = Offset(hxX, currentY), size = Size(hzWidth, hzHeight * 0.65f))
                // Head
                drawCircle(color = hazardColor, radius = hzWidth * 0.2f, center = Offset(hxX + hzWidth / 2, currentY - hzWidth * 0.05f))
                // Angry eyes
                drawCircle(color = Color.White, radius = hzWidth * 0.06f, center = Offset(hxX + hzWidth * 0.35f, currentY - hzWidth * 0.08f))
                drawCircle(color = Color.White, radius = hzWidth * 0.06f, center = Offset(hxX + hzWidth * 0.65f, currentY - hzWidth * 0.08f))
            }

            // Bin body
            drawRoundRect(color = BinColor, topLeft = Offset(scaledX, scaledY), size = Size(scaledWidth, binHeight), cornerRadius = CornerRadius(6f))
            // Bin lid
            drawRoundRect(color = BinLid, topLeft = Offset(scaledX - 4f, scaledY - 12f), size = Size(scaledWidth + 8f, 16f), cornerRadius = CornerRadius(4f))
            // Graffiti tag on bin (random color per bin)
            val tagColor = listOf(GraffitiPink, GraffitiBlue, GraffitiLime, GraffitiYellow)[bin.id.hashCode().and(3)]
            drawRoundRect(color = tagColor.copy(alpha = 0.5f), topLeft = Offset(scaledX + scaledWidth * 0.2f, scaledY + binHeight * 0.4f), size = Size(scaledWidth * 0.6f, binHeight * 0.15f), cornerRadius = CornerRadius(3f))
        }

        // === THE CAT ===
        val catSize = 120f * canvasScale
        val catDrawX = state.catX * canvasScale
        val catDrawY = state.catY * canvasScale - catSize
        val catColor = when (state.catState) { CatState.DEAD -> Color(0xFF555555); CatState.JUMPING, CatState.FALLING -> Color(0xFFFFAA33); else -> CatOrangeColor }

        // Warm glow behind cat
        drawCircle(brush = Brush.radialGradient(listOf(WarmGold.copy(alpha = 0.2f), Color.Transparent), center = Offset(catDrawX + catSize / 2, catDrawY + catSize / 2), radius = catSize), center = Offset(catDrawX + catSize / 2, catDrawY + catSize / 2), radius = catSize)
        // Body
        drawRoundRect(color = catColor, topLeft = Offset(catDrawX + catSize * 0.15f, catDrawY + catSize * 0.35f), size = Size(catSize * 0.7f, catSize * 0.5f), cornerRadius = CornerRadius(catSize * 0.15f))
        // Head
        drawCircle(color = catColor, radius = catSize * 0.2f, center = Offset(catDrawX + catSize / 2, catDrawY + catSize * 0.28f))
        // Ears
        drawPath(path = Path().apply { moveTo(catDrawX + catSize * 0.32f, catDrawY + catSize * 0.16f); lineTo(catDrawX + catSize * 0.26f, catDrawY + catSize * 0.02f); lineTo(catDrawX + catSize * 0.42f, catDrawY + catSize * 0.14f); close() }, color = catColor)
        drawPath(path = Path().apply { moveTo(catDrawX + catSize * 0.68f, catDrawY + catSize * 0.16f); lineTo(catDrawX + catSize * 0.74f, catDrawY + catSize * 0.02f); lineTo(catDrawX + catSize * 0.58f, catDrawY + catSize * 0.14f); close() }, color = catColor)
        // Eyes
        val eyeColor = if (state.catState == CatState.DEAD) Color.Red else GraffitiLime
        drawCircle(color = eyeColor, radius = catSize * 0.045f, center = Offset(catDrawX + catSize * 0.4f, catDrawY + catSize * 0.26f))
        drawCircle(color = eyeColor, radius = catSize * 0.045f, center = Offset(catDrawX + catSize * 0.6f, catDrawY + catSize * 0.26f))
        // Tail
        drawLine(color = catColor, start = Offset(catDrawX + catSize * 0.75f, catDrawY + catSize * 0.55f), end = Offset(catDrawX + catSize * 0.95f, catDrawY + catSize * 0.3f), strokeWidth = catSize * 0.06f, cap = StrokeCap.Round)

        if (state.catState == CatState.DEAD) {
            drawCircle(color = Color.Red.copy(alpha = 0.25f), radius = catSize * 0.6f, center = Offset(catDrawX + catSize / 2, catDrawY + catSize / 2))
        }
    }
}

// === LOADING SCREEN ===
@Composable
fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyTop, SkyMid, SkyBottom))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🐱", fontSize = 80.sp)
            Spacer(Modifier.height(24.dp))
            Text("ALLEY CAT", color = WarmGold, fontSize = 52.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Cursive)
            Spacer(Modifier.height(8.dp))
            Text("SUNSET STREETS", color = GraffitiPink, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
            Spacer(Modifier.height(48.dp))
            Text("LOADING...", color = Color.White.copy(alpha = alpha), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

// === SPLASH SCREEN ===
@Composable
fun SplashScreen(onStart: () -> Unit, onTutorial: () -> Unit, showTutorialButton: Boolean = true) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyTop, Color(0xFFFF3864), SkyMid, SkyBottom))), contentAlignment = Alignment.Center) {
        // Graffiti splashes in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = GraffitiPink.copy(alpha = 0.15f), radius = 80f, center = Offset(size.width * 0.15f, size.height * 0.2f))
            drawCircle(color = GraffitiBlue.copy(alpha = 0.12f), radius = 60f, center = Offset(size.width * 0.85f, size.height * 0.35f))
            drawCircle(color = GraffitiLime.copy(alpha = 0.1f), radius = 100f, center = Offset(size.width * 0.5f, size.height * 0.85f))
            drawCircle(color = GraffitiYellow.copy(alpha = 0.08f), radius = 70f, center = Offset(size.width * 0.2f, size.height * 0.7f))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
            Text("🐱", fontSize = 72.sp)
            Spacer(Modifier.height(12.dp))
            Text("ALLEY CAT", color = WarmGold, fontSize = 60.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Cursive)
            Spacer(Modifier.height(4.dp))
            Text("SUNSET STREETS", color = GraffitiPink, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
            Spacer(Modifier.height(12.dp))
            Text("Jump. Dodge. Own the alley.", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(60.dp))

            // Tutorial button (shown for first-time players)
            if (showTutorialButton) {
                Button(onClick = onTutorial, colors = ButtonDefaults.buttonColors(containerColor = GraffitiLime), modifier = Modifier.width(240.dp).height(52.dp), shape = RoundedCornerShape(26.dp)) {
                    Text("🎓  PLAY TUTORIAL", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
            }

            // Start button
            Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = SkyTop), modifier = Modifier.width(240.dp).height(56.dp), shape = RoundedCornerShape(28.dp)) {
                Text("▶  START GAME", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text("v1.0 • Sunset Streets Edition", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

// === TUTORIAL HINT OVERLAY ===
@Composable
fun TutorialHint(step: Int, onNext: () -> Unit) {
    val message = when (step) {
        1 -> "👈  HOLD LEFT or RIGHT side\n     to move the cat"
        2 -> "👆  TAP the CENTER\n     to JUMP onto a bin!"
        3 -> "🎯  Great! Land on bins to score.\n     Avoid hazards. You got this!"
        else -> ""
    }
    val highlightZone = when (step) {
        1 -> "sides"
        2 -> "center"
        else -> "none"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Dim areas that aren't the target
        if (highlightZone == "sides") {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.4f).align(Alignment.Center).background(Color.Black.copy(alpha = 0.5f)))
        } else if (highlightZone == "center") {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f).align(Alignment.CenterStart).background(Color.Black.copy(alpha = 0.5f)))
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.3f).align(Alignment.CenterEnd).background(Color.Black.copy(alpha = 0.5f)))
        }

        // Hint card at top
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp).background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(16.dp)).border(2.dp, GraffitiYellow, RoundedCornerShape(16.dp)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TUTORIAL", color = GraffitiYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Text(message, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp)
            if (step == 3) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = GraffitiLime), shape = RoundedCornerShape(12.dp)) {
                    Text("GOT IT!", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// === TUTORIAL COMPLETE ===
@Composable
fun TutorialComplete(onDone: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(BuildingMid, RoundedCornerShape(24.dp)).border(3.dp, GraffitiLime, RoundedCornerShape(24.dp)).padding(40.dp)) {
            Text("🎉", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("TUTORIAL COMPLETE!", color = GraffitiLime, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            Text("You're ready for the alleys!", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
            Spacer(Modifier.height(32.dp))
            Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = SkyTop), shape = RoundedCornerShape(16.dp), modifier = Modifier.width(200.dp).height(48.dp)) {
                Text("LET'S GO!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

// === SCORE HUD ===
@Composable
fun ScoreHud(score: Int, streak: Int, lives: Int, currentLevel: Int) {
    val levelData = LevelSystem.getLevelData(currentLevel)
    Column(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, end = 20.dp), horizontalAlignment = Alignment.End) {
        Text(levelData.name, color = GraffitiBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("$score", color = WarmGold, fontSize = 52.sp, fontWeight = FontWeight.Black)
        if (streak >= 5) { Text("🔥 $streak", color = GraffitiYellow, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold) }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 50.dp, start = 20.dp), horizontalArrangement = Arrangement.Start) {
        repeat(lives) { Text("❤️", fontSize = 22.sp, modifier = Modifier.padding(end = 3.dp)) }
    }
}

// === INSTRUCTIONS OVERLAY ===
@Composable
fun InstructionsOverlay(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.95f)).pointerInput(Unit) { detectTapGestures(onTap = { onClose() }) }, contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 400.dp).padding(24.dp).background(BuildingMid, RoundedCornerShape(24.dp)).border(2.dp, GraffitiBlue.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("HOW TO PLAY", color = WarmGold, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))
            InstructionRow("⬅️➡️", "HOLD left/right to move")
            InstructionRow("👆", "TAP center to jump")
            InstructionRow("🗑️", "Land on bins to score")
            InstructionRow("🐕", "Dodge dogs & crazy cats")
            InstructionRow("🔥", "5+ streak = bonus!")
            Spacer(Modifier.height(32.dp))
            Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = SkyTop), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("GOT IT!", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun InstructionRow(icon: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.width(14.dp))
        Text(text, color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp)
    }
}

// === LEVEL COMPLETE ===
@Composable
fun LevelCompleteOverlay(currentLevel: Int, levelName: String, score: Int, hasNextLevel: Boolean, isLastMainLevel: Boolean = false, onContinue: () -> Unit, onHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1A4D1A), Color(0xFF0A2A0A)))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(BuildingMid, RoundedCornerShape(24.dp)).border(3.dp, GraffitiLime.copy(alpha = 0.7f), RoundedCornerShape(24.dp)).padding(36.dp)) {
            Text("🎉", fontSize = 64.sp)
            Spacer(Modifier.height(12.dp))
            Text("LEVEL COMPLETE!", color = GraffitiLime, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            Text(levelName, color = GraffitiBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Text("SCORE: $score", color = WarmGold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            if (isLastMainLevel) { Spacer(Modifier.height(12.dp)); Text("✨ MYSTERY ALLEYS UNLOCKED!", color = GraffitiYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(36.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onHome, border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(12.dp)) { Text("MENU", color = Color.White) }
                Button(onClick = onContinue, colors = ButtonDefaults.buttonColors(containerColor = GraffitiLime), shape = RoundedCornerShape(12.dp)) { Text("NEXT →", color = Color.Black, fontWeight = FontWeight.Black) }
            }
        }
    }
}

// === GAME OVER ===
@Composable
fun GameOverOverlay(score: Int, highScore: Int, onHome: () -> Unit, onRestart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(BuildingMid, RoundedCornerShape(24.dp)).border(2.dp, GraffitiPink.copy(alpha = 0.6f), RoundedCornerShape(24.dp)).padding(36.dp)) {
            Text("💀", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text("GAME OVER", color = GraffitiPink, fontSize = 40.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(20.dp))
            Text("SCORE: $score", color = WarmGold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("BEST: $highScore", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
            Spacer(Modifier.height(36.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onHome, border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(12.dp)) { Text("MENU", color = Color.White) }
                Button(onClick = onRestart, colors = ButtonDefaults.buttonColors(containerColor = SkyTop), shape = RoundedCornerShape(12.dp)) { Text("RETRY", color = Color.White, fontWeight = FontWeight.Black) }
            }
        }
    }
}

// === PAUSE ===
@Composable
fun PauseOverlay(onResume: () -> Unit, onHome: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(BuildingMid, RoundedCornerShape(24.dp)).border(3.dp, WarmGold.copy(alpha = 0.6f), RoundedCornerShape(24.dp)).padding(36.dp)) {
            Text("⏸️", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text("PAUSED", color = WarmGold, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(36.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onHome, border = BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(12.dp)) { Text("MENU", color = Color.White, fontWeight = FontWeight.Bold) }
                Button(onClick = onResume, colors = ButtonDefaults.buttonColors(containerColor = GraffitiLime), shape = RoundedCornerShape(12.dp)) { Text("RESUME", color = Color.Black, fontWeight = FontWeight.Black) }
            }
        }
    }
}
