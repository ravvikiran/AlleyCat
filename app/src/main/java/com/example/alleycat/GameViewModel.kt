package com.example.alleycat

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Main game view model handling all game logic, physics, and state management.
 * Uses MVVM architecture with Jetpack Compose state flow for reactive UI updates.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val prefs = application.getSharedPreferences("alley_cat_prefs", Context.MODE_PRIVATE)
    private var lastLandedBinId: String? = null
    private var gameJob: kotlinx.coroutines.Job? = null
    
    private companion object {
        private const val TAG = "GameViewModel"
        private const val HIGH_SCORE_KEY = "high_score"
        private const val TUTORIAL_DONE_KEY = "tutorial_completed"
    }

    init {
        try {
            val savedHighScore = try {
                prefs.getInt(HIGH_SCORE_KEY, 0)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading high score from SharedPreferences, defaulting to 0", e)
                0
            }
            val tutorialDone = try {
                prefs.getBoolean(TUTORIAL_DONE_KEY, false)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading tutorial state from SharedPreferences", e)
                false
            }
            _gameState.update { it.copy(highScore = savedHighScore, tutorialCompleted = tutorialDone) }
            
            // Initial loading sequence
            viewModelScope.launch {
                delay(LOADING_SCREEN_DURATION_MS)
                _gameState.update { it.copy(isLoading = false) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ViewModel", e)
            _gameState.update { it.copy(isLoading = false, highScore = 0) }
        }
    }

    /**
     * Starts the interactive tutorial for first-time players.
     */
    fun startTutorial() {
        try {
            gameJob?.cancel()
            lastLandedBinId = null
            _gameState.update {
                GameState(
                    catY = GROUND_Y,
                    catX = VIEWPORT_WIDTH / 2,
                    isGameStarted = true,
                    isLoading = false,
                    isTutorial = true,
                    tutorialStep = 1,
                    tutorialCompleted = it.tutorialCompleted,
                    dustbins = listOf(
                        Dustbin(x = 400f),
                        Dustbin(x = 800f),
                        Dustbin(x = 1200f)
                    ),
                    highScore = it.highScore,
                    gameSpeed = 5f,  // Slower for tutorial
                    lives = 99,  // Can't die in tutorial
                    currentLevel = 1
                )
            }
            gameLoop()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tutorial", e)
        }
    }

    /**
     * Advances the tutorial to the next step.
     */
    fun advanceTutorial() {
        val currentStep = _gameState.value.tutorialStep
        if (currentStep >= 4) {
            // Tutorial complete
            prefs.edit().putBoolean(TUTORIAL_DONE_KEY, true).apply()
            _gameState.update { it.copy(isTutorial = false, tutorialCompleted = true, isGameStarted = false) }
            gameJob?.cancel()
        } else {
            _gameState.update { it.copy(tutorialStep = currentStep + 1) }
        }
    }

    /**
     * Tracks left movement during tutorial step 1.
     * Advances to step 2 when both left and right have been used.
     */
    fun onTutorialMoveLeft() {
        _gameState.update { state ->
            val updated = state.copy(tutorialMovedLeft = true)
            if (updated.tutorialMovedLeft && updated.tutorialMovedRight) {
                updated.copy(tutorialStep = 2)
            } else {
                updated
            }
        }
    }

    /**
     * Tracks right movement during tutorial step 1.
     * Advances to step 2 when both left and right have been used.
     */
    fun onTutorialMoveRight() {
        _gameState.update { state ->
            val updated = state.copy(tutorialMovedRight = true)
            if (updated.tutorialMovedLeft && updated.tutorialMovedRight) {
                updated.copy(tutorialStep = 2)
            } else {
                updated
            }
        }
    }

    /**
     * Skips the tutorial without marking it as completed.
     * Returns to splash screen with tutorial button still visible.
     */
    fun skipTutorial() {
        gameJob?.cancel()
        _gameState.update { it.copy(isTutorial = false, isGameStarted = false) }
    }

    fun startGame() {
        try {
            gameJob?.cancel()
            lastLandedBinId = null
            val levelData = LevelSystem.getLevelData(1)
            _gameState.update {
                GameState(
                    catY = GROUND_Y,
                    catX = VIEWPORT_WIDTH / 2,  // Center of screen
                    isGameStarted = true,
                    isLoading = false,
                    showInstructions = false,
                    dustbins = generateInitialDustbins(),
                    highScore = it.highScore,
                    gameSpeed = levelData.startingSpeed,
                    streak = 0,
                    lives = levelData.lives,
                    currentLevel = 1,
                    showLevelComplete = false
                )
            }
            gameLoop()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting game", e)
        }
    }
    
    /**
     * Starts the next level with increased difficulty.
     * Preserves the player's score across levels.
     */
    fun startNextLevel() {
        try {
            gameJob?.cancel()
            lastLandedBinId = null
            val nextLevel = _gameState.value.currentLevel + 1
            val levelData = LevelSystem.getLevelData(nextLevel)
            _gameState.update {
                GameState(
                    catY = GROUND_Y,
                    catX = VIEWPORT_WIDTH / 2,
                    isGameStarted = true,
                    isLoading = false,
                    showInstructions = false,
                    dustbins = generateInitialDustbins(),
                    highScore = it.highScore,
                    gameSpeed = levelData.startingSpeed,
                    streak = 0,
                    lives = levelData.lives,
                    currentLevel = nextLevel,
                    score = it.score,  // Keep score across levels
                    showLevelComplete = false
                )
            }
            gameLoop()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting next level", e)
        }
    }

    fun showInstructions() {
        _gameState.update { it.copy(showInstructions = true) }
    }

    /**
     * Hides the game instructions overlay.
     */
    fun hideInstructions() {
        _gameState.update { it.copy(showInstructions = false) }
    }

    /**
     * Starts continuous left movement of the cat.
     */
    fun moveLeft() {
        if (stateIsActionable()) {
            _gameState.update { it.copy(movingLeft = true) }
        }
    }

    /**
     * Stops left movement of the cat.
     */
    fun stopMoveLeft() {
        _gameState.update { it.copy(movingLeft = false) }
    }

    /**
     * Starts continuous right movement of the cat.
     */
    fun moveRight() {
        if (stateIsActionable()) {
            _gameState.update { it.copy(movingRight = true) }
        }
    }

    /**
     * Stops right movement of the cat.
     */
    fun stopMoveRight() {
        _gameState.update { it.copy(movingRight = false) }
    }

    /**
     * Toggles pause state. Works when game is started and not game over,
     * regardless of current pause state.
     */
    fun togglePause() {
        val state = _gameState.value
        if (state.isGameStarted && !state.isGameOver && !state.isLoading && !state.showLevelComplete) {
            _gameState.update { it.copy(isPaused = !it.isPaused) }
        }
    }

    /**
     * Resumes a paused game.
     */
    fun resume() {
        _gameState.update { it.copy(isPaused = false) }
    }

    private fun generateInitialDustbins(): List<Dustbin> {
        return try {
            val list = mutableListOf<Dustbin>()
            val levelData = LevelSystem.getLevelData(_gameState.value.currentLevel)
            list.add(Dustbin(x = 500f)) // first safe bin - no food on first bin
            for (i in 1..INITIAL_DUSTBIN_COUNT) {
                val lastX = list.lastOrNull()?.x ?: 500f
                val distance = Random.nextFloat() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE) + MIN_SPAWN_DISTANCE
                // Determine hazard
                val spawnHazard = Random.nextFloat() < levelData.baseHazardChance
                val hazardType = if (spawnHazard) {
                    if (Random.nextBoolean()) HazardType.DOG else HazardType.CRAZY_CAT
                } else HazardType.NONE
                // Mutual exclusion: food only on non-hazard bins
                val hasFood = if (!spawnHazard) {
                    Random.nextFloat() < levelData.foodSpawnChance
                } else false
                list.add(
                    Dustbin(
                        x = lastX + distance,
                        hasHazard = spawnHazard,
                        hazardType = hazardType,
                        hasFood = hasFood
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error generating initial dustbins", e)
            emptyList()
        }
    }

    private fun gameLoop() {
        try {
            gameJob = viewModelScope.launch {
                while (true) {
                    val currentState = _gameState.value
                    if (!currentState.isGameStarted || currentState.isGameOver) {
                        break
                    }
                    try {
                        update()
                        delay(GAME_LOOP_TICK_MS)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in game loop update", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting game loop", e)
        }
    }

    private fun update() {
        _gameState.update { state ->
            // Skip physics updates when paused, but allow pause/resume
            if (state.isPaused) {
                return@update state
            }

            // Update cat horizontal position
            var newCatX = state.catX
            if (state.movingLeft) {
                newCatX = (state.catX - CAT_SPEED).coerceAtLeast(CAT_SCREEN_PADDING)
            }
            if (state.movingRight) {
                newCatX = (state.catX + CAT_SPEED).coerceAtMost(VIEWPORT_WIDTH - CAT_WIDTH - CAT_SCREEN_PADDING)
            }

            // Update vertical physics
            var newY = state.catY + state.velocityY
            var newVelocityY = state.velocityY + GRAVITY
            var catState = state.catState

            // Determine if falling
            if (newVelocityY > 0 && catState != CatState.FALLING) {
                catState = CatState.FALLING
            }

            // Landing detection
            if (newVelocityY > 0 && state.catY <= DUSTBIN_TOP_Y && newY >= DUSTBIN_TOP_Y) {
                val landingBin = state.dustbins.find { bin ->
                    newCatX + CAT_WIDTH > bin.x && newCatX < bin.x + bin.width
                }
                
                if (landingBin != null) {
                    // Check for hazard collision on landing
                    if (landingBin.hasHazard && landingBin.hazardYOffset > HAZARD_VISIBLE_THRESHOLD) {
                        // Check escape window - only lose life if escape frames exhausted
                        if (landingBin.hazardEscapeFrames >= 10 && landingBin.hazardYOffset >= HAZARD_COLLISION_THRESHOLD) {
                            return@update loseLife(state)
                        } else if (landingBin.hazardYOffset >= HAZARD_COLLISION_THRESHOLD) {
                            return@update loseLife(state)
                        }
                    }
                    
                    // Successful landing
                    newY = DUSTBIN_TOP_Y
                    newVelocityY = 0f
                    
                    // Tutorial step 3: advance when player lands on a dustbin
                    if (state.isTutorial && state.tutorialStep == 3) {
                        return@update state.copy(
                            catY = newY,
                            catX = newCatX,
                            velocityY = newVelocityY,
                            catState = CatState.IDLE,
                            tutorialStep = 4
                        )
                    }
                    
                    if (catState == CatState.JUMPING || catState == CatState.FALLING) {
                        var newStreak = state.streak
                        var jumpScore = POINTS_PER_LANDING
                        var newFoodItems = state.foodItems.toMutableList()
                        var updatedDustbins = state.dustbins.toMutableList()

                        // Streak logic
                        if (lastLandedBinId != landingBin.id) {
                            newStreak++
                            lastLandedBinId = landingBin.id
                            
                            // Bonus points for long streaks
                            if (newStreak >= STREAK_BONUS_THRESHOLD) {
                                jumpScore += BONUS_POINTS
                                SoundManager.playScoreBonus()
                                HapticFeedback.streakBonusFeedback()
                            } else {
                                // Regular landing feedback
                                HapticFeedback.landingFeedback()
                            }
                        }
                        
                        // Play landing sound on every successful landing (Task 6.2)
                        SoundManager.playLandingSound()

                        // Food item spawning on landing (Task 2.1)
                        if (!landingBin.hasHazard && landingBin.hasFood && !landingBin.foodCollected) {
                            // Spawn food item above the dustbin
                            val foodItem = FoodItem(
                                x = landingBin.x + (landingBin.width / 2) - (FOOD_WIDTH / 2),
                                y = DUSTBIN_TOP_Y - FOOD_HEIGHT,
                                velocityY = FOOD_INITIAL_VELOCITY_Y,
                                sourceDustbinId = landingBin.id
                            )
                            newFoodItems.add(foodItem)
                            // Mark food as collected on this dustbin to prevent duplicates
                            val binIndex = updatedDustbins.indexOfFirst { it.id == landingBin.id }
                            if (binIndex >= 0) {
                                updatedDustbins[binIndex] = updatedDustbins[binIndex].copy(foodCollected = true)
                            }
                        }
                        
                        val newScore = state.score + jumpScore
                        val newHighScore = if (newScore > state.highScore) {
                            saveHighScore(newScore)
                            newScore
                        } else state.highScore
                        
                        val levelData = LevelSystem.getLevelData(state.currentLevel)
                        val levelComplete = newScore >= levelData.scoreToNext
                        
                        if (levelComplete) {
                            HapticFeedback.levelCompleteFeedback()
                            SoundManager.playLevelUp()
                        }
                        
                        return@update state.copy(
                            catY = newY,
                            catX = newCatX,
                            velocityY = newVelocityY,
                            catState = CatState.IDLE,
                            score = newScore,
                            highScore = newHighScore,
                            streak = newStreak,
                            gameSpeed = (state.gameSpeed + SPEED_INCREASE_PER_LANDING).coerceAtMost(MAX_GAME_SPEED),
                            showLevelComplete = levelComplete,
                            isGameStarted = !levelComplete,  // Pause game on level complete
                            dustbins = updatedDustbins,
                            foodItems = newFoodItems
                        )
                    }
                    catState = CatState.IDLE
                } else {
                    // Missed bin - fall off
                    return@update loseLife(state.copy(catX = newCatX))
                }
            }

            // Fall off screen -> Lose a life
            if (newY > FALL_OFF_THRESHOLD) {
                return@update loseLife(state.copy(catX = newCatX))
            }

            // Move and animate dustbins, track escape frames
            val isCatOnBin = state.catState == CatState.IDLE && state.catY == DUSTBIN_TOP_Y
            val newDustbins = state.dustbins.map { bin ->
                val hazardOffset = if (bin.hasHazard && bin.x < HAZARD_SPAWN_THRESHOLD_X) {
                    (bin.hazardYOffset + HAZARD_ANIMATION_SPEED).coerceAtMost(1f)
                } else {
                    bin.hazardYOffset
                }
                // Track escape frames for CRAZY_CAT hazards (Task 3.2)
                val escapeFrames = if (bin.hasHazard && bin.hazardType == HazardType.CRAZY_CAT 
                    && hazardOffset > 0f && isCatOnBin
                    && newCatX + CAT_WIDTH > bin.x && newCatX < bin.x + bin.width) {
                    bin.hazardEscapeFrames + 1
                } else {
                    bin.hazardEscapeFrames
                }
                bin.copy(
                    x = bin.x - state.gameSpeed, 
                    hazardYOffset = hazardOffset,
                    hazardEscapeFrames = escapeFrames
                )
            }.filter { it.x + it.width > DUSTBIN_OUT_OF_BOUNDS_LEFT }
             .toMutableList()

            // Hazard proximity warning (Task 3.3)
            newDustbins.forEachIndexed { index, bin ->
                if (bin.hasHazard && bin.hazardType == HazardType.CRAZY_CAT 
                    && bin.hazardYOffset > 0f && !bin.hazardWarned) {
                    val horizontalDistance = kotlin.math.abs((bin.x + bin.width / 2) - (newCatX + CAT_WIDTH / 2))
                    if (horizontalDistance < 300f) {
                        HapticFeedback.hazardWarningFeedback()
                        SoundManager.playHazardWarning()
                        newDustbins[index] = bin.copy(hazardWarned = true)
                    }
                }
            }

            // Spawn new dustbins
            if (newDustbins.isEmpty() || newDustbins.last().x < SPAWN_THRESHOLD_X) {
                val lastX = newDustbins.lastOrNull()?.x ?: 1000f
                val distanceMultiplier = state.gameSpeed / INITIAL_GAME_SPEED
                val distance = (Random.nextFloat() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE) + MIN_SPAWN_DISTANCE) * distanceMultiplier
                
                // Use level-specific hazard probability
                val levelData = LevelSystem.getLevelData(state.currentLevel)
                val scoreMultiplier = (state.score / 50) * 0.02f  // 2% increase per 50 points
                val hazardProbability = (levelData.baseHazardChance + scoreMultiplier).coerceAtMost(0.9f)
                val spawnHazard = Random.nextFloat() < hazardProbability
                val type = if (spawnHazard) {
                    if (Random.nextBoolean()) HazardType.DOG else HazardType.CRAZY_CAT
                } else HazardType.NONE
                
                // Mutual exclusion: food only on non-hazard bins (Task 2.1)
                val hasFood = if (!spawnHazard) {
                    Random.nextFloat() < levelData.foodSpawnChance
                } else false
                
                newDustbins.add(
                    Dustbin(
                        x = lastX + distance,
                        hasHazard = spawnHazard,
                        hazardType = type,
                        hasFood = hasFood
                    )
                )
            }

            // Hazard side collision detection (with escape window - Task 3.2)
            newDustbins.forEach { bin ->
                if (bin.hasHazard && bin.hazardYOffset >= HAZARD_COLLISION_THRESHOLD 
                    && bin.hazardEscapeFrames >= 10) {
                    // Hazard center position with proper width consideration
                    val hazardLeft = bin.x + (bin.width / 2) - (HAZARD_WIDTH / 2)
                    val hazardRight = hazardLeft + HAZARD_WIDTH
                    
                    val catLeft = newCatX
                    val catRight = newCatX + CAT_WIDTH
                    
                    // Check collision
                    if (catRight > hazardLeft && catLeft < hazardRight && state.catY > DUSTBIN_TOP_Y - 100f) {
                        return@update loseLife(state.copy(catX = newCatX))
                    }
                }
            }

            // Update food items physics (Task 2.2)
            var updatedFoodItems = state.foodItems.map { food ->
                food.copy(
                    x = food.x - state.gameSpeed,  // Scroll with world
                    y = food.y + food.velocityY,
                    velocityY = food.velocityY + GRAVITY
                )
            }.filter { food ->
                // Remove food items that fall below threshold or scroll off-screen
                food.y <= FALL_OFF_THRESHOLD && food.x + food.width > DUSTBIN_OUT_OF_BOUNDS_LEFT
            }.toMutableList()

            // Check food collision (Task 2.3)
            var bonusScore = 0
            val catTop = newY - CAT_HEIGHT
            val catBottom = newY
            val catLeft = newCatX
            val catRight = newCatX + CAT_WIDTH
            
            updatedFoodItems = updatedFoodItems.filter { food ->
                val collides = catRight > food.x && catLeft < food.x + food.width &&
                        catBottom > food.y && catTop < food.y + food.height
                if (collides) {
                    bonusScore += FOOD_POINTS
                    SoundManager.playFoodCollected()
                    HapticFeedback.landingFeedback()  // Light haptic for food collection
                }
                !collides  // Keep only non-colliding food items
            }.toMutableList()

            val newScore = state.score + bonusScore
            val newHighScore = if (newScore > state.highScore) {
                saveHighScore(newScore)
                newScore
            } else state.highScore

            state.copy(
                catY = newY,
                catX = newCatX,
                velocityY = newVelocityY,
                catState = catState,
                dustbins = newDustbins,
                foodItems = updatedFoodItems,
                distanceTraveled = state.distanceTraveled + state.gameSpeed,
                score = newScore,
                highScore = newHighScore
            )
        }
    }

    private fun loseLife(state: GameState): GameState {
        // Tutorial mode prevents life loss
        if (state.isTutorial) {
            return state
        }
        
        SoundManager.playDeathSound()
        HapticFeedback.collisionFeedback()
        val newLives = state.lives - 1
        
        return if (newLives <= 0) {
            state.copy(isGameOver = true, catState = CatState.DEAD, lives = 0)
        } else {
            lastLandedBinId = null
            state.copy(
                catY = GROUND_Y,
                catX = VIEWPORT_WIDTH / 2,
                velocityY = 0f,
                catState = CatState.IDLE,
                lives = newLives,
                streak = 0
            )
        }
    }

private fun saveHighScore(score: Int) {
        prefs.edit().putInt(HIGH_SCORE_KEY, score).commit()
    }

    fun jump() {
        if (stateIsActionable()) {
            if (_gameState.value.catState != CatState.JUMPING && _gameState.value.catState != CatState.FALLING) {
                SoundManager.playJumpSound()
                _gameState.update { it.copy(velocityY = JUMP_STRENGTH, catState = CatState.JUMPING) }
            }
        }
    }
    
    /**
     * Checks if the current game state allows player input.
     * Returns false during loading, instructions, game over, or level complete screens.
     */
    private fun stateIsActionable(): Boolean {
        val state = _gameState.value
        return state.isGameStarted && !state.isGameOver && !state.isLoading && !state.showInstructions && !state.showLevelComplete && !state.isPaused
    }

    /**
     * Closes the level complete overlay and resets to home screen.
     */
    fun closeLevelComplete() {
        _gameState.update { it.copy(showLevelComplete = false) }
    }

    /**
     * Resets game to the home/splash screen.
     * Cancels any ongoing game loop.
     */
    fun resetToHome() {
        gameJob?.cancel()
        _gameState.update {
            GameState(highScore = it.highScore, isLoading = false)
        }
    }

    /**
     * Cleanup method called when ViewModel is cleared.
     * Cancels all coroutines to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }
}
