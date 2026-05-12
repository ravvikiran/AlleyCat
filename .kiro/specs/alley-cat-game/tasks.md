# Implementation Plan: Alley Cat Game

## Overview

All core implementation tasks are complete. The Alley Cat game now fully implements all 16 requirements including: food item collection system, rival cat escape mechanics, tutorial system with action-based advancement and skip functionality, auto-pause on app background, landing sound on all landings, Level 4 completion with mystery level unlock, and Kotest property-based testing infrastructure with custom generators. The remaining tasks are optional property-based tests (marked with `*`) and checkpoints.

## Tasks

- [x] 1. Extend data models and constants for food items and rival cat mechanics
  - [x] 1.1 Add FoodItem data class and extend Dustbin/GameState models
    - Add `FoodItem` data class with id, x, y, width, height, velocityY, isCollected, sourceDustbinId fields
    - Add `FoodType` enum (FISH, MILK, CHEESE) for visual variety
    - Extend `Dustbin` with `hasFood: Boolean` and `foodCollected: Boolean` fields
    - Extend `GameState` with `foodItems: List<FoodItem>` field
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 5.9_

  - [x] 1.2 Add food-related constants to GameConstants.kt
    - Add `FOOD_WIDTH = 60f`, `FOOD_HEIGHT = 60f`, `FOOD_INITIAL_VELOCITY_Y = -15f`
    - Add `FOOD_POINTS = 5` for bonus score on collection
    - Add `FOOD_SPAWN_DURATION_MS` range (300-500ms) for upward animation
    - Add level-specific `foodSpawnChance` values to `LevelData`
    - _Requirements: 4.2, 4.3, 4.5_

  - [x] 1.3 Add foodSpawnChance to LevelData and LevelSystem
    - Extend `LevelData` with `foodSpawnChance: Float` field
    - Update all level definitions in `LevelSystem.getLevelData()` with food spawn probabilities (0.3 at L1, 0.25 at L2, 0.2 at L3, 0.15 at L4)
    - Update mystery level generation to include foodSpawnChance of 0.1
    - _Requirements: 4.5, 9.4_

- [x] 2. Implement food item spawning, physics, and collection logic
  - [x] 2.1 Implement food item spawning on dustbin landing
    - In `GameViewModel.update()`, when a successful landing occurs on a non-hazard dustbin with `hasFood=true` and `foodCollected=false`, spawn a `FoodItem` above the dustbin
    - Set `foodCollected=true` on the dustbin after spawning to prevent duplicates
    - Assign food to non-hazard dustbins during dustbin spawning using level-specific `foodSpawnChance`
    - Ensure mutual exclusion: dustbins with hazards never get food assigned
    - _Requirements: 4.1, 4.5, 4.6, 5.9_

  - [x] 2.2 Implement food item physics and lifecycle
    - Add `updateFoodItems()` method to GameViewModel that applies gravity to each FoodItem each frame
    - Food items start with upward velocity (`FOOD_INITIAL_VELOCITY_Y`) and arc back down under gravity
    - Remove food items that fall below `FALL_OFF_THRESHOLD`
    - Remove food items that scroll off-screen to the left
    - _Requirements: 4.2, 4.4_

  - [x] 2.3 Implement food item collision detection and scoring
    - Add `checkFoodCollision()` method that checks Player_Cat bounding box overlap with each FoodItem
    - On collision: award 5 bonus points, remove the FoodItem from the list, play collection sound and haptic
    - Add `playFoodCollected()` to SoundManager for reward chime
    - _Requirements: 4.3_

  - [ ]* 2.4 Write property tests for food item system (Properties 14-19)
    - **Property 14: Mutual exclusion of hazard and food on same dustbin**
    - **Property 15: Food spawns only on non-hazard bins with available food**
    - **Property 16: Food items follow gravity physics**
    - **Property 17: Food collection awards bonus points and removes item**
    - **Property 18: Food items are removed when off-screen**
    - **Property 19: No duplicate food from same dustbin**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 5.9**

- [x] 3. Implement rival cat escape mechanics
  - [x] 3.1 Add visual warning indicator for emerging rival cats
    - When a `CRAZY_CAT` hazard begins emerging (hazardYOffset transitions from 0.0), display a warning indicator on the dustbin
    - Warning remains visible until hazard reaches full emergence
    - Render warning as a pulsing exclamation or color highlight on the dustbin in GameCanvas
    - _Requirements: 6.1_

  - [x] 3.2 Implement escape window and delayed collision activation
    - While rival cat hazardYOffset < 0.5 (HAZARD_COLLISION_THRESHOLD), allow the player to jump away without life loss
    - When player is standing on a dustbin and its rival cat begins emerging, ensure at least 10 frames before collision activates
    - Add frame counter tracking for escape window per hazard
    - _Requirements: 6.2, 6.3, 6.5_

  - [x] 3.3 Add hazard proximity warning haptic feedback
    - In the update loop, detect when a CRAZY_CAT hazard is within 300 logical units horizontally of the Player_Cat and has begun emerging
    - Trigger `hazardWarningFeedback()` double-pulse pattern (50ms + 50ms gap + 50ms) via HapticFeedback
    - Add `playHazardWarning()` sound effect to SoundManager
    - Ensure warning only fires once per hazard emergence (track warned state)
    - _Requirements: 6.4_

  - [ ]* 3.4 Write property test for hazard collision threshold (Property 11)
    - **Property 11: Hazard collision only activates at or above threshold**
    - **Validates: Requirements 5.2, 6.2, 6.3**

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Refine tutorial system implementation
  - [x] 5.1 Implement 3-step interactive tutorial flow
    - Step 1 (Movement): Display "Hold left/right side to move" with highlighted touch zones; advance when player moves left AND right
    - Step 2 (Jump): Display "Tap center to jump" with highlighted center zone; advance when player performs a jump
    - Step 3 (Landing): Display "Land on a dustbin!" with arrow indicator; advance when player lands on a dustbin
    - Ensure tutorial prevents life loss (already partially implemented with lives=99, formalize with isTutorial check)
    - _Requirements: 11.2, 11.3, 11.4_

  - [x] 5.2 Implement tutorial persistence and skip functionality
    - On tutorial completion, persist `tutorial_completed = true` to SharedPreferences
    - Hide tutorial button on splash screen when `tutorialCompleted = true`
    - Add skip/back button during tutorial that exits to splash without marking complete
    - _Requirements: 11.1, 11.5, 11.6, 11.7_

  - [ ]* 5.3 Write property test for tutorial mode (Property 28)
    - **Property 28: Tutorial mode prevents life loss**
    - **Validates: Requirements 11.4**

- [x] 6. Align existing systems with requirements
  - [x] 6.1 Fix level progression score thresholds and Level 4 completion
    - Update Level 4 `scoreToNext` from `Int.MAX_VALUE` to 350 (cumulative during that level)
    - Implement mystery level unlock after Level 4 completion with speed=18, hazardChance=0.70, +1 speed per mystery level
    - Ensure level complete overlay shows completed level name and next level name
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.6_

  - [x] 6.2 Fix landing sound effect trigger
    - Add `SoundManager.playLandingSound()` call on every successful landing (currently only streak bonus plays sound)
    - Ensure landing sound plays for regular landings (non-streak) in addition to haptic feedback
    - _Requirements: 3.5, 14.1_

  - [x] 6.3 Ensure pause auto-triggers on app background
    - In `MainActivity`, add lifecycle observer that calls `viewModel.togglePause()` when app moves to background (onPause/onStop)
    - Verify pause overlay shows resume and menu options
    - Verify menu option persists high score before returning to splash
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [x] 6.4 Ensure high score persistence handles edge cases
    - Verify high score updates in real-time during gameplay (not just on game over)
    - Add fallback to 0 if SharedPreferences read fails
    - Ensure Game Over screen displays both current score and all-time high score
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

- [ ] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Set up Kotest property testing infrastructure
  - [x] 8.1 Add Kotest dependencies and configure test runner
    - Add `kotest-runner-junit5:5.8.0`, `kotest-assertions-core:5.8.0`, `kotest-property:5.8.0` to build.gradle.kts testImplementation
    - Add `mockk:1.13.8` for mocking
    - Configure JUnit5 test task in build.gradle.kts with `useJUnitPlatform()`
    - Create test directory structure: `app/src/test/java/com/example/alleycat/properties/` and `generators/`
    - _Requirements: All (testing infrastructure)_

  - [x] 8.2 Create custom Kotest generators for game state
    - Create `GameStateArb.kt` with `Arb.gameState()` generating valid GameState instances with random positions, velocities, scores within bounds
    - Create `DustbinArb.kt` with `Arb.dustbin()` generating dustbins with random positions, hazard configurations respecting mutual exclusion
    - Create `FoodItemArb.kt` with `Arb.foodItem()` generating food items at various lifecycle stages
    - Add `Arb.catPosition()` for valid (catX, catY) pairs within screen bounds
    - _Requirements: All (testing infrastructure)_

- [ ] 9. Implement movement and input property tests
  - [ ]* 9.1 Write property test for horizontal movement bounds (Property 1)
    - **Property 1: Horizontal movement is bounded and constant-rate**
    - **Validates: Requirements 1.1, 1.2, 1.3, 1.4**

  - [ ]* 9.2 Write property test for input ignored when not actionable (Property 2)
    - **Property 2: Inputs are ignored when game is not actionable**
    - **Validates: Requirements 1.5, 12.6**

- [ ] 10. Implement jump and physics property tests
  - [ ]* 10.1 Write property test for jump from IDLE only (Property 3)
    - **Property 3: Jump only applies from IDLE state**
    - **Validates: Requirements 2.1, 2.3, 2.5**

  - [ ]* 10.2 Write property test for gravity application (Property 4)
    - **Property 4: Gravity is applied every frame while airborne**
    - **Validates: Requirements 2.2**

  - [ ]* 10.3 Write property test for JUMPING to FALLING transition (Property 5)
    - **Property 5: Cat transitions from JUMPING to FALLING when velocity becomes positive**
    - **Validates: Requirements 2.4**

- [ ] 11. Implement landing and scoring property tests
  - [ ]* 11.1 Write property test for successful landing (Property 6)
    - **Property 6: Successful landing awards exactly base points and sets IDLE**
    - **Validates: Requirements 3.1, 3.2**

  - [ ]* 11.2 Write property test for streak increment (Property 7)
    - **Property 7: Streak increments on landing on a distinct dustbin**
    - **Validates: Requirements 3.3, 10.1**

  - [ ]* 11.3 Write property test for life loss reset (Property 8)
    - **Property 8: Life loss resets cat position, velocity, and streak**
    - **Validates: Requirements 3.4, 8.2, 8.3, 10.5**

  - [ ]* 11.4 Write property test for game over trigger (Property 9)
    - **Property 9: Zero lives triggers game over**
    - **Validates: Requirements 8.4**

  - [ ]* 11.5 Write property test for hazard landing (Property 10)
    - **Property 10: Landing on a hazard bin with visible hazard loses a life**
    - **Validates: Requirements 3.7**

- [ ] 12. Implement hazard and world property tests
  - [ ]* 12.1 Write property test for hazard emergence rate (Property 12)
    - **Property 12: Hazard emergence animates at constant rate**
    - **Validates: Requirements 5.1**

  - [ ]* 12.2 Write property test for hazard type validity (Property 13)
    - **Property 13: Hazard type is always DOG or CRAZY_CAT when hasHazard is true**
    - **Validates: Requirements 5.8**

  - [ ]* 12.3 Write property test for dustbin scrolling (Property 20)
    - **Property 20: Dustbins scroll leftward at game speed**
    - **Validates: Requirements 7.1**

  - [ ]* 12.4 Write property test for off-screen dustbin removal (Property 21)
    - **Property 21: Off-screen dustbins are removed**
    - **Validates: Requirements 7.3**

  - [ ]* 12.5 Write property test for spawn distance bounds (Property 22)
    - **Property 22: Spawned dustbin gap is within bounds**
    - **Validates: Requirements 7.2, 7.4**

  - [ ]* 12.6 Write property test for speed increase cap (Property 23)
    - **Property 23: Game speed increases on landing up to maximum**
    - **Validates: Requirements 7.5**

- [ ] 13. Implement progression and game flow property tests
  - [ ]* 13.1 Write property test for level complete trigger (Property 24)
    - **Property 24: Level complete triggers at score threshold**
    - **Validates: Requirements 9.1**

  - [ ]* 13.2 Write property test for streak bonus (Property 25)
    - **Property 25: Streak bonus awards extra points at threshold**
    - **Validates: Requirements 10.2**

  - [ ]* 13.3 Write property test for pause freeze (Property 26)
    - **Property 26: Pause freezes all game state**
    - **Validates: Requirements 12.1, 12.3**

  - [ ]* 13.4 Write property test for high score update (Property 27)
    - **Property 27: High score updates when current score exceeds it**
    - **Validates: Requirements 16.3**

  - [ ]* 13.5 Write property test for lives reset on new level (Property 29)
    - **Property 29: Lives reset to 3 on new level**
    - **Validates: Requirements 9.5**

- [x] 14. Wire food items into rendering and UI
  - [x] 14.1 Render food items in GameCanvas
    - Draw food items (fish/milk/cheese sprites) at their logical positions with canvas scaling
    - Animate food items with a slight rotation or glow during upward arc
    - Show collection particle effect when food is collected
    - _Requirements: 4.2, 4.3, 13.4_

  - [x] 14.2 Render rival cat warning indicators in GameCanvas
    - Draw pulsing warning icon (exclamation mark or red glow) on dustbins with emerging CRAZY_CAT hazards
    - Animate warning indicator opacity/scale to draw player attention
    - _Requirements: 6.1_

  - [x] 14.3 Render tutorial overlays and highlighted zones
    - Draw semi-transparent highlight over the active touch zone for current tutorial step
    - Display instructional text centered above the game area
    - Show step progress indicator (1/3, 2/3, 3/3)
    - Add skip button in tutorial overlay
    - _Requirements: 11.2, 11.3, 11.7_

- [x] 15. Final integration and wiring
  - [x] 15.1 Integrate food item updates into main game loop
    - Call `updateFoodItems()` and `checkFoodCollision()` within the `update()` method
    - Ensure food items scroll with the world (x position decreases by gameSpeed each frame)
    - Wire food spawning into dustbin generation logic
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [x] 15.2 Integrate rival cat escape mechanics into game loop
    - Wire hazard warning detection into update loop
    - Ensure escape window frame counter is tracked and checked before collision activation
    - Wire haptic and sound warnings for nearby emerging rival cats
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [x] 15.3 Wire tutorial step advancement into input handlers
    - In `moveLeft()`/`moveRight()`: if tutorial step 1, track movement and advance when both directions used
    - In `jump()`: if tutorial step 2, advance to step 3
    - In landing logic: if tutorial step 3, advance to step 4 (complete)
    - _Requirements: 11.2_

  - [ ]* 15.4 Write integration tests for food and rival cat systems
    - Test food spawning end-to-end: land on food bin → food spawns → collect → score increases
    - Test rival cat escape: cat on bin → hazard emerges → jump away before threshold → no life loss
    - Test tutorial flow: start → move → jump → land → complete → persisted
    - _Requirements: 4.1, 4.3, 6.2, 6.5, 11.2_

- [ ] 16. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster MVP
- All core implementation tasks (1-3, 5-6, 8, 14-15) are COMPLETE ✅
- Testing infrastructure (Kotest + generators) is ready — optional property tests can be written at any time
- Each task references specific requirements for traceability
- Checkpoints (4, 7, 16) are for manual verification
- The codebase now fully implements all 16 requirements:
  - Req 1-3: Movement, jumping, landing (with landing sound fix)
  - Req 4: Food item collection (spawn, physics, collision, scoring)
  - Req 5: Hazard system (emergence, collision, types, mutual exclusion)
  - Req 6: Rival cat escape (warning indicator, escape window, proximity haptic)
  - Req 7: Scrolling world and dustbin spawning
  - Req 8: Lives and game over
  - Req 9: Level progression (Level 4 completion, mystery levels)
  - Req 10: Streak and scoring bonuses
  - Req 11: Tutorial system (3-step action-based, skip, persistence)
  - Req 12: Pause and resume (auto-pause on background)
  - Req 13: Visual presentation (parallax, food items, warning indicators)
  - Req 14: Audio feedback (all sounds including food collection)
  - Req 15: Haptic feedback (all patterns including hazard warning)
  - Req 16: High score persistence (with edge case handling)

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["1.3", "8.1"] },
    { "id": 2, "tasks": ["2.1", "3.1", "8.2"] },
    { "id": 3, "tasks": ["2.2", "3.2", "5.1"] },
    { "id": 4, "tasks": ["2.3", "3.3", "5.2"] },
    { "id": 5, "tasks": ["2.4", "3.4", "5.3"] },
    { "id": 6, "tasks": ["6.1", "6.2", "6.3", "6.4"] },
    { "id": 7, "tasks": ["9.1", "9.2", "10.1", "10.2", "10.3"] },
    { "id": 8, "tasks": ["11.1", "11.2", "11.3", "11.4", "11.5"] },
    { "id": 9, "tasks": ["12.1", "12.2", "12.3", "12.4", "12.5", "12.6"] },
    { "id": 10, "tasks": ["13.1", "13.2", "13.3", "13.4", "13.5"] },
    { "id": 11, "tasks": ["14.1", "14.2", "14.3"] },
    { "id": 12, "tasks": ["15.1", "15.2", "15.3"] },
    { "id": 13, "tasks": ["15.4"] }
  ]
}
```
