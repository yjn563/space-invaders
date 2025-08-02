package game;


import game.achievements.PlayerStatsTracker;
import game.core.*;
import game.utility.Logger;
import game.core.SpaceObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the game information and state. Stores and manipulates the game state.
 */
public class GameModel {
    public static final int GAME_HEIGHT = 20;
    public static final int GAME_WIDTH = 10;
    public static final int START_SPAWN_RATE = 2; // spawn rate (percentage chance per tick)
    public static final int SPAWN_RATE_INCREASE = 5; // Increase spawn rate by 5% per level
    public static final int START_LEVEL = 1; // Starting level value
    public static final int SCORE_THRESHOLD = 100; // Score threshold for leveling
    public static final int ASTEROID_DAMAGE = 10; // The amount of damage an asteroid deals
    public static final int ENEMY_DAMAGE = 20; // The amount of damage an enemy deals
    public static final double ENEMY_SPAWN_RATE = 0.5; // Percentage of asteroid spawn chance
    public static final double POWER_UP_SPAWN_RATE = 0.25; // Percentage of asteroid spawn chance

    private final Random random = new Random(); // ONLY USED IN this.spawnObjects()
    private final List<SpaceObject> spaceObjects; // List of all objects
    private Ship ship; // Core.Ship starts at (5, 10) with 100 health
    private int lvl; // The current game level
    private int spawnRate; // The current game spawn rate
    private Logger wrter; // The Logger reference used for logging.
    private PlayerStatsTracker statsTracker;
    private boolean verbose;

    /**
     * Models a game, storing and modifying data relevant to the game.<br>
     * <p>
     * Logger argument should be a method reference to a .log method such as the UI.log method.<br>
     * Example: Model gameModel = new GameModel(ui::log)<br>
     * <p>
     * - Instantiates an empty list for storing all SpaceObjects (except the ship) that the model needs to track.<br>
     * - Instantiates the game level with the starting level value.<br>
     * - Instantiates the game spawn rate with the starting spawn rate.<br>
     * - Instantiates a new ship.<br>
     * - Stores reference to the given logger.<br>
     * - Stores reference to the given PlayerStatsTracker.<br>
     *
     * @param wrter a functional interface for passing information between classes.
     * @param statsTracker - a PlayerStatsTracker instance to record stats.
     */
    public GameModel(Logger wrter,  PlayerStatsTracker statsTracker) {
        spaceObjects = new ArrayList<>();
        lvl = START_LEVEL;
        spawnRate = START_SPAWN_RATE;
        ship = new Ship();
        this.wrter = wrter;
        this.statsTracker = statsTracker;
    }

    /**
     * Returns the ship instance in the game.
     *
     * @return the current ship instance.
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Returns a list of all SpaceObjects in the game.
     *
     * @return a list of all spaceObjects.
     */
    public List<SpaceObject> getSpaceObjects() {
        return spaceObjects;
    }

    /**
     * Returns the current level.
     *
     * @return the current level.
     */
    public int getLevel() {
        return lvl;
    }

    /**
     * Returns the current player stats tracker.
     *
     * @return the current player stats tracker.
     */
    public PlayerStatsTracker getStatsTracker() {
        return statsTracker;
    }

    /**
     * Adds a SpaceObject to the game.<br>
     * <p>
     * Objects are considered part of the game only when they are tracked by the model.<br>
     *
     * @param object the SpaceObject to be added to the game.
     * @requires object != null.
     */
    public void addObject(SpaceObject object) {
        this.spaceObjects.add(object);
    }

    /**
     * Updates the game state by moving all objects and then removing off-screen objects.<br>
     * <p>
     * Objects should be moved by calling .tick(tick) on each object.<br>
     * The game state is updated by removing out-of-bound objects during the tick.<br>
     *
     * @param tick the tick value passed through to the objects tick() method.
     */
    public void updateGame(int tick) {
        List<SpaceObject> toRemove = new ArrayList<>();
        for (SpaceObject obj : spaceObjects) {
            obj.tick(tick); // Move objects downward
            if (!isInBounds(obj)) {
                toRemove.add(obj);
            }
        }
        spaceObjects.removeAll(toRemove);
    }

    /**
     * Sets verbose state to the provided input.
     *
     * @param verbose - whether to set verbose state to true or false.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Spawns new objects (asteroids, enemies, and power-ups) at random positions.
     * Uses this.random to make EXACTLY 6 calls to random.nextInt() and 1 random.nextBoolean.
     * <p>
     * Random calls should be in the following order:<br>
     * 1. Check if an asteroid should spawn (random.nextInt(100) &lt; spawnRate)<br>
     * 2. If spawning an asteroid, spawn at x-coordinate = random.nextInt(GAME_WIDTH)<br>
     * 3. Check if an enemy should spawn (random.nextInt(100) &lt; spawnRate * ENEMY_SPAWN_RATE)<br>
     * 4. If spawning an enemy, spawn at x-coordinate = random.nextInt(GAME_WIDTH)<br>
     * 5. Check if a power-up should spawn (random.nextInt(100) &lt; spawnRate * POWER_UP_SPAWN_RATE)<br>
     * 6. If spawning a power-up, spawn at x-coordinate = random.nextInt(GAME_WIDTH)<br>
     * 7. If spawning a power-up, spawn a ShieldPowerUp if random.nextBoolean(), else a HealthPowerUp.<br>
     * <p>
     * Failure to match random calls correctly will result in failed tests.<br>
     * <p>
     * Objects spawn at y = 0 (top of the screen).<br>
     * Objects may not spawn if there is a ship or space object at the intended spawn location.<br>
     * This should NOT impact calls to random.<br>
     */
    public void spawnObjects() {
        List<Integer> usedXs = new ArrayList<>();
        spawnAsteroids(usedXs);
        spawnEnemies(usedXs);
        spawnPowerUps(usedXs);
    }

    /**
     * Generates a random x-coordinate within the game width and ensures objects do not
     * spawn on top of each other by making sure it is not present in the given list.
     *
     * @param usedXs - A list of x-coordinates that have an object spawning on them.
     * @return A new unique x-coordinate within the game boundaries.
     */
    private int calcX(List<Integer> usedXs) {
        int x = random.nextInt(GAME_WIDTH);
        while (usedXs.contains(x) || isOccupied(x, usedXs)) {
            x = random.nextInt(GAME_WIDTH);
        }
        usedXs.add(x);
        return x;
    }

    /**
     * Checks whether a given X-coordinate is occupied.
     * - Returns true if the coordinate exists in the usedXs list.
     * - Iterates through space objects to check if any occupy the given X-coordinate at Y = 0.
     * - Returns false if the coordinate is not found in either case.
     */
    private boolean isOccupied(int objectXs, List<Integer> usedXs) {
        if (usedXs.contains(objectXs)) {
            return true;
        }
        for (SpaceObject spaceObject : getSpaceObjects()) {
            if (spaceObject.getX() == objectXs && spaceObject.getY() == 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * Attempts to spawn asteroid at random x-coordinate and ensures no collision with
     * the ship if spawning. Asteroids spawn at the top of the screen with a
     * probability defined by spawnRate.
     *
     * @param usedXs - A list of x-coordinates that have an object spawning on them.
     */
    private void spawnAsteroids(List<Integer> usedXs) {
        // Spawn asteroids with a chance determined by spawnRate
        if (random.nextInt(100) < spawnRate) {
            int x = calcX(usedXs);
            int y = 0; // Spawn at the top of the screen
            if (!isCollidingWithShip(x, y)) {
                spaceObjects.add(new Asteroid(x, y));
            }
        }
    }

    /**
     * Attempts to spawn enemy at random x-coordinate and ensures no collision with
     * the ship if spawning. Asteroids spawn at the top of the screen with a lower
     * probability than asteroids.
     *
     * @param usedXs - A list of x-coordinates that have an object spawning on them.
     */
    private void spawnEnemies(List<Integer> usedXs) {
        // Spawn enemies with a lower chance
        // Half the rate of asteroids
        if (random.nextInt(100) < spawnRate * ENEMY_SPAWN_RATE) {
            int x = calcX(usedXs);
            int y = 0;
            if (!isCollidingWithShip(x, y)) {
                spaceObjects.add(new Enemy(x, y));
            }
        }
    }

    /**
     * Attempts to spawn power-up at random x-coordinate and ensures no collision with
     * the ship if spawning. Power-ups spawn at the top of the screen with a lower
     * probability than enemies. The type of power-up is randomly chosen between
     * ShieldPowerUp and HealthPowerUp.
     *
     * @param usedXs - A list of x-coordinates that have an object spawning on them.
     */
    private void spawnPowerUps(List<Integer> usedXs) {
        // Spawn power-ups with an even lower chance
        // One-fourth the spawn rate of asteroids
        if (random.nextInt(100) < spawnRate * POWER_UP_SPAWN_RATE) {
            int x = calcX(usedXs);
            int y = 0;
            PowerUp powerUp = random.nextBoolean() ? new ShieldPowerUp(x, y) :
                    new HealthPowerUp(x, y);
            if (!isCollidingWithShip(x, y)) {
                spaceObjects.add(powerUp);
            }
        }
    }

    /**
     * Checks if a given position would collide with the ship.
     *
     * @param x the x-coordinate to check.
     * @param y the y-coordinate to check.
     * @return true if the position collides with the ship, false otherwise.
     */
    private boolean isCollidingWithShip(int x, int y) {
        return (ship.getX() == x) && (ship.getY() == y);
    }

    /**
     * If level progression requirements are satisfied, levels up the game by
     * increasing the spawn rate and level number.<br>
     * <p>
     * To level up, the score must not be less than the current level multiplied by the score threshold.<br>
     * To increase the level the spawn rate should increase by SPAWN_RATE_INCREASE, and the level number should increase by 1.<br>
     * If the level is increased, and verbose is set to true, log the following: "Level Up! Welcome
     * to Level {new level}. Spawn rate increased to {new spawn rate}%."<br>
     */
    public void levelUp() {
        if (ship.getScore() < lvl * SCORE_THRESHOLD) {
            return;
        }
        lvl++;
        spawnRate += SPAWN_RATE_INCREASE;
        if (verbose) {
            wrter.log("Level Up! Welcome to Level " + lvl + ". Spawn rate increased to "
                    + spawnRate + "%.");
        }
    }

    /**
     * Fires a bullet from the ship's current position.<br>
     * <p>
     * Creates a new bullet at the coordinates the ship occupies.<br>
     */
    public void fireBullet() {
        int bulletX = ship.getX();
        int bulletY = ship.getY(); // Core.Bullet starts just above the ship
        spaceObjects.add(new Bullet(bulletX, bulletY));
    }

    /**
     * Detects and handles collisions between spaceObjects (Ship and Bullet collisions).<br>
     * Objects are considered to be colliding if they share x and y coordinates.<br>
     * <p>
     * First checks ship collision:
     * - If the ship is colliding with a PowerUp, apply the effect, and if verbose is true,
     * log "PowerUp collected: {obj.render()}"<br>
     * - If the ship is colliding with an Asteroid or Enemy, take the appropriate damage, and if
     * verbose is true, log "Hit by {obj.render()}! Health reduced by {damage_taken}."<br>
     * For any collisions with the ship, the colliding object should be removed.<br>
     * <p>
     * Then check bullet collision:<br>
     * If a bullet collides with an enemy, remove both the enemy and the bullet. No logging required.<br>
     * Also, record the shot hit using recordShotHit() to track successful hits.<br>
     * If a Bullet collides with an Asteroid, remove just the Bullet. No logging required.<br>
     * <p>
     * recordShotHit() is only called when a Bullet successfully hits an Enemy.<br>
     */
    public void checkCollisions() {
        List<SpaceObject> toRemove = new ArrayList<>();
        checkShipCollisions(toRemove);
        checkBulletCollisions(toRemove);
        spaceObjects.removeAll(toRemove); // Remove all collided objects
    }

    /**
     * Checks for collisions between the ship and other space objects, excluding bullets.
     * If a collision occurs, applies appropriate collision effect and logs the collision.
     * Objects that collided with the ship are added to the toRemove list.
     * @param toRemove - A list that stores objects that will be removed after collision
     *                 detection.
     */
    private void checkShipCollisions(List<SpaceObject> toRemove) {
        for (SpaceObject obj : spaceObjects) {
            // Skip checking Ships (No ships should be in this list)
            if (obj instanceof Ship) {
                continue;
            }

            if (isCollidingWithShip(obj.getX(), obj.getY()) && !(obj instanceof Bullet)) {
                applyCollisionEffect(obj);
                toRemove.add(obj);
            }
        }
    }

    /**
     * Applies appropriate collision effect based on type of space object.
     * - Power-ups applies beneficial effects to ship
     * - Asteroids and enemies apply damage to ship.
     * Logs event if verbose is true.
     *
     * @param obj - The space object that collided with the ship.
     */
    private void applyCollisionEffect(SpaceObject obj) {
        switch (obj) {
            case PowerUp powerUp -> {
                powerUp.applyEffect(ship);
                if (verbose) {
                    wrter.log("PowerUp collected: " + obj.render());
                }
            }
            case Asteroid asteroid -> {
                ship.takeDamage(ASTEROID_DAMAGE);
                if (verbose) {
                    wrter.log("Hit by asteroid! Health reduced by "
                            + ASTEROID_DAMAGE + ".");
                }
            }
            case Enemy enemy -> {
                ship.takeDamage(ENEMY_DAMAGE);
                if (verbose) {
                    wrter.log("Hit by enemy! Health reduced by " + ENEMY_DAMAGE + ".");
                }
            }
            default -> {
            }
        }
    }

    /**
     * Checks for collisions between bullets and enemy or asteroid.
     * If a bullet and an enemy or an asteroid have matching coordinates they have collided,
     * and the collision will be handled.
     *
     * @param toRemove - A list that stores objects that will be removed after collision
     *                 detection.
     */
    private void checkBulletCollisions(List<SpaceObject> toRemove) {
        for (SpaceObject obj : spaceObjects) {
            // Check only Bullets
            if (!(obj instanceof Bullet)) {
                continue;
            }
            // Check Bullet collision
            for (SpaceObject other : spaceObjects) {
                // Check only Enemies
                if (!((other instanceof Enemy) || (other instanceof Asteroid))) {
                    continue;
                }
                if ((obj.getX() == other.getX()) && (obj.getY() == other.getY())) {
                    handleBulletCollision(obj, other, toRemove);
                    break;
                }
            }
        }
    }

    /**
     * Handles collision between a bullet and other space object.
     * - Bullet is always added to the toRemove list upon collision.
     * - If other object is enemy it will also be added to the toRemove list,
     *   and shot hit is recorded.
     *
     * @param bullet - The bullet in the collision
     * @param other - The space object hit by the bullet (would be either enemy or asteroid).
     * @param toRemove - A list that stores objects that will be removed after collision
     *                 detection.
     */
    private void handleBulletCollision(SpaceObject bullet, SpaceObject other,
                                       List<SpaceObject> toRemove) {
        toRemove.add(bullet); // Remove bullet
        if (other instanceof Enemy) {
            toRemove.add(other); // Remove enemy
            statsTracker.recordShotHit();
        }
    }

    /**
     * Sets the seed of the Random instance created in the constructor using .setSeed().<br>
     * <p>
     * This method should NEVER be called.
     *
     * @param seed to be set for the Random instance
     * @provided
     */
    public void setRandomSeed(int seed) {
        this.random.setSeed(seed);
    }

    /**
     * Checks if the game is over.
     * The game is considered over if the Ship heath is <= 0.
     *
     * @return true if the Ship health is <= 0, false otherwise
     */
    public boolean checkGameOver() {
        return getShip().getHealth() <= 0;
    }

    /**
     * Checks if the given SpaceObject is inside the game bounds.
     * The SpaceObject is considered outside the game boundaries if they are at:
     * x-coordinate >= GAME_WIDTH,
     * y-coordinate >= GAME_HEIGHT,
     * x-coordinate < 0, or
     * y-coordinate < 0
     *
     * @param spaceObject - the SpaceObject to check
     * @return true if the SpaceObject is in bounds, false otherwise.
     *
     * @requires spaceObject is not Null.
     */
    public static boolean isInBounds(SpaceObject spaceObject) {
        double x = spaceObject.getX();;
        double y = spaceObject.getY();
        if (x >= GAME_WIDTH) {
            return false;
        } else if (x < 0) {
            return false;
        } else if (y >= GAME_HEIGHT) {
            return false;
        } else if (y < 0) {
            return false;
        }
        return true;
    }
}
