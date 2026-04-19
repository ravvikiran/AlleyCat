package com.example.alleycat

import java.util.UUID

/**
 * Main game state data class holding all game information.
 * Immutable - use copy() to update state.
 */
data class GameState(
    val catY: Float = 0f,
    val catX: Float = 200f,  // Starting X position (center-ish)
    val catState: CatState = CatState.IDLE,
    val velocityY: Float = 0f,
    val score: Int = 0,
    val highScore: Int = 0,
    val streak: Int = 0,
    val lives: Int = INITIAL_LIVES,
    val currentLevel: Int = 1,
    val isGameOver: Boolean = false,
    val isGameStarted: Boolean = false,
    val isLoading: Boolean = true,
    val isPaused: Boolean = false,
    val showInstructions: Boolean = false,
    val showLevelComplete: Boolean = false,
    val dustbins: List<Dustbin> = emptyList(),
    val gameSpeed: Float = 10f,
    val distanceTraveled: Float = 0f,
    val lastLandedBinId: String = "",
    val movingLeft: Boolean = false,
    val movingRight: Boolean = false
)

/**
 * Enumeration of possible cat states during gameplay.
 */
enum class CatState {
    IDLE, JUMPING, FALLING, DEAD
}

/**
 * Data class representing a dustbin in the game world.
 * Contains position, hazard information, and animation state.
 */
data class Dustbin(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val width: Float = 200f,
    val height: Float = 250f,
    val hasHazard: Boolean = false,
    val hazardType: HazardType = HazardType.NONE,
    val hazardYOffset: Float = 0f // 0f means hidden in bin, 1f means fully visible
)

/**
 * Enumeration of possible hazard types that can emerge from dustbins.
 */
enum class HazardType {
    NONE, DOG, CRAZY_CAT
}
