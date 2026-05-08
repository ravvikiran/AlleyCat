package com.example.alleycat.generators

import com.example.alleycat.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int

/**
 * Custom Kotest arbitrary generators for GameState.
 * Generates valid GameState instances that respect game invariants.
 */
fun Arb.Companion.gameState(): Arb<GameState> = arbitrary {
    val score = Arb.int(0..1000).bind()
    val highScore = Arb.int(score..maxOf(score, 1000)).bind()
    val streak = Arb.int(0..20).bind()
    val lives = Arb.int(1..3).bind()
    val gameSpeed = Arb.float(INITIAL_GAME_SPEED..MAX_GAME_SPEED).bind()
    val catX = Arb.float(CAT_SCREEN_PADDING..(VIEWPORT_WIDTH - CAT_WIDTH - CAT_SCREEN_PADDING)).bind()
    val catY = Arb.float(0f..FALL_OFF_THRESHOLD).bind()
    val velocityY = Arb.float(JUMP_STRENGTH..50f).bind()
    val catState = Arb.enum<CatState>().bind()
    val isPaused = Arb.boolean().bind()
    val isGameOver = Arb.boolean().bind()
    val isGameStarted = Arb.boolean().bind()
    val isLoading = Arb.boolean().bind()
    val isTutorial = Arb.boolean().bind()
    val movingLeft = Arb.boolean().bind()
    val movingRight = Arb.boolean().bind()

    GameState(
        catX = catX,
        catY = catY,
        catState = catState,
        velocityY = velocityY,
        score = score,
        highScore = highScore,
        streak = streak,
        lives = lives,
        gameSpeed = gameSpeed,
        isPaused = isPaused,
        isGameOver = isGameOver,
        isGameStarted = isGameStarted,
        isLoading = isLoading,
        isTutorial = isTutorial,
        movingLeft = movingLeft,
        movingRight = movingRight
    )
}

/**
 * Generates valid (catX, catY) pairs within screen bounds.
 */
fun Arb.Companion.catPosition(): Arb<Pair<Float, Float>> = arbitrary {
    val catX = Arb.float(CAT_SCREEN_PADDING..(VIEWPORT_WIDTH - CAT_WIDTH - CAT_SCREEN_PADDING)).bind()
    val catY = Arb.float(0f..FALL_OFF_THRESHOLD).bind()
    Pair(catX, catY)
}
