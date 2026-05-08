# Requirements Document

## Introduction

This document defines the requirements for the "Alley Cat" Android game, inspired by the classic DOS-era Alley Cat game. The player controls a cat navigating an alley environment, jumping between dustbins (trash cans), collecting food items, avoiding hazards (rival cats and dogs), and surviving through increasingly difficult levels. The game is built with Kotlin and Jetpack Compose, targeting Android devices with touch-based controls.

## Glossary

- **Game_Engine**: The core game loop and physics system that processes game state updates at approximately 60 FPS
- **Player_Cat**: The player-controlled cat character that moves horizontally and jumps vertically
- **Dustbin**: A trash can object that the Player_Cat can land on and interact with to collect food
- **Food_Item**: A collectible object that spawns from dustbins, granting score points when collected by the Player_Cat
- **Hazard**: A dangerous entity (rival cat or dog) that emerges from dustbins and causes the Player_Cat to lose a life on contact
- **Rival_Cat**: A hazard type representing an aggressive cat that emerges from a dustbin
- **Dog**: A hazard type representing a dog that emerges from a dustbin
- **Level_System**: The progression system that manages difficulty scaling, level transitions, and win conditions
- **HUD**: The heads-up display showing score, lives, streak, and level information during gameplay
- **Touch_Controller**: The input system that translates touch gestures into Player_Cat movement and jump actions
- **Score_System**: The system that tracks points earned from landing on bins and collecting food items
- **Streak_System**: The subsystem that tracks consecutive successful landings and awards bonus points
- **Sound_Manager**: The audio system that plays sound effects for game events
- **Haptic_System**: The vibration feedback system that provides tactile responses to game events

## Requirements

### Requirement 1: Player Cat Movement

**User Story:** As a player, I want to move the cat left and right by holding the sides of the screen, so that I can position the cat to land on dustbins.

#### Acceptance Criteria

1. WHILE the player holds the left zone (leftmost 30% of screen width) of the screen, THE Touch_Controller SHALL move the Player_Cat leftward at a constant horizontal speed of 20 logical units per game tick
2. WHILE the player holds the right zone (rightmost 30% of screen width) of the screen, THE Touch_Controller SHALL move the Player_Cat rightward at a constant horizontal speed of 20 logical units per game tick
3. THE Game_Engine SHALL constrain the Player_Cat horizontal position to remain no less than 20 logical units from the left screen edge and no more than 20 logical units from the right screen edge (accounting for the Player_Cat width of 100 logical units)
4. WHEN the player releases the touch zone, THE Touch_Controller SHALL stop the Player_Cat horizontal movement within the same game tick
5. WHILE the game is paused, in a Game Over state, or displaying a level complete overlay, THE Touch_Controller SHALL ignore all movement inputs

### Requirement 2: Player Cat Jumping

**User Story:** As a player, I want to make the cat jump by tapping the center of the screen, so that I can reach dustbins and avoid ground-level hazards.

#### Acceptance Criteria

1. WHEN the player taps the center zone (middle 40% of screen width) of the screen AND the Player_Cat is in the IDLE state, THE Game_Engine SHALL apply an upward velocity of JUMP_STRENGTH to the Player_Cat and transition the Player_Cat state to JUMPING
2. WHILE the Player_Cat is airborne, THE Game_Engine SHALL apply a gravitational acceleration of GRAVITY per frame (at approximately 60 FPS) to increase downward velocity each frame
3. WHILE the Player_Cat is in a JUMPING or FALLING state, THE Game_Engine SHALL ignore additional jump inputs from the center zone
4. WHEN the Player_Cat vertical velocity changes from negative (upward) to positive (downward), THE Game_Engine SHALL transition the Player_Cat state from JUMPING to FALLING
5. IF the Player_Cat is not in the IDLE state when a center zone tap occurs, THEN THE Game_Engine SHALL discard the tap input without altering the Player_Cat velocity or state

### Requirement 3: Dustbin Landing

**User Story:** As a player, I want to land on dustbins to score points and progress through the level, so that I can advance in the game.

#### Acceptance Criteria

1. WHEN the Player_Cat is descending with positive vertical velocity and crosses the Dustbin top boundary, and the Player_Cat horizontal bounding box overlaps the Dustbin horizontal bounding box by at least 1 pixel, THE Game_Engine SHALL register a successful landing and set the Player_Cat state to IDLE
2. WHEN a successful landing occurs, THE Score_System SHALL award 1 point to the player per landing
3. WHEN a successful landing occurs on a Dustbin different from the last landed Dustbin, THE Streak_System SHALL increment the streak counter by one
4. WHEN the Player_Cat falls below the fall-off threshold (ground level plus 200 logical units), THE Game_Engine SHALL deduct one life from the player and reset the Player_Cat to the starting position
5. WHEN a successful landing occurs, THE Sound_Manager SHALL play a landing confirmation sound
6. WHEN a successful landing occurs, THE Haptic_System SHALL provide a 50ms vibration pulse
7. IF the Player_Cat lands on a Dustbin that has an active Hazard with visibility above the collision threshold, THEN THE Game_Engine SHALL deduct one life from the player instead of registering a successful landing

### Requirement 4: Food Collection from Dustbins

**User Story:** As a player, I want to catch food items that appear from dustbins, so that I can earn bonus points and feel rewarded for skillful play.

#### Acceptance Criteria

1. WHEN the Player_Cat lands on a Dustbin that contains a Food_Item and does not contain a Hazard, THE Game_Engine SHALL spawn the Food_Item above the Dustbin
2. WHEN a Food_Item is spawned, THE Game_Engine SHALL animate the Food_Item moving upward for a duration of 300 to 500 milliseconds before applying gravity to make it fall
3. WHEN the Player_Cat collides with a visible Food_Item, THE Score_System SHALL award 5 bonus points to the player and remove the Food_Item from the game world
4. IF a Food_Item falls below the screen without being collected, THEN THE Game_Engine SHALL remove the Food_Item from the game world
5. THE Game_Engine SHALL assign Food_Items to Dustbins that do not contain a Hazard, with a probability between 0.1 and 0.5 configurable per level
6. IF the Player_Cat has already collected a Food_Item from a specific Dustbin, THEN THE Game_Engine SHALL NOT spawn another Food_Item from that same Dustbin

### Requirement 5: Hazard System

**User Story:** As a player, I want to face hazards emerging from dustbins that I must avoid, so that the game provides challenge and tension.

#### Acceptance Criteria

1. WHEN a Dustbin with a Hazard scrolls within 1500 logical units of the left screen edge, THE Game_Engine SHALL animate the Hazard emerging upward from the Dustbin at a rate of 0.05 offset units per frame
2. WHEN the Hazard emergence animation offset reaches 0.5 (50% visible), THE Game_Engine SHALL enable collision detection for that Hazard using the Hazard bounding box (150x150 logical units) against the Player_Cat bounding box (100x100 logical units)
3. WHEN the Player_Cat collides with an active Hazard, THE Game_Engine SHALL deduct one life from the player and reposition the Player_Cat to the safe starting position
4. IF the Player_Cat has already lost a life from a Hazard collision within the current repositioning sequence, THEN THE Game_Engine SHALL not deduct additional lives until the Player_Cat has been reset to the safe starting position
5. WHEN the Player_Cat collides with an active Hazard, THE Sound_Manager SHALL play a collision sound effect
6. WHEN the Player_Cat collides with an active Hazard, THE Haptic_System SHALL produce a strong double-pulse vibration pattern
7. THE Game_Engine SHALL assign Hazards to Dustbins with a probability determined by the current level (20% at level 1, 35% at level 2, 50% at level 3, 65% at level 4)
8. THE Game_Engine SHALL support two Hazard types: Dog and Rival_Cat, selected randomly when a Hazard is assigned to a Dustbin
9. THE Game_Engine SHALL NOT assign both a Hazard and a Food_Item to the same Dustbin

### Requirement 6: Rival Cat Escape Mechanic

**User Story:** As a player, I want to be warned when rival cats emerge from bins so I can escape in time, so that the game feels fair and reactive.

#### Acceptance Criteria

1. WHEN a Rival_Cat Hazard begins emerging from a Dustbin (hazardYOffset transitions from 0.0), THE Game_Engine SHALL display a visual warning indicator on that Dustbin that remains visible until the Hazard reaches full emergence
2. WHILE a Rival_Cat Hazard emergence offset is below the collision threshold of 0.5, THE Game_Engine SHALL allow the Player_Cat to escape by jumping away without triggering a life loss
3. WHEN the Rival_Cat Hazard emergence offset reaches 0.5 or above, THE Game_Engine SHALL activate collision detection such that contact with the Player_Cat deducts a life
4. WHEN a Rival_Cat Hazard is on a Dustbin within 300 logical units horizontally of the Player_Cat position and has begun emerging, THE Haptic_System SHALL provide a warning vibration pulse consisting of a double-pulse pattern lasting no more than 150ms
5. IF the Player_Cat is standing on a Dustbin when its Rival_Cat Hazard begins emerging, THEN THE Game_Engine SHALL allow at least 10 game frames before collision detection activates, giving the player time to jump away

### Requirement 7: Scrolling World and Dustbin Spawning

**User Story:** As a player, I want the alley to scroll continuously with new dustbins appearing, so that the game feels like an endless run through the alley.

#### Acceptance Criteria

1. WHILE the game is active, THE Game_Engine SHALL scroll all Dustbins leftward at the current game speed, starting at an initial speed of 10 pixels per frame
2. WHEN the rightmost Dustbin position falls below the spawn threshold of 2000 pixels from the left edge, THE Game_Engine SHALL generate a new Dustbin at a random horizontal distance between 450 and 750 pixels ahead of the previous Dustbin
3. WHEN a Dustbin scrolls beyond 200 pixels past the left edge of the screen, THE Game_Engine SHALL remove that Dustbin and any associated Food_Item or Hazard from the game world
4. THE Game_Engine SHALL maintain a horizontal gap between consecutive Dustbins of no less than 450 pixels and no more than 750 pixels so that the Player_Cat can reach the next Dustbin with a single jump
5. WHEN the Player_Cat completes a successful landing, THE Game_Engine SHALL increase the scroll speed by 0.1 pixels per frame, up to a maximum speed of 25 pixels per frame
6. WHEN the game starts, THE Game_Engine SHALL place an initial set of 6 Dustbins distributed across the screen with spacing between 450 and 750 pixels apart

### Requirement 8: Lives and Game Over

**User Story:** As a player, I want a lives system that gives me multiple chances before the game ends, so that a single mistake does not end my run.

#### Acceptance Criteria

1. THE Game_Engine SHALL initialize the player with 3 lives at the start of each level
2. WHEN the player loses a life, THE Game_Engine SHALL reset the Player_Cat to the horizontal center of the screen at ground level with zero velocity
3. WHEN the player loses a life, THE Game_Engine SHALL reset the streak counter to zero
4. WHEN the player has zero remaining lives and loses another life, THE Game_Engine SHALL transition to the Game Over state
5. WHEN the Game Over state is reached, THE HUD SHALL display the final score, the all-time high score, and options to restart or return to the main menu
6. IF the final score exceeds the stored high score, THEN THE Score_System SHALL persist the new high score to device storage
7. WHEN the player loses a life and has at least one life remaining, THE HUD SHALL update the displayed life count within 1 frame of the life loss event

### Requirement 9: Level Progression

**User Story:** As a player, I want to progress through increasingly difficult levels, so that the game remains challenging and rewarding over time.

#### Acceptance Criteria

1. WHEN the player score reaches the current level score threshold, THE Level_System SHALL display a level complete overlay showing the completed level name and the next level name, and the overlay SHALL remain visible until the player taps to continue
2. WHEN the player advances to the next level, THE Level_System SHALL increase the base game speed by 2 units per level, starting at 10 units at Level 1 up to a maximum of 16 units at Level 4
3. WHEN the player advances to the next level, THE Level_System SHALL increase the hazard spawn probability, progressing from 0.20 at Level 1 through 0.35, 0.50, to 0.65 at Level 4
4. THE Level_System SHALL define at least four distinct levels, each with a unique name, a starting speed, a maximum speed, a base hazard probability, and a score threshold to advance to the next level
5. WHEN the player advances to the next level, THE Level_System SHALL reset the player lives to 3
6. WHEN the player completes Level 4 by reaching a cumulative score of 350 during that level, THE Level_System SHALL unlock procedurally generated mystery levels with a fixed starting speed of 18 units, a hazard probability of 0.70, and speed increasing by 1 unit per subsequent mystery level up to the maximum of 25 units

### Requirement 10: Streak and Scoring Bonuses

**User Story:** As a player, I want to earn bonus points for consecutive successful landings, so that skillful play is rewarded.

#### Acceptance Criteria

1. WHEN the Player_Cat lands on a Dustbin different from the most recently landed Dustbin without losing a life in between, THE Streak_System SHALL increment the streak counter by 1
2. WHILE the streak counter is at or above 5, THE Score_System SHALL award 1 additional bonus point per successful landing on top of the base landing points
3. WHILE the streak counter is at or above 5, THE HUD SHALL display a streak indicator showing the current streak count
4. WHEN the Player_Cat lands on a new Dustbin and the streak counter reaches or exceeds 5, THE Sound_Manager SHALL play a bonus sound effect
5. WHEN the player loses a life, THE Streak_System SHALL reset the streak counter to zero
6. WHEN a new game or new level begins, THE Streak_System SHALL initialize the streak counter to zero

### Requirement 11: Tutorial System

**User Story:** As a new player, I want an interactive tutorial that teaches me the controls, so that I can learn how to play without frustration.

#### Acceptance Criteria

1. WHEN a player launches the game and no tutorial completion flag exists in device storage, THE Game_Engine SHALL display a tutorial button on the splash screen
2. WHEN the player starts the tutorial, THE Game_Engine SHALL present 3 sequential steps: (1) a movement step requiring the player to move left and right, (2) a jump step requiring the player to perform a jump, and (3) a landing step requiring the player to land on a Dustbin, advancing to the next step only after the player successfully performs the required action
3. WHILE the tutorial is active, THE Game_Engine SHALL display an instructional text prompt describing the required action and highlight the corresponding input zone for the current step
4. WHILE the tutorial is active, THE Game_Engine SHALL prevent the player from losing lives
5. WHEN the player completes all 3 tutorial steps, THE Game_Engine SHALL persist the tutorial completion status to device storage
6. WHEN the tutorial is completed, THE Game_Engine SHALL hide the tutorial button on subsequent launches
7. IF the player taps a back or skip control during the tutorial, THEN THE Game_Engine SHALL exit the tutorial, return to the splash screen, and retain the tutorial button for the next launch

### Requirement 12: Pause and Resume

**User Story:** As a player, I want to pause and resume the game at any time during gameplay, so that I can take breaks without losing progress.

#### Acceptance Criteria

1. WHEN the player taps the pause button during active gameplay, THE Game_Engine SHALL freeze all physics updates, animations, and audio playback within the current frame
2. WHILE the game is paused, THE HUD SHALL display a pause overlay with resume and menu options
3. WHEN the player taps resume, THE Game_Engine SHALL continue gameplay from the exact paused state, restoring all object positions, velocities, score, lives, streak count, and audio playback
4. WHEN the player taps menu from the pause overlay, THE Game_Engine SHALL persist the high score if the current score exceeds the stored high score, then return to the splash screen and discard the current run
5. WHEN the app loses foreground focus during active gameplay, THE Game_Engine SHALL automatically trigger the pause state
6. WHILE the game is paused, THE Game_Engine SHALL ignore all gameplay touch inputs except interactions with the pause overlay buttons

### Requirement 13: Visual Presentation

**User Story:** As a player, I want a visually appealing alley environment with parallax scrolling and street art aesthetics, so that the game feels immersive and stylish.

#### Acceptance Criteria

1. THE Game_Engine SHALL render a 3-layer parallax background consisting of a sunset sky gradient layer, a far buildings layer, and a near buildings layer, where each successive layer scrolls at a faster rate relative to the game scroll speed
2. WHILE the game is active, THE Game_Engine SHALL scroll the far buildings layer at 20% of the current game speed and the near buildings layer at 50% of the current game speed to produce a depth effect
3. THE Game_Engine SHALL render the Player_Cat with a unique visual representation for each of the four states: idle, jumping, falling, and dead, such that each state is visually distinguishable from the others without relying on position alone
4. THE Game_Engine SHALL render Dustbins with visible lids and graffiti-style color accents
5. WHEN a Hazard begins emerging from a Dustbin, THE Game_Engine SHALL animate the Hazard rising from fully hidden to fully visible over the course of the emergence animation, rendering body, head, and eye details progressively
6. THE Game_Engine SHALL render a glow effect with 30% opacity behind the Player_Cat to enhance visibility against the background

### Requirement 14: Audio Feedback

**User Story:** As a player, I want sound effects for key game events, so that the game feels responsive and engaging.

#### Acceptance Criteria

1. WHEN the Player_Cat jumps, THE Sound_Manager SHALL play an ascending beep tone with a duration of no more than 100 milliseconds
2. WHEN the Player_Cat loses a life, THE Sound_Manager SHALL play a descending collision tone with a duration of no more than 200 milliseconds
3. WHEN the player earns a streak bonus, THE Sound_Manager SHALL play a celebratory pip tone with a duration of no more than 150 milliseconds
4. WHEN the player completes a level, THE Sound_Manager SHALL play a triumphant confirmation tone with a duration of no more than 400 milliseconds
5. WHEN the app launches, THE Sound_Manager SHALL initialize audio resources and confirm readiness before gameplay begins
6. WHEN the app is destroyed, THE Sound_Manager SHALL release all audio resources to prevent resource leaks
7. IF audio resource initialization fails, THEN THE Sound_Manager SHALL log the failure and allow the game to continue without sound

### Requirement 15: Haptic Feedback

**User Story:** As a player, I want vibration feedback for important game events, so that the game feels tactile and responsive on my phone.

#### Acceptance Criteria

1. WHEN the Player_Cat lands successfully, THE Haptic_System SHALL produce a single vibration pulse of 50ms duration at maximum amplitude
2. WHEN the Player_Cat collides with a Hazard, THE Haptic_System SHALL produce a double-pulse vibration pattern consisting of two 150ms pulses separated by a 100ms gap at maximum amplitude
3. WHEN the player earns a streak bonus, THE Haptic_System SHALL produce a double-pulse vibration pattern consisting of two 100ms pulses separated by a 50ms gap at moderate amplitude (approximately 80% of maximum)
4. WHEN the player completes a level, THE Haptic_System SHALL produce a triple-pulse vibration pattern consisting of three 100ms pulses each separated by 100ms gaps at maximum amplitude
5. IF the device does not support vibration, THEN THE Haptic_System SHALL skip all vibration calls and allow gameplay to continue uninterrupted without displaying any error to the player
6. THE Haptic_System SHALL produce vibration patterns that are each perceptibly distinct from one another in duration, amplitude, or pulse count so that the player can differentiate event types by feel alone
7. IF the player has disabled haptic feedback in device accessibility settings, THEN THE Haptic_System SHALL suppress all vibration output

### Requirement 16: High Score Persistence

**User Story:** As a player, I want my high score saved between sessions, so that I can track my best performance over time.

#### Acceptance Criteria

1. THE Score_System SHALL persist the highest achieved score to device SharedPreferences storage as an integer value with a default of 0
2. WHEN the game launches, THE Score_System SHALL load the previously saved high score from device storage, defaulting to 0 if no prior value exists
3. WHEN the player's current run score exceeds the stored high score during gameplay, THE Score_System SHALL update the persisted value in SharedPreferences within the same frame
4. WHEN the Game Over screen is displayed, THE HUD SHALL display the all-time high score alongside the current run score
5. IF the Score_System fails to read or write the high score from device storage, THEN THE Score_System SHALL default the high score to 0 and continue gameplay without interruption
