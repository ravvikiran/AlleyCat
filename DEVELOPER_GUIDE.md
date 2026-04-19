# Developer Guide - AlleyCat Game Architecture

## Overview

AlleyCat is built with a clean MVVM architecture using Kotlin and Jetpack Compose. This guide explains the codebase structure and how to extend it.

## Architecture Layers

### 1. Data Layer (`GameModels.kt`)

**Responsibility**: Define immutable data classes representing game state

```kotlin
data class GameState(
    val catY: Float,
    val catX: Float,
    val catState: CatState,  // IDLE, JUMPING, FALLING, DEAD
    val score: Int,
    val lives: Int,
    val currentLevel: Int,
    // ... more properties
)

data class Dustbin(
    val id: String,
    val x: Float,
    val width: Float,
    val height: Float,
    val hasHazard: Boolean,
    val hazardType: HazardType,  // DOG, CRAZY_CAT, NONE
    val hazardYOffset: Float  // 0f = hidden, 1f = fully visible
)
```

**Key Points**:
- All state immutable (use `copy()` for updates)
- UUIDs for unique bin identification
- Y-offset for hazard animation

### 2. Business Logic Layer (`GameViewModel.kt`)

**Responsibility**: Game loop, physics, collision detection, state mutations

#### Game Loop Structure
```kotlin
private fun gameLoop() {
    // Runs at ~60 FPS (16ms ticks)
    while (isGameStarted && !isGameOver) {
        update()  // Single frame logic
        delay(16)  // Next frame
    }
}
```

#### Update Function Flow
1. **Cat Movement** - Update X position based on left/right input
2. **Physics** - Apply gravity to Y velocity
3. **Landing Detection** - Check if cat intersects dustbin top
4. **Hazard Collision** - Check side collisions with hazards
5. **Dustbin Management** - Move, filter, spawn new bins
6. **State Persistence** - Save high score to SharedPreferences

#### Collision Detection

**Landing Detection**:
```kotlin
if (newVelocityY > 0 && state.catY <= DUSTBIN_TOP_Y && newY >= DUSTBIN_TOP_Y) {
    val landingBin = state.dustbins.find { bin ->
        newCatX + CAT_WIDTH > bin.x && newCatX < bin.x + bin.width
    }
    // ... handle landing or fall off
}
```

**Hazard Collision** (FIXED):
```kotlin
// Hazard is 150px wide, centered on bin
val hazardLeft = bin.x + (bin.width / 2) - (HAZARD_WIDTH / 2)
val hazardRight = hazardLeft + HAZARD_WIDTH

if (catRight > hazardLeft && catLeft < hazardRight) {
    // Collision!
}
```

### 3. Configuration Layer (`GameConstants.kt`)

**Responsibility**: Centralized game tuning values

```kotlin
// Physics
const val GRAVITY = 1.3f
const val JUMP_STRENGTH = -28f

// Difficulty
const val INITIAL_GAME_SPEED = 10f
const val MAX_GAME_SPEED = 25f
const val SPEED_INCREASE_PER_LANDING = 0.1f

// Level thresholds
const val INITIAL_LIVES = 3
const val TOTAL_LEVELS = 4
```

**Why Centralize?**
- Easy difficulty balancing
- One place to adjust gameplay feel
- No "magic numbers" scattered in logic

### 4. Level System (`LevelSystem.kt`)

**Responsibility**: Level-specific data and progression

```kotlin
data class LevelData(
    val level: Int,
    val name: String,
    val startingSpeed: Float,
    val maxSpeed: Float,
    val baseHazardChance: Float,
    val scoreToNext: Int,
    val lives: Int
)
```

**Usage**:
```kotlin
val levelData = LevelSystem.getLevelData(currentLevel)
val hazardProbability = levelData.baseHazardChance + scoreMultiplier
```

### 5. Audio Layer (`SoundManager.kt`)

**Responsibility**: Game sound effects management

```kotlin
object SoundManager {
    fun playJumpSound()
    fun playDeathSound()
    fun playScoreBonus()
    fun playHazardWarning()
    fun playLandingSound()
    fun playLevelUp()
}
```

**Current Implementation**: ToneGenerator (built-in Android)
**Future**: Can be extended to use MediaPlayer for MP3/WAV samples

### 6. Presentation Layer (`MainActivity.kt`)

**Responsibility**: UI rendering with Jetpack Compose

#### Composables Hierarchy
```
GameScreen()
├── LoadingScreen()
├── GameCanvas()  // Canvas-based game rendering
├── GameOverlay (Control Zones)
├── ScoreHud()
├── SplashScreen()
├── InstructionsOverlay()
├── LevelCompleteOverlay()
└── GameOverOverlay()
```

#### Canvas Rendering
```kotlin
Canvas(modifier = Modifier.fillMaxSize()) {
    val canvasScale = size.height / LOGICAL_HEIGHT
    
    // Parallax background
    // Dustbins with glow
    // Hazards with animation
    // Player cat
}
```

## Key Design Decisions

### 1. Immutable State with Copy
```kotlin
_gameState.update { state ->
    state.copy(
        score = newScore,
        catX = newCatX,
        // ... only changed fields
    )
}
```
✅ Easier to track changes  
✅ No side effects  
✅ Better for time-travel debugging

### 2. Canvas for Game Rendering
```kotlin
// Instead of animating Views/Images individually
Canvas { drawImage(), drawCircle(), drawLine() }
```
✅ High performance  
✅ Pixel-perfect control  
✅ Easy parallax effects

### 3. StateFlow for Reactive Updates
```kotlin
val gameState = _gameState.asStateFlow()
val state by gameState.collectAsState()  // Recompose on change
```
✅ Reactive updates  
✅ Lifecycle-aware  
✅ Compose-first design

### 4. Gesture Detection for Input
```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth(0.3f)
        .pointerInput(Unit) {
            detectTapGestures {
                viewModel.moveLeft()
            }
        }
)
```
✅ Fine-grained control  
✅ Multiple simultaneous inputs  
✅ Natural mobile feel

## Extending the Game

### Adding a New Level

1. Update `GameConstants.kt`:
```kotlin
const val TOTAL_LEVELS = 5  // was 4
```

2. Add level data to `LevelSystem.kt`:
```kotlin
5 -> LevelData(
    level = 5,
    name = "NIGHTMARE ALLEY",
    startingSpeed = 18f,
    maxSpeed = 26f,
    baseHazardChance = 0.8f,
    scoreToNext = Int.MAX_VALUE,
    lives = 3
)
```

### Adding a New Hazard Type

1. Update `GameModels.kt`:
```kotlin
enum class HazardType {
    NONE, DOG, CRAZY_CAT, BIRD  // Add BIRD
}
```

2. Update `GameViewModel.kt` spawn logic:
```kotlin
val type = when (Random.nextInt(3)) {
    0 -> HazardType.DOG
    1 -> HazardType.CRAZY_CAT
    else -> HazardType.BIRD
}
```

3. Update `MainActivity.kt` rendering:
```kotlin
val hazardImg = when (bin.hazardType) {
    HazardType.DOG -> hazardDogImg
    HazardType.CRAZY_CAT -> hazardCatImg
    HazardType.BIRD -> hazardBirdImg
    else -> hazardDogImg
}
```

### Adding Power-ups

1. Add to GameState:
```kotlin
data class GameState(
    val activePowerUps: List<PowerUp> = emptyList(),
    // ...
)

data class PowerUp(
    val id: String,
    val x: Float,
    val type: PowerUpType,  // SHIELD, DOUBLE_POINTS, SLOW_TIME
    val activationTime: Long
)
```

2. Add spawn/collision logic in `update()`:
```kotlin
if (catCollidesWith(powerUp)) {
    activatePowerUp(powerUp)
}
```

### Adding Sound

Current: Tone-based sounds
Future: MediaPlayer or ExoPlayer for music/samples

```kotlin
// Add to SoundManager
private var mediaPlayer: MediaPlayer? = null

fun playBackgroundMusic(resourceId: Int) {
    mediaPlayer = MediaPlayer.create(context, resourceId)
    mediaPlayer?.start()
}
```

## Performance Considerations

### Optimization Tips

1. **Dustbin Culling** ✅
```kotlin
.filter { it.x + it.width > DUSTBIN_OUT_OF_BOUNDS_LEFT }
// Removes off-screen bins from memory
```

2. **Avoid Allocations**
- Reuse dustbin list instead of creating new
- Use `update { }` instead of creating new state repeatedly

3. **Canvas Efficiency**
- Draw in order: background, bins, hazards, cat
- Minimal drawing operations per frame

4. **Coroutine Scope**
```kotlin
gameJob = viewModelScope.launch { }  // Tied to lifecycle
// Automatically cancelled when ViewModel destroyed
```

## Testing

### Unit Test Ideas

```kotlin
// Test collision detection
fun testHazardCollision() {
    // Create test hazard and cat positions
    // Assert collision detected correctly
}

// Test scoring logic
fun testStreakBonus() {
    // 5+ consecutive landings
    // Assert bonus points awarded
}

// Test level progression
fun testLevelCompletion() {
    // Simulate reaching score threshold
    // Assert level complete state triggered
}
```

## Debugging

### Common Issues

**Cat not moving left/right?**
- Check `movingLeft`/`movingRight` state
- Verify touch zones in GameScreen composable
- Check `CAT_SPEED` constant

**Collisions feel wrong?**
- Verify hazard hitbox calculation
- Check `HAZARD_COLLISION_THRESHOLD` value
- Log hazardYOffset during development

**Game too slow?**
- Check `delay(GAME_LOOP_TICK_MS)` value
- Profile with Android Profiler
- Reduce drawable complexity

## Useful Debugging Tips

Add to GameViewModel:
```kotlin
fun debugPrint(state: GameState) {
    Log.d("AlleyCat", """
        Cat: (${state.catX}, ${state.catY})
        Vel: ${state.velocityY}
        Score: ${state.score}
        Lives: ${state.lives}
        Level: ${state.currentLevel}
    """.trimIndent())
}
```

## Future Architecture Improvements

1. **Game States Pattern** - Separate GameOverState, PlayingState, LevelCompleteState
2. **Event System** - Decouple score/sound events
3. **Resource Manager** - Preload images/sounds
4. **Analytics** - Track player progression
5. **Networking** - Online leaderboards
6. **Plugins** - Custom level packs

## Resources

- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Canvas Drawing](https://developer.android.com/reference/kotlin/androidx/compose/foundation/Canvas)
- [Android Input Handling](https://developer.android.com/guide/topics/ui/gestures/index)

---

Happy coding! 🐱🎮
