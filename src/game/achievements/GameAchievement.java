package game.achievements;

/**
 * A concrete implementation of the Achievement interface.
 */
public class GameAchievement implements Achievement {
    private final String name;
    private final String description;
    private double progress;

    /**
     * Constructs an Achievement with the specified name and description.
     * The initial progress is 0.
     *
     * @param name - the unique name.
     * @param description - the achievement description.
     *
     * @requires name is not null.
     * @requires name is not empty.
     * @requires description is not null.
     * @requires description is not empty.
     */
    public GameAchievement(String name, String description) {
        this.name = name;
        this.description = description;
        this.progress = 0.0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public void setProgress(double newProgress) {
        this.progress = newProgress;
    }

    @Override
    public String getCurrentTier() {
        if (getProgress() < 0.5) {
            return "Novice";
        } else if (0.5 <= getProgress() && getProgress() < 0.999) {
            return "Expert";
        } else {
            return "Master";
        }
    }
}
