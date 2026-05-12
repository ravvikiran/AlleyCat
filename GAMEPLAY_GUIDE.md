# AlleyCat - Android Retro Arcade Game

A modern recreation of the classic 1990s arcade game "Alley Cat" for Android, with enhanced gameplay mechanics and multiple difficulty levels.

## 🎮 Gameplay Overview

You control a nimble cat navigating through dangerous alleys by jumping on trash cans and dodging hazards. The goal is to score as many points as possible while avoiding falling off the screen and being caught by enemies.

## 🕹️ Controls

### Touch Controls
- **LEFT ZONE** (Left 30% of screen): Tap and hold to move the cat LEFT
- **CENTER ZONE** (Center 40% of screen): Tap to JUMP
- **RIGHT ZONE** (Right 30% of screen): Tap and hold to move the cat RIGHT

The cat stays in place horizontally while the alley scrolls past. Use left/right movement to position yourself to land on the next dustbin.

## 📊 Game Mechanics

### Scoring
- **+1 point** for each successful landing
- **+1 bonus point** for landing on 5+ consecutive different bins (streak bonus)
- **+5 bonus points** for collecting a food item
- Score carries across level transitions

### Food Collection
- When the cat lands on a non-hazard dustbin, food may spawn upward
- Food types: **Fish** (blue), **Milk** (white), **Cheese** (yellow)
- Food arcs upward then falls under gravity - position yourself to catch it
- Each food item collected awards **5 bonus points**
- Dustbins never have both food AND hazards (mutual exclusion)
- Food spawn chance decreases per level:
  - Level 1: 30%
  - Level 2: 25%
  - Level 3: 20%
  - Level 4: 15%
  - Mystery levels: 10%

### Lives System
- Start with **3 lives** per level
- Lose a life if:
  - You fall off the screen (miss a dustbin)
  - You collide with a hazard (dog or crazy cat)
- Game Over when all lives are depleted

### Streak System
- Track consecutive landings on different bins
- Achieve 5+ streak for bonus points and visual celebration (🔥)
- Streak resets when you lose a life

### Difficulty Progression

#### Level 1: Alley Basics
- Starting Speed: 10
- Hazard Chance: 20%
- Target Score: 100 points
- Food Spawn Chance: 30%
- Description: Learn to jump and land on bins

#### Level 2: Hazard Alley
- Starting Speed: 12
- Hazard Chance: 35%
- Target Score: 200 points
- Food Spawn Chance: 25%
- Description: More dangers appear! Avoid those hazards!

#### Level 3: Chaos Corner
- Starting Speed: 14
- Hazard Chance: 50%
- Target Score: 350 points
- Food Spawn Chance: 20%
- Description: Speed increases and hazards are everywhere!

#### Level 4: Final Gauntlet
- Starting Speed: 16
- Hazard Chance: 65%
- Target Score: 350 points
- Food Spawn Chance: 15%
- Description: The ultimate test of your skills!

#### Mystery Levels (Level 5+)
- Starting Speed: 18+ (increases each level)
- Hazard Chance: 70%
- Target Score: 500 + (level-4) × 100 points
- Food Spawn Chance: 10%
- Named "MYSTERY ALLEY 1", "MYSTERY ALLEY 2", etc.
- Description: Beyond the known alleys...

### Hazards
- **Dogs 🐕**: Emerge from dustbins and must be avoided
- **Crazy Cats 😼**: Enemy cats also pop out of bins
- Hazards animate upward from the dustbin - time your jump carefully!

### Rival Cat Escape Mechanic
When a CRAZY_CAT hazard begins emerging from a dustbin:
- **Visual Warning**: A pulsing red exclamation mark (❗) appears above the bin
- **Escape Window**: You have a 10-frame window to jump away before collision activates
- **Haptic Warning**: Device vibrates when a CRAZY_CAT is within 300 units of the player
- **Sound Warning**: A warning sound plays once per hazard emergence
- Use these warnings to react quickly and avoid losing a life!

## 🎨 Visual Features

- **Retro Arcade Aesthetic**: Neon glows and cyberwave colors
- **Parallax Background**: Scrolling alley creates depth
- **Smooth Animations**: Cat state transitions (idle, jumping, falling)
- **Real-time HUD**: Score, lives, streak counter, and level name
- **Visual Feedback**: Glow effects for hazards and player
- **Food Items**: Colored ovals with glow effects (blue=fish, white=milk, yellow=cheese)
- **Hazard Warnings**: Pulsing red exclamation mark for emerging CRAZY_CAT hazards
- **Tutorial Progress**: Step indicator (Step 1/3, 2/3, 3/3) during tutorial

## 🔊 Audio

- Jump sound effect
- Landing confirmation tone (plays on every successful landing)
- Streak bonus celebration sound
- Danger/hazard warning sound (plays once per hazard emergence)
- Death sound effect
- Level complete fanfare
- Food collection reward chime (60ms)

## 💾 Persistence

- High score saved locally in SharedPreferences
- Score accumulates across level transitions
- Lives refresh at each level start

## 🏗️ Architecture

### Kotlin/Jetpack Compose
- **MVVM Pattern**: Clean separation of concerns
- **StateFlow**: Reactive state management
- **Canvas Drawing**: High-performance game rendering
- **Touch Input Handling**: Responsive gesture detection

### Code Organization
- `GameConstants.kt`: Centralized tuning values and magic numbers
- `GameModels.kt`: Data classes for game state
- `GameViewModel.kt`: Game loop and logic
- `LevelSystem.kt`: Level progression and difficulty scaling
- `SoundManager.kt`: Audio effects
- `MainActivity.kt`: UI and Compose rendering

## 🚀 How to Build & Run

### Prerequisites
- Android Studio (latest version)
- Android SDK 29+ (minSdk requirement)
- Target SDK 36 (Android 15)

### Build Steps
1. Open project in Android Studio
2. Sync Gradle files
3. Connect Android device or start emulator
4. Run the app (Shift+F10)

### Build Variant
- Default: Debug build for development
- Release: Use for production builds with ProGuard obfuscation

## 🎯 Tips & Strategies

1. **Master the Controls**: Practice moving left and right smoothly
2. **Anticipate Hazards**: Watch for bins with hazards (enemies emerge after a delay)
3. **Build Streaks**: Try to land on 5+ different bins consecutively for bonus points
4. **Speed Management**: Game speed increases with each landing - stay focused
5. **Level by Level**: Complete each level to unlock harder challenges
6. **High Score Chase**: Compare your score against your best

## 🔧 Technical Specifications

- **Minimum SDK**: 29 (Android 10)
- **Target SDK**: 36 (Android 15)
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Game Loop**: 60 FPS (16ms tick rate)
- **Physics**: Gravity-based vertical movement with keyboard-style horizontal controls

## 🐛 Known Issues & Future Enhancements

### Future Features
- [ ] Additional furniture/obstacles (plants, clotheslines, windows)
- [ ] More detailed cat animations with separate falling sprite
- [ ] Background music loop
- [ ] Global leaderboard system
- [ ] Difficulty selection before game start
- [ ] Different alley themes
- [ ] Settings screen (sound/haptics toggle)
- [ ] Statistics tracking (best streak, max distance)

### Performance Optimizations
- Dustbin culling for off-screen objects
- Efficient particle system for hazard animations
- Canvas layer optimization

## 📝 License & Credits

Classic "Alley Cat" game inspired by the original 1994 DOS arcade game.
This Android implementation is an educational recreation with modern enhancements.

## 🎓 Learning Resources

This project demonstrates:
- Kotlin coroutines for game loops
- Jetpack Compose for responsive UI
- Canvas API for custom graphics
- Touch input handling
- State management with StateFlow
- MVVM architecture in games
- Real-time physics simulation

Enjoy! 🐱
