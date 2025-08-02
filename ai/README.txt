# Space Invaders
This repository contains a classic arcade-style Space Invaders game built with Java and Swing. Take control of your ship, dodge asteroids, destroy enemy UFOs, and collect power-ups to achieve the highest score. The game features a dynamic difficulty system, a comprehensive statistics tracker, and an achievement system to reward your skills.

## Features

- **Classic Arcade Gameplay**: Navigate your ship and shoot down descending enemies.
- **Dynamic Entities**: Encounter different types of objects, including hostile enemies, damaging asteroids, and helpful power-ups.
- **Player Progression**: The game's difficulty increases as you play. Level up by reaching score thresholds, which increases the spawn rate of objects.
- **Power-Ups**: Collect power-ups to gain advantages, such as restoring health (`HealthPowerUp`) or boosting your score (`ShieldPowerUp`).
- **Statistics and Achievements**: Your performance is tracked throughout the game. Stats include:
    - Shots Fired & Hit
    - Shooting Accuracy
    - Survival Time
    - Enemies Destroyed
- **Unlockable Achievements**: Master the game to unlock achievements like "Survivor", "Enemy Exterminator", and "Sharp Shooter". Progress is tiered from Novice to Master.
- **Graphical User Interface**: A clean and responsive UI built with Java Swing, showing the game area, a live event log, and player stats.

## How to Play

The controls are simple and intuitive:

| Key | Action          |
|-----|-----------------|
| `W` | Move Up         |
| `A` | Move Left       |
| `S` | Move Down       |
| `D` | Move Right      |
| `F` | Fire Bullet     |
| `P` | Pause/Unpause Game |

The objective is to survive as long as possible while destroying enemies to maximize your score. Avoid colliding with enemies and asteroids, as they will deplete your health. The game ends when your ship's health reaches zero.

## How to Run

You will need a Java Development Kit (JDK) installed to compile and run the game.

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/yjn563/space-invaders.git
    cd space-invaders
    ```

2.  **Compile the source files:**
    Navigate to the root directory of the project and compile the Java files.
    ```sh
    javac -d bin -sourcepath src src/game/Main.java
    ```

3.  **Run the game:**
    Execute the main class to start the game.
    ```sh
    java -cp bin game.Main
    ```

A game window will appear, and you can start playing immediately.

## Project Structure

The project is organized into several packages, following a Model-View-Controller (MVC) pattern.

```
└── src/
    └── game/
        ├── Main.java              # Main entry point for the application.
        ├── GameController.java    # Handles game logic, user input, and game loop.
        ├── GameModel.java         # Manages game state, objects, collisions, and spawning.
        ├── achievements/          # Classes for managing and tracking player achievements.
        ├── core/                  # Defines core game objects (Ship, Enemy, Bullet, PowerUp).
        ├── exceptions/            # Custom exceptions, e.g., for boundary checks.
        ├── ui/                    # UI interfaces and the Swing GUI implementation.
        └── utility/               # Helper enums and functional interfaces.
└── assets/
    ├── asteroid.png
    ├── bullet.png
    ├── enemy.png
    ├── health.png
    ├── shield.png
    └── ship.png
```

-   **`game.core`**: Contains the fundamental game entities like `Ship`, `Enemy`, `Asteroid`, `Bullet`, and `PowerUp`. Each object defines its behavior and appearance.
-   **`game.ui`**: Manages the user interface. `GUI.java` builds the main window with Swing and includes panels for the game `Canvas`, a `Log` for messages, and a `Stats` display.
-   **`game.achievements`**: Implements the achievement system. `PlayerStatsTracker` monitors performance metrics, and `AchievementManager` handles the logic for unlocking and tracking achievements.
-   **`assets`**: Contains the PNG images used for all game objects.