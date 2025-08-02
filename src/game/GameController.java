package game;

import game.achievements.Achievement;
import game.achievements.AchievementManager;
import game.achievements.PlayerStatsTracker;
import game.core.SpaceObject;
import game.exceptions.BoundaryExceededException;
import game.ui.UI;
import game.utility.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * The Controller handling the game flow and interactions.
 * <p>
 * Holds references to the UI and the Model, so it can pass information and references back and forth as necessary.<br>
 * Manages changes to the game, which are stored in the Model, and displayed by the UI.<br>
 */
public class GameController {
    private static final int MASTERED_SURVIVAL_TIME = 120;
    private static final int MASTERED_ENEMY_SHOTS_NUM = 20;

    private final long startTime;
    private final UI ui;
    private final GameModel model;
    private final AchievementManager achievementManager;
    private PlayerStatsTracker statsTracker;

    /**
     * An internal variable indicating whether certain methods should log their actions.
     * Not all methods respect isVerbose.
     */
    private boolean isVerbose = false;
    private boolean isPaused = false;


    /**
     * Initializes the game controller with the given UI, GameModel and AchievementManager.<br>
     * Stores the UI, GameModel, AchievementManager and start time.<br>
     * The start time System.currentTimeMillis() should be stored as a long.<br>
     * Starts the UI using UI.start().<br>
     *
     * @param ui the UI used to draw the Game
     * @param model the model used to maintain game information
     * @param achievementManager the manager used to maintain achievement information
     *
     * @requires ui is not null
     * @requires model is not null
     * @requires achievementManager is not null
     * @provided
     */
    public GameController(UI ui, GameModel model, AchievementManager achievementManager) {
        this.ui = ui;
        ui.start();
        this.model = model;
        this.startTime = System.currentTimeMillis(); // Current time
        this.achievementManager = achievementManager;
    }


    /**
     * Initializes the game controller with the given UI and GameModel.<br>
     * Stores the ui, model and start time.<br>
     * The start time System.currentTimeMillis() should be stored as a long.<br>
     *
     * @param ui    the UI used to draw the Game
     * @param achievementManager the manager used to maintain achievement information
     *
     * @requires ui is not null
     * @requires achievementManager is not null
     * @provided
     */
    public GameController(UI ui, AchievementManager achievementManager) {
        this(ui, new GameModel(ui::log, new PlayerStatsTracker()), achievementManager);
    }

    /**
     * Returns the current game model.
     * @return the current game model.
     */
    public GameModel getModel() {
        return model;
    }

    /**
     * Returns the current PlayerStatsTracker.
     * @return the current PlayerStatsTracker
     */
    public PlayerStatsTracker getStatsTracker() {
        if (statsTracker == null) {
            statsTracker = new PlayerStatsTracker(startTime);
        }
        return statsTracker;
    }

    /**
     * Sets verbose state to the provided input. Also sets the models verbose state to the
     * provided input.
     * @param verbose - whether to set verbose state to true or false.
     */
    public void setVerbose(boolean verbose) {
        this.isVerbose = verbose;
        model.setVerbose(verbose);
    }

    /**
     * Starts the main game loop.<br>
     * <p>
     * Passes onTick and handlePlayerInput to ui.onStep and ui.onKey respectively.
     * @provided
     */
    public void startGame() {
        ui.onStep(this::onTick);
        ui.onKey(this::handlePlayerInput);
    }

    /**
     * Uses the provided tick to call and advance the following:<br>
     * - A call to model.updateGame(tick) to advance the game by the given tick.<br>
     * - A call to model.checkCollisions() to handle game interactions.<br>
     * - A call to model.spawnObjects() to handle object creation.<br>
     * - A call to model.levelUp() to check and handle leveling.<br>
     * - A call to refreshAchievements(tick) to handle achievement updating.<br>
     * - A call to renderGame() to draw the current state of the game.<br>
     * @param tick the provided tick
     * @provided
     */
    public void onTick(int tick) {
        model.updateGame(tick); // Update GameObjects
        model.checkCollisions(); // Check for Collisions
        model.spawnObjects(); // Handles new spawns
        model.levelUp(); // Level up when score threshold is met
        refreshAchievements(tick); // Handle achievement updating.
        renderGame(); // Update Visual

        // Check game over
        if (model.checkGameOver()) {
            pauseGame();
            showGameOverWindow();
        }
    }

    /**
     * Updates the player's progress towards achievements on every game tick, and uses
     * the achievementManager to track and update the player's achievements.
     * Progress is a double representing completion percentage, and must be >= 0.0, and <= 1.0.
     *
     * Achievement Progress Calculations:
     * - Survivor achievement: survival time since game start in seconds, mastered at 120 seconds.
     * - Enemy Exterminator achievement: shots hit since game start, mastered at 20 shots.
     * - Sharp Shooter achievement: if shots fired > 10, then result is accuracy / 0.99,
     *   with the maximum result possible being 1; otherwise if shots fired <= 10, result is 0.
     * (This is so that mastery is achieved at accuracy >= 0.99)
     *
     * The AchievementManager stores all new achievements mastered, and then updates the UI
     * statistics with each new achievement's name and progress value.
     * Once every 100 ticks, and only if verbose is true, the achievement progress is logged
     * to the UI.
     *
     * @param tick - the provided tick
     */
    public void refreshAchievements(int tick) {
        handleSurvivorProgress();
        handleEnemyProgress();
        handleShooterProgress();
        if (tick % 100 == 0 && isVerbose) {
            ui.logAchievements(achievementManager.getAchievements());
        }
    }

    /**
     * Updates the progress of the "Survivor" achievement based on elapsed survival time.
     * - Calculates survivor progress as a ratio of elapsed seconds to the mastery threshold.
     * - Caps progress at 1.0 if it exceeds the maximum.
     * - Iterates through achievements to locate "Survivor" and updates its progress accordingly.
     */
    private void handleSurvivorProgress() {
        double survivorProgress =
                (double) model.getStatsTracker().getElapsedSeconds() / MASTERED_SURVIVAL_TIME;
        if (survivorProgress > 1) {
            survivorProgress = 1.0;
        }
        for (Achievement achievement : achievementManager.getAchievements()) {
            if (achievement.getName().equals("Survivor")) {
                achievement.setProgress(survivorProgress);
            }
        }
    }

    /**
     * Updates the progress of the "Enemy Exterminator" achievement based on successful shots hit.
     * - Calculates enemy progress as a ratio of shots hit to the mastery threshold.
     * - Caps progress at 1.0 if it exceeds the maximum.
     * - Iterates through achievements to locate "Enemy Exterminator" and updates its progress
     *   accordingly.
     */
    private void handleEnemyProgress() {
        double enemyProgress =
                (double) model.getStatsTracker().getShotsHit() / MASTERED_ENEMY_SHOTS_NUM;
        if (enemyProgress > 1) {
            enemyProgress = 1.0;
        }
        for (Achievement achievement : achievementManager.getAchievements()) {
            if (achievement.getName().equals("Enemy Exterminator")) {
                achievement.setProgress(enemyProgress);
            }
        }
    }

    /**
     * Updates the progress of the "Sharp Shooter" achievement based on shooting accuracy.
     * - Calculates sharp shooter progress if more than 10 shots have been fired.
     * - Normalises accuracy to a maximum threshold of 0.99 and ensures progress does not exceed 1.0.
     * - Iterates through achievements to locate "Sharp Shooter" and updates its progress accordingly.
     */
    private void handleShooterProgress() {
        double sharpShooterProgress = 0.0;
        if (model.getStatsTracker().getShotsFired() > 10) {
            sharpShooterProgress = Math.min(model.getStatsTracker().getAccuracy() / 0.99, 1.0);
        } else if (model.getStatsTracker().getShotsFired() <= 10) {
            sharpShooterProgress = 0.0;
        }
        if (sharpShooterProgress > 1) {
            sharpShooterProgress = 1.0;
        }
        for (Achievement achievement : achievementManager.getAchievements()) {
            if (achievement.getName().equals("Sharp Shooter")) {
                achievement.setProgress(sharpShooterProgress);
            }
        }
    }

    /**
     * Renders the current game state, including score, health, and ship position.
     * - Uses ui.setStat() to update the "Score", "Health" and "Level" appropriately
     *   with information from the model.
     * - Uses ui.setStat() to update "Time Survived" with (System.currentTimeMillis() - startTime) / 1000 + " seconds"
     * - Renders all SpaceObjects (including the Ship) using a single call to
     *   ui.render().
     */
    public void renderGame() {
        ui.setStat("Score", String.valueOf(model.getShip().getScore()));
        ui.setStat("Health", String.valueOf(model.getShip().getHealth()));
        ui.setStat("Level", String.valueOf(model.getLevel()));
        ui.setStat("Time Survived", (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        model.addObject(model.getShip());
        List<SpaceObject> spaceObjects = model.getSpaceObjects();
        ui.render(spaceObjects);
    }

    /**
     * Handles player input and performs actions such as moving the ship or firing Bullets.
     * Uppercase and lowercase inputs should be treated identically:
     * - For movement keys "W", "A", "S" and "D" the ship should be moved up, left, down,
     * or right respectively, unless the game is paused. The movement should also be logged,
     * provided verbose is true, as follows:
     * "Ship moved to ({model.getShip().getX()}, {model.getShip().getY()})"
     *
     * - For input "F" the fireBullet() method of the Model instance should be called, and
     *   the recordShotFired() method of the PlayerStatsTracker instance should be called.
     * - For input "P" the pauseGame() method should be called.
     * - For all other inputs, the following should be logged, irrespective of the verbose state:
     * "Invalid input. Use W, A, S, D, F, or P."
     * When the game is paused, only un-pausing should be possible. No other action of printing
     * should occur.
     *
     * @param input - the player's input command.
     *
     * @requires input is a single character.
     */
    public void handlePlayerInput(String input) {
        input = input.toUpperCase();
        if (!isPaused) {
            try {
                switch (input) {
                    case "W":
                        handleShipMovement(Direction.UP);
                        break;
                    case "A":
                        handleShipMovement(Direction.LEFT);
                        break;
                    case "S":
                        handleShipMovement(Direction.DOWN);
                        break;
                    case "D":
                        handleShipMovement(Direction.RIGHT);
                        break;
                    case "F":
                        handleFiringBullet();
                        break;
                    case "P":
                        pauseGame();
                        break;
                    default:
                        ui.log("Invalid input. Use W, A, S, D, F, or P.");
                        break;
                }

            } catch (BoundaryExceededException e) {
                System.err.println(e.getMessage());
            }
        } else {
            if (input.equals("P")) {
                pauseGame();
            }
        }
    }

    /**
     * Handles ship movement in the specified direction, logging position changes if verbose mode is enabled.
     * - Retrieves the current X and Y coordinates of the ship.
     * - Moves the ship in the given direction.
     * - If verbose mode is enabled, checks whether the ship's position has changed.
     * - Logs the new position if movement occurred.
     * - Throws a BoundaryExceededException if movement exceeds allowed limits.
     */
    private void handleShipMovement(Direction direction) throws BoundaryExceededException {
        int currentX = model.getShip().getX();
        int currentY = model.getShip().getY();
        model.getShip().move(direction);
        if (isVerbose) {
            if (model.getShip().getX() != currentX
                    || model.getShip().getY() != currentY) {
                ui.log("Core.Ship moved to (" + model.getShip().getX()
                        + ", " + model.getShip().getY() + ")");
            }
        }
    }

    /**
     * Handles firing a bullet and updates shooting statistics.
     * - Fires a bullet using the game model.
     * - Records the shot in the statistics tracker.
     */
    private void handleFiringBullet() {
        model.fireBullet();
        model.getStatsTracker().recordShotFired();
    }

    /**
     * Calls ui.pause() to pause the game until the method is called again.
     * LCalls ui.pause(). Logs "Game paused." or "Game unpaused." as appropriate,
     * after calling ui.pause(), irrespective of verbose state.
     */
    public void pauseGame() {
        ui.pause();
        if (isPaused) {
            isPaused = false;
        } else {
            isPaused = true;
        }
        if (isPaused) {
            ui.log("Game paused.");
        } else {
            ui.log("Game unpaused.");
        }

    }

    /**
     * Displays a Game Over window containing the player's final statistics and achievement
     * progress.<br>
     * <p>
     * This window includes:<br>
     * - Number of shots fired and shots hit<br>
     * - Number of Enemies destroyed<br>
     * - Survival time in seconds<br>
     * - Progress for each achievement, including name, description, completion percentage
     * and current tier<br>
     * @provided
     */
    private void showGameOverWindow() {

        // Create a new window to display game over stats.
        javax.swing.JFrame gameOverFrame = new javax.swing.JFrame("Game Over - Player Stats");
        gameOverFrame.setSize(400, 300);
        gameOverFrame.setLocationRelativeTo(null); // center on screen
        gameOverFrame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);


        StringBuilder sb = new StringBuilder();
        sb.append("Shots Fired: ").append(getStatsTracker().getShotsFired()).append("\n");
        sb.append("Shots Hit: ").append(getStatsTracker().getShotsHit()).append("\n");
        sb.append("Enemies Destroyed: ").append(getStatsTracker().getShotsHit()).append("\n");
        sb.append("Survival Time: ").append(getStatsTracker()
                .getElapsedSeconds()).append(" seconds\n");


        List<Achievement> achievements = achievementManager.getAchievements();
        for (Achievement ach : achievements) {
            double progressPercent = ach.getProgress() * 100;
            sb.append(ach.getName())
                    .append(" - ")
                    .append(ach.getDescription())
                    .append(" (")
                    .append(String.format("%.0f%%", progressPercent))
                    .append(" complete, Tier: ")
                    .append(ach.getCurrentTier())
                    .append(")\n");
        }

        String statsText = sb.toString();

        // Create a text area to show stats.
        javax.swing.JTextArea statsArea = new javax.swing.JTextArea(statsText);
        statsArea.setEditable(false);
        statsArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));

        // Add the text area to a scroll pane (optional) and add it to the frame.
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(statsArea);
        gameOverFrame.add(scrollPane);

        // Make the window visible.
        gameOverFrame.setVisible(true);
    }

}

