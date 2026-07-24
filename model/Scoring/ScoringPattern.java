package model.Scoring;


public enum ScoringPattern {

    MULTI_KILL("Multi Kill", "Killing multiple zombies with one shot", 50),

    QUICK_KILL("Quick Kill", "Killing zombies quickly", 20),

    SIMULTANEOUS_KILL("Simultaneous Kill", "Killing zombies at the same time", 40),

    KILL_STREAK("Kill Streak", "Kill chain without losing a plant", 30),

    PRECISION_FINISH("Precision Finish", "Precise final hit", 25);

    private final String title;
    private final String description;
    private final int points;

    ScoringPattern(String title, String description, int points) {
        this.title = title;
        this.description = description;
        this.points = points;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }
}
