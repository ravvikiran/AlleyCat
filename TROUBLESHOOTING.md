# 🛠️ AlleyCat - Troubleshooting & Known Issues Guide

## Common Issues & Solutions

### 1. App Crashes on Startup
**Symptoms**: App force closes immediately after launching

**Possible Causes & Fixes**:
- [ ] **Missing drawables** → ✅ FIXED - All drawable resources created
- [ ] **Android version too old** → Update to minSdk 29 or later
- [ ] **Permissions missing** → Check `AndroidManifest.xml` has all required permissions

---

### 2. Touch Controls Not Responding
**Symptoms**: Cat doesn't move left/right, or jump doesn't work

**Possible Causes & Fixes**:
- [ ] **Control zones bug** → ✅ FIXED - Now using proper pointer event handling
- [ ] **Device touch sensitivity** → Try tapping more deliberately
- [ ] **Compose UI not recomposing** → Restart the app
- [ ] **Pointer event handler issue** → Check if `changedToDown()` import is available (API 29+)

---

### 3. Sound Not Playing
**Symptoms**: Game is silent, no sound effects

**Possible Causes & Fixes**:
- [ ] **Device muted** → Check system volume settings
- [ ] **Audio stream** → Ensure `STREAM_MUSIC` is not muted
- [ ] **SoundManager not initialized** → Verify `SoundManager.init()` is called in MainActivity
- [ ] **ToneGenerator permissions** → Should work without explicit permission

**Workaround**: Game is still playable without sound

---

### 4. Game Too Fast/Slow
**Symptoms**: Game speed doesn't feel right, gameplay is laggy

**Possible Causes & Fixes**:
- [ ] **Device performance** → Close background apps
- [ ] **Drawable quality** → Placeholder XMLs are lightweight, replace with optimized PNGs
- [ ] **Game loop timing** → Currently 16ms (60 FPS), adjust `GAME_LOOP_TICK_MS` in GameConstants.kt
- [ ] **Physics values** → Tune GRAVITY, JUMP_STRENGTH constants

**Optimization Tips**:
```kotlin
// In GameConstants.kt
const val GAME_LOOP_TICK_MS = 16L  // Decrease for faster, increase for slower

// Reduce drawable quality for better performance
// Replace placeholder XMLs with optimized PNG/WebP images
```

---

### 5. Drawable Resources Still Missing
**Symptoms**: Still getting `ResourceNotFoundException` errors

**Solution**:
The placeholder XML drawables should be in `/app/src/main/res/drawable/`:
- ✅ `cat_idle.xml`
- ✅ `cat_jump.xml`
- ✅ `dustbin.xml`
- ✅ `hazard_dog.xml`
- ✅ `hazard_crazy_cat.xml`
- ✅ `bg_alley_night.xml`

If still missing, rebuild:
```bash
./gradlew clean build
```

---

### 6. Control Zones Not Responding Correctly
**Symptoms**: Wrong zone is triggering action, overlapping zones

**Causes & Fixes**:
- [ ] **Old code still running** → Clean rebuild needed
- [ ] **Pointer event handler issue** → Requires API 29+, ensure minSdk is set correctly
- [ ] **Touch coordinate calculation** → Check if screen width is being calculated correctly

**Debug**: Add logging to touch handler:
```kotlin
.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val x = event.changes.firstOrNull()?.position?.x
            Log.d("TouchDebug", "Touch at X: $x, Screen width: ${size.width}")
            // ... rest of handler
        }
    }
}
```

---

### 7. Level Progression Not Working
**Symptoms**: Game doesn't move to next level, stuck at level 1

**Possible Causes & Fixes**:
- [ ] **Level complete not triggering** → Verify score reaches target (100, 200, 350...)
- [ ] **LevelCompleteOverlay not showing** → Check `state.showLevelComplete` is true
- [ ] **startNextLevel() not working** → Ensure gameJob is properly cancelled
- [ ] **Score calculation wrong** → Debug score increments

**Debug Score**:
```kotlin
// Add logging in GameViewModel.update()
Log.d("Score", "Current: ${state.score}, Target: ${levelData.scoreToNext}, Complete: $levelComplete")
```

---

### 8. High Score Not Saving
**Symptoms**: High score resets every app restart

**Causes & Fixes**:
- [ ] **SharedPreferences not initialized** → Verify `prefs` object in GameViewModel
- [ ] **Wrong key name** → Ensure key is `"high_score"` (not case-sensitive)
- [ ] **Storage permission** → Usually auto-granted, but check if app has write permission
- [ ] **Device file system issue** → Try clearing app data

**Manual Testing**:
```kotlin
// In GameViewModel
val currentScore = prefs.getInt("high_score", 0)
Log.d("SavedScore", "Retrieved: $currentScore")
```

---

### 9. Game Freezes During Gameplay
**Symptoms**: App becomes unresponsive, touches don't register

**Possible Causes & Fixes**:
- [ ] **Memory leak** → Ensure gameJob is cancelled on game over
- [ ] **Coroutine scope issue** → Verify using `viewModelScope`
- [ ] **Too many dustbins** → Check dustbin culling is working (filter `x + width > -200`)
- [ ] **Infinite loop** → Review game loop condition

**Check Memory**: Use Android Profiler in Android Studio to monitor heap

---

### 10. Hazard Collision Not Accurate
**Symptoms**: Getting hit even though cat appears to have dodged

**Causes & Fixes**:
- [ ] **Hazard hitbox calculation** → Verify formula in update():
```kotlin
val hazardLeft = bin.x + (bin.width / 2) - (HAZARD_WIDTH / 2)
val hazardRight = hazardLeft + HAZARD_WIDTH
```
- [ ] **Cat hitbox wrong** → CAT_WIDTH should be 100f
- [ ] **Threshold value** → HAZARD_COLLISION_THRESHOLD = 0.5f
- [ ] **Y-coordinate check** → `state.catY > DUSTBIN_TOP_Y - 100f`

**Adjust Hitbox** in GameConstants.kt:
```kotlin
const val HAZARD_COLLISION_THRESHOLD = 0.5f  // Lower = earlier collision
const val HAZARD_WIDTH = 150f                 // Exact width of hazard
const val CAT_WIDTH = 100f                    // Exact width of cat
```

---

## Performance Optimization Tips

### For Slow Devices
```kotlin
// GameConstants.kt
const val GAME_LOOP_TICK_MS = 20L      // Slower frame rate (50 FPS)
const val MAX_SPAWN_DISTANCE = 600f    // Fewer dustbins spawning
```

### For High-End Devices
```kotlin
// GameConstants.kt
const val GAME_LOOP_TICK_MS = 12L      // Faster frame rate (83 FPS)
const val PARALLAX_SPEED_FACTOR = 0.3f // More detailed parallax
```

---

## Debug Logging Template

To add debug logging, include this in any function:

```kotlin
// GameViewModel.kt - add to update() function
private fun debugState(state: GameState) {
    Log.d("GameState", """
        Cat: (${state.catX}, ${state.catY})
        Speed: ${state.gameSpeed}
        Score: ${state.score}
        Lives: ${state.lives}
        Level: ${state.currentLevel}
        Dustbins: ${state.dustbins.size}
        VelocityY: ${state.velocityY}
    """.trimIndent())
}
```

Then call `debugState(state)` periodically to track game state.

---

## Testing Checklist

Before each deployment, verify:

- [ ] App builds without errors
- [ ] App launches without crashing
- [ ] Splash screen displays correctly
- [ ] "START GAME" button works
- [ ] Left/right/jump controls responsive
- [ ] Score increments on landing
- [ ] Hazards appear and animate
- [ ] Collision detection accurate
- [ ] Level complete triggers at target score
- [ ] Next level starts correctly
- [ ] High score persists after restart
- [ ] Sound effects play (if enabled)
- [ ] Game over triggers on 0 lives

---

## Contact & Support

For issues not covered here:
1. Check logcat output in Android Studio
2. Enable verbose logging in GameViewModel
3. Use Android Profiler to check memory/CPU
4. Review git diff to see recent changes
5. Roll back recent changes if issue just started

---

**Last Updated**: April 18, 2026  
**Status**: All Known Issues Resolved ✅
