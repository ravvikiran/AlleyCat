# 🐱 AlleyCat - Classic Arcade Reimagined

A feature-rich Android port of the iconic 1990s "Alley Cat" arcade game, rebuilt with modern technologies and enhanced gameplay mechanics.

## ✨ What's New

This version brings the classic arcade experience to your Android device with significant improvements:

### Core Improvements
- ✅ **Horizontal Cat Movement** - Move left/right to dodge hazards and position yourself
- ✅ **Multiple Lives System** - 3 lives per level with visual heart indicators
- ✅ **Level Progression** - 4 increasingly difficult levels with unique challenges
- ✅ **Fixed Collision Detection** - Accurate hazard hitboxes for fair gameplay
- ✅ **Streak Bonus System** - Land on 5+ consecutive bins for point multipliers
- ✅ **Game Constants Centralization** - All tuning values in one place for easy balancing

### Visual & Audio Enhancements
- Neon glow effects on cat and dustbins
- Parallax scrolling background
- Multiple sound effects with level-appropriate audio feedback
- Retro arcade aesthetic with cyberwave color scheme
- Smooth animations with falling state transitions

### Difficulty Scaling
- Progressive hazard increase (20% → 65% spawn chance)
- Game speed acceleration per landing
- Level-specific starting speeds and target scores
- Balanced difficulty curve across all 4 levels

## 🎮 Quick Start

### Installation
1. Clone or download this repository
2. Open in Android Studio
3. Run on Android device/emulator (SDK 29+)

### First Time Playing
1. Tap **START GAME** to begin Level 1
2. **HOW TO PLAY** button shows control instructions
3. Tap LEFT/CENTER/RIGHT zones to move/jump
4. Reach the score target to complete the level
5. Progress through all 4 levels!

## 📱 System Requirements

| Requirement | Version |
|---|---|
| Min SDK | 29 (Android 10) |
| Target SDK | 36 (Android 15) |
| Language | Kotlin 100% |
| UI Framework | Jetpack Compose |

## 🏗️ Project Structure

```
app/src/main/java/com/example/alleycat/
├── GameConstants.kt          # All game tuning values
├── GameModels.kt             # Data classes (GameState, Dustbin, etc)
├── GameViewModel.kt          # Game logic and state management
├── LevelSystem.kt            # Level progression data
├── SoundManager.kt           # Audio effects handler
├── MainActivity.kt           # UI and Compose rendering
└── ui/theme/                 # Theme definitions
```

## 🎯 Gameplay Features

### Scoring
- 1 point per successful landing
- 1 bonus point for 5+ streak
- Score persists across levels

### Lives & Game Over
- Start with 3 lives per level
- Lose life by falling or hitting hazard
- Game over when lives depleted

### Hazards
- **Dogs** and **Crazy Cats** emerge from bins
- Higher difficulty = more frequent hazards
- Hazards animate upward - dodge with movement

### Level Targets
| Level | Name | Target | Hazard % |
|-------|------|--------|----------|
| 1 | Alley Basics | 100 pts | 20% |
| 2 | Hazard Alley | 200 pts | 35% |
| 3 | Chaos Corner | 350 pts | 50% |
| 4 | Final Gauntlet | Unlimited | 65% |

## 🕹️ Controls

| Action | Control |
|--------|---------|
| Move Left | Tap LEFT 30% of screen |
| Move Right | Tap RIGHT 30% of screen |
| Jump | Tap CENTER 40% of screen |

## 🔧 Key Technical Highlights

### Game Architecture
- **MVVM Pattern** with clean separation of concerns
- **Reactive State** management using Kotlin StateFlow
- **Canvas-Based Rendering** for high-performance drawing
- **Gesture Detection** for responsive touch input

### Game Loop
- 60 FPS (16ms tick rate)
- Gravity-based physics simulation
- Collision detection with fixed hitboxes
- Smooth horizontal movement with screen boundaries

### Audio System
- Tone-based sound effects (can be extended with samples)
- Context-aware audio (landing, danger, bonus, death)
- Sound manager singleton pattern

## 📊 Recent Changes

### v2.0 (Major Rewrite)
- Implemented horizontal cat movement (critical missing feature)
- Fixed hazard collision detection (was missing 25% of hitbox)
- Added multiple lives system (was instant game over)
- Implemented 4-level progression system
- Centralized game constants
- Enhanced audio feedback
- Improved UI with level display
- Added comprehensive documentation

### v1.0 (Original)
- Basic jumping mechanics
- Single infinite alley
- Hazard spawning system
- Score tracking with high score persistence

## 🚀 Building & Running

### From Android Studio
1. Sync Gradle
2. Select device/emulator
3. Run (Shift+F10)

### Debug Build
```bash
./gradlew installDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## 📚 Documentation

- **[GAMEPLAY_GUIDE.md](GAMEPLAY_GUIDE.md)** - Detailed gameplay mechanics and strategies
- **Code Comments** - Inline documentation for complex logic
- **GameConstants.kt** - Annotated configuration values

## 🎓 Learning Value

This project is excellent for learning:
- Kotlin game development patterns
- Jetpack Compose for game UI
- Canvas API for graphics
- Coroutines for async game loops
- Touch input handling
- State management in games
- Physics simulation basics

## 🐛 Troubleshooting

### App crashes on startup
- Ensure Android API 29+ is installed
- Try clearing app data and reinstalling

### Game is too slow/fast
- Check device performance settings
- Lower graphics quality if needed

### Sound not working
- Enable audio in device settings
- Check system volume level

## 🔮 Future Enhancements

Potential additions for v3.0:
- Multiple alley themes (home, industrial, rooftop)
- Power-up items
- Animated cat sprites
- Background music loops
- Global leaderboard
- Touch haptic feedback
- Android TV support

## 📝 Credits

Original game concept: **"Alley Cat"** (Sierra On-Line, 1994)

This Android implementation recreates and enhances the classic gameplay with modern development practices.

## 📄 License

Educational project - feel free to modify and extend!

---

**Ready to jump into the alleys? Download and play AlleyCat now!** 🐱💨
