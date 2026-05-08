package com.example.alleycat

/**
 * Central repository for all game constants and tuning values
 * Organized by category for easy maintenance
 */

// ==================== PHYSICS ====================
const val GRAVITY = 1.3f
const val JUMP_STRENGTH = -28f
const val GROUND_Y = 850f
const val DUSTBIN_TOP_Y = 600f
const val FALL_OFF_THRESHOLD = 1050f  // groundY + 200

// ==================== CAT PROPERTIES ====================
const val CAT_WIDTH = 100f
const val CAT_HEIGHT = 100f
const val CAT_SPEED = 20f  // horizontal movement speed
const val CAT_SCREEN_PADDING = 20f  // distance from screen edges

// ==================== DUSTBIN PROPERTIES ====================
const val DUSTBIN_WIDTH = 200f
const val DUSTBIN_HEIGHT = 250f
const val DUSTBIN_OUT_OF_BOUNDS_LEFT = -200f
const val HAZARD_WIDTH = 150f
const val HAZARD_HEIGHT = 150f

// ==================== HAZARD ANIMATION ====================
const val HAZARD_SPAWN_THRESHOLD_X = 1500f
const val HAZARD_ANIMATION_SPEED = 0.05f
const val HAZARD_VISIBLE_THRESHOLD = 0.3f  // minimum offset to cause collision
const val HAZARD_COLLISION_THRESHOLD = 0.5f  // hazard must be this visible to collide

// ==================== DIFFICULTY SCALING ====================
const val INITIAL_GAME_SPEED = 10f
const val MAX_GAME_SPEED = 25f
const val SPEED_INCREASE_PER_LANDING = 0.1f
const val BASE_HAZARD_PROBABILITY = 0.2f
const val HAZARD_PROBABILITY_PER_SCORE = 0.01f
const val MAX_HAZARD_PROBABILITY = 0.6f

// ==================== DUSTBIN SPAWNING ====================
const val INITIAL_DUSTBIN_COUNT = 6
const val SPAWN_THRESHOLD_X = 2000f
const val MIN_SPAWN_DISTANCE = 450f
const val MAX_SPAWN_DISTANCE = 750f

// ==================== STREAK & SCORING ====================
const val POINTS_PER_LANDING = 1
const val STREAK_BONUS_THRESHOLD = 5
const val BONUS_POINTS = 1

// ==================== LIVES SYSTEM ====================
const val INITIAL_LIVES = 3
const val LIVES_PER_LEVEL = 3

// ==================== LEVELS ====================
const val TOTAL_LEVELS = 4
const val SCORE_FOR_NEXT_LEVEL = 100  // will be scaled per level

// ==================== UI/RENDERING ====================
const val LOGICAL_HEIGHT = 1000f
const val PARALLAX_SPEED_FACTOR = 0.2f
const val DUST_BIN_GLOW_ALPHA = 0.15f
const val CAT_GLOW_ALPHA = 0.3f
const val GROUND_LINE_ALPHA = 0.5f
const val GROUND_LINE_WIDTH = 2f

// ==================== TIMING ====================
const val GAME_LOOP_TICK_MS = 16L  // ~60 FPS
const val LOADING_SCREEN_DURATION_MS = 2500L

// ==================== CAMERA/VIEWPORT ====================
const val DUST_BIN_SPAWN_X = 2500f  // where bins spawn off-screen
const val VIEWPORT_WIDTH = 1080f    // approximate screen width in logical units

// ==================== FOOD ITEMS ====================
const val FOOD_WIDTH = 60f
const val FOOD_HEIGHT = 60f
const val FOOD_INITIAL_VELOCITY_Y = -15f  // upward launch velocity when spawned
const val FOOD_POINTS = 5  // bonus score awarded on collection
const val FOOD_SPAWN_DURATION_MIN_MS = 300L  // minimum upward animation duration
const val FOOD_SPAWN_DURATION_MAX_MS = 500L  // maximum upward animation duration

// Level-specific food spawn chances (probability of food on non-hazard bins)
// These values are used in LevelData.foodSpawnChance:
//   Level 1: 0.3 (30%)
//   Level 2: 0.25 (25%)
//   Level 3: 0.2 (20%)
//   Level 4: 0.15 (15%)
//   Mystery levels: 0.1 (10%)
object LevelFoodSpawnChances {
    val level1 = 0.3f
    val level2 = 0.25f
    val level3 = 0.2f
    val level4 = 0.15f
    val mystery = 0.1f
}

// ==================== LEVEL-SPECIFIC HAZARD PROBABILITIES ====================
object LevelHazardChances {
    val level1 = 0.2f   // 20% base
    val level2 = 0.35f  // 35% base
    val level3 = 0.5f   // 50% base
    val level4 = 0.65f  // 65% base
}

// ==================== LEVEL-SPECIFIC SPEED ====================
object LevelStartingSpeeds {
    val level1 = 10f
    val level2 = 12f
    val level3 = 14f
    val level4 = 16f
}
