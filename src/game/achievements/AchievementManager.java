package game.achievements;

import java.util.*;

/**
 * GameAchievementManager coordinates achievement updates, file persistence management.
 */
public class AchievementManager {
    private final AchievementFile achievementFile;
    private final List<Achievement> achievements = new ArrayList<>();
    private final Set<String> loggedAchievements = new HashSet<>();

    /**
     * Constructs a GameAchievementManager with the specified AchievementFile.
     *
     * @param achievementFile - the AchievementFile instance to use (non-null).
     * @throws IllegalArgumentException - if achievementFile is null.
     *
     * @requires achievementFile is not null.
     */
    public AchievementManager(AchievementFile achievementFile) {
        if (achievementFile == null) {
            throw new IllegalArgumentException();
        }
        this.achievementFile = achievementFile;
    }

    /**
     * Registers a new achievement.
     *
     * @param achievement - the Achievement to register.
     * @throws IllegalArgumentException - if achievement is already registered.
     *
     * @requires achievementFile is not null.
     */
    public void addAchievement(Achievement achievement) {
        for (Achievement comparingAchievement : achievements) {
            if (comparingAchievement.getName().equals(achievement.getName())) {
                throw new IllegalArgumentException();
            }
        }
        achievements.add(achievement);
    }

    /**
     * Sets the progress of the specified achievement to a given amount.
     *
     * @param achievementName - the name of the achievement.
     * @param absoluteProgressValue - the value the achievement's progress will be set to.
     * @throws IllegalArgumentException - if no achievement is registered under the provided name.
     *
     * @requires achievementName must be a non-null, non-empty string identifying a registered
     * achievement.
     */
    public void updateAchievement(String achievementName, double absoluteProgressValue) {
        for (Achievement achievement : achievements) {
            if (achievement.getName().equals(achievementName)) {
                achievement.setProgress(absoluteProgressValue);
                return;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks all registered achievements. For any achievement that is mastered and has not yet
     * been logged, this method logs the event via AchievementFile, and marks the achievement
     * as logged.
     */
    public void logAchievementMastered() {
        for (Achievement achievement : achievements) {
            if (achievement.getProgress() == 1.0
                    && !loggedAchievements.contains((achievement.getName()))) {
                achievementFile.save(achievement.getName());
                loggedAchievements.add(achievement.getName());
            }
        }
    }

    /**
     * Returns a list of all registered achievements.
     *
     * @return a List of Achievement objects..
     */
    public List<Achievement> getAchievements() {
        return new ArrayList<>(achievements);
    }
}
