# 🐱 AlleyCat - Classic Arcade Reimagined

A feature-rich Android port of the iconic 1990s "Alley Cat" arcade game, rebuilt with modern technologies and enhanced gameplay mechanics.

## ✨ What's New

This version brings the classic arcade experience to your Android device with significant improvements:

### v3.0 Features (Latest)
- ✅ **Food Item Collection** - Collect fish, milk, and cheese from dustbins for 5 bonus points each
- ✅ **Rival Cat Escape Mechanic** - Visual/haptic warnings and 10-frame escape window for CRAZY_CAT hazards
- ✅ **Refined Tutorial System** - Action-based 3-step tutorial with skip button and progress indicator
- ✅ **Auto-Pause on Background** - Game pauses automatically when app goes to background
- ✅ **Landing Sound on All Landings** - Audio feedback on every successful landing
- ✅ **Level 4 Completion & Mystery Levels** - Level 4 now completable (350 pts), infinite mystery levels beyond
- ✅ **Kotest Property-Based Testing** - 29 correctness properties with custom generators

### Core Improvements (v2.0)
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
- Food items rendered as colored ovals with glow effects (blue=fish, white=milk, yellow=cheese)
- Retro arcade aesthetic with cyberwave color scheme
- Smooth animations with falling state transitions
- Pulsing red exclamation mark warning for emerging hazards

### Difficulty Scaling
- Progressive hazard increase (20% → 65% spawn chance)
- Food spawn chance decreases per level (30% → 10%)
- Game speed acceleration per landing
- Level-specific starting speeds and target scores
- Balanced difficulty curve across all levels including mystery levels

## 🎮 Quick Start

### Installation
1. Clone or download this repository
2. Open in Android Studio
3. Run on Android device/emulator (SDK 29+)

### First Time Playing
1. Tap **START GAME** to begin Level 1
2. **HOW TO PLAY** button shows control instructions
3. Complete the 3-step tutorial (move left/right, jump, land on a bin)
4. Tap LEFT/CENTER/RIGHT zones to move/jump
5. Collect food items for bonus points
6. Reach the score target to complete the level
7. Progress through all 4 levels and into mystery levels!

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
├── GameModels.kt             # Data classes (GameState, Dustbin, FoodItem, etc)
├── GameViewModel.kt          # Game logic and state management
├── HapticFeedback.kt         # Vibration feedback system
├── LevelSystem.kt            # Level progression data
├── SoundManager.kt           # Audio effects handler
├── MainActivity.kt           # UI and Compose rendering
└── ui/theme/                 # Theme definitions

app/src/test/java/com/example/alleycat/
└── properties/               # Kotest property-based tests
    └── GamePropertyTests.kt  # 29 correctness properties
```

## 🎯 Gameplay Features

### Scoring
- 1 point per successful landing
- 1 bonus point for 5+ streak
- 5 bonus points for collecting food items
- Score persists across levels

### Food Collection
- Non-hazard dustbins may spawn food when the cat lands
- Food types: Fish (blue), Milk (white), Cheese (yellow)
- Food arcs upward then falls under gravity - catch it for 5 bonus points
- Spawn chance decreases per level (30% L1 → 10% mystery levels)
- Dustbins never have both food AND hazards (mutual exclusion)

### Lives & Game Over
- Start with 3 lives per level
- Lose life by falling or hitting hazard
- Game over when lives depleted

### Hazards
- **Dogs** and **Crazy Cats** emerge from bins
- Higher difficulty = more frequent hazards
- Hazards animate upward - dodge with movement

### Rival Cat Escape
- Pulsing red exclamation mark warns when CRAZY_CAT hazards begin emerging
- 10-frame escape window: jump away before collision activates
- Proximity-based haptic warning when CRAZY_CAT is within 300 units
- Sound warning plays once per hazard emergence

### Level Targets
| Level | Name | Target | Hazard % | Food Chance |
|-------|------|--------|----------|-------------|
| 1 | Alley Basics | 100 pts | 20% | 30% |
| 2 | Hazard Alley | 200 pts | 35% | 25% |
| 3 | Chaos Corner | 350 pts | 50% | 20% |
| 4 | Final Gauntlet | 350 pts | 65% | 15% |
| 5+ | Mystery Alley N | 500+(N×100) pts | 70% | 10% |

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

### v3.0 (Feature Expansion)
- Food item collection system (fish/milk/cheese) with physics-based spawning
- Rival cat escape mechanic with visual/haptic/audio warnings
- Refined action-based tutorial system with skip and progress indicator
- Auto-pause on app background via lifecycle observer
- Landing sound on all successful landings
- Level 4 now completable (350 pts), mystery levels beyond level 4
- Kotest property-based testing infrastructure (29 properties)
- New data model fields: FoodItem, FoodType, food/escape tracking on Dustbin
- High score edge case handling with nested try/catch

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

Potential additions for future versions:
- Multiple alley themes (home, industrial, rooftop)
- Animated cat sprites with separate falling sprite
- Background music loops
- Global leaderboard
- Android TV support
- Settings screen (sound/haptics toggle, difficulty adjust)
- Statistics tracking (best streak, max distance, levels reached)
- Cosmetics (different cat skins, bin styles)
- Screen effects (shake on collision, particle effects)

## 📝 Credits

Original game concept: **"Alley Cat"** (Sierra On-Line, 1994)

This Android implementation recreates and enhances the classic gameplay with modern development practices.

## 📄 License

Educational project - feel free to modify and extend!

---

**Ready to jump into the alleys? Download and play AlleyCat now!** 🐱💨
