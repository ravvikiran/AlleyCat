package com.example.alleycat

/**
 * Manages game levels and difficulty progression
 */
object LevelSystem {
    
    fun getLevelData(level: Int): LevelData {
        return when (level) {
            1 -> LevelData(
                level = 1,
                name = "ALLEY BASICS",
                startingSpeed = 10f,
                maxSpeed = 18f,
                baseHazardChance = 0.2f,
                scoreToNext = 100,
                lives = 3,
                description = "Learn to jump and land on bins"
            )
            2 -> LevelData(
                level = 2,
                name = "HAZARD ALLEY",
                startingSpeed = 12f,
                maxSpeed = 20f,
                baseHazardChance = 0.35f,
                scoreToNext = 200,
                lives = 3,
                description = "More dangers appear! Avoid those hazards!"
            )
            3 -> LevelData(
                level = 3,
                name = "CHAOS CORNER",
                startingSpeed = 14f,
                maxSpeed = 22f,
                baseHazardChance = 0.5f,
                scoreToNext = 350,
                lives = 3,
                description = "Speed increases and hazards are everywhere!"
            )
            4 -> LevelData(
                level = 4,
                name = "FINAL GAUNTLET",
                startingSpeed = 16f,
                maxSpeed = 25f,
                baseHazardChance = 0.65f,
                scoreToNext = Int.MAX_VALUE,  // No level after this
                lives = 3,
                description = "The ultimate test of your skills!"
            )
            else -> LevelData(
                level = level,
                name = "MYSTERY ALLEY",
                startingSpeed = 18f + (level - 4) * 1f,
                maxSpeed = 25f,
                baseHazardChance = 0.7f,
                scoreToNext = Int.MAX_VALUE,
                lives = 3,
                description = "Beyond the known alleys..."
            )
        }
    }
    
    fun getNextLevel(currentLevel: Int): Int = currentLevel + 1
    
    fun getTotalLevels(): Int = TOTAL_LEVELS
}

data class LevelData(
    val level: Int,
    val name: String,
    val startingSpeed: Float,
    val maxSpeed: Float,
    val baseHazardChance: Float,
    val scoreToNext: Int,
    val lives: Int,
    val description: String
)
