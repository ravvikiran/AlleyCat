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
- Score carries across level transitions

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
- Description: Learn to jump and land on bins

#### Level 2: Hazard Alley
- Starting Speed: 12
- Hazard Chance: 35%
- Target Score: 200 points
- Description: More dangers appear! Avoid those hazards!

#### Level 3: Chaos Corner
- Starting Speed: 14
- Hazard Chance: 50%
- Target Score: 350 points
- Description: Speed increases and hazards are everywhere!

#### Level 4: Final Gauntlet
- Starting Speed: 16
- Hazard Chance: 65%
- Target Score: Unlimited
- Description: The ultimate test of your skills!

### Hazards
- **Dogs 🐕**: Emerge from dustbins and must be avoided
- **Crazy Cats 😼**: Enemy cats also pop out of bins
- Hazards animate upward from the dustbin - time your jump carefully!

## 🎨 Visual Features

- **Retro Arcade Aesthetic**: Neon glows and cyberwave colors
- **Parallax Background**: Scrolling alley creates depth
- **Smooth Animations**: Cat state transitions (idle, jumping, falling)
- **Real-time HUD**: Score, lives, streak counter, and level name
- **Visual Feedback**: Glow effects for hazards and player

## 🔊 Audio

- Jump sound effect
- Landing confirmation tone
- Streak bonus celebration sound
- Danger/hazard warning sound
- Death sound effect
- Level complete fanfare

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
- [ ] Power-ups (food, milk dishes)
- [ ] Different alley themes

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
