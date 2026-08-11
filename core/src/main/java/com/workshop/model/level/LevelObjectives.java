package com.workshop.model.level;

import com.workshop.model.plants.Plant;

import java.util.List;
import java.util.stream.Collectors;


public final class LevelObjectives {

    private LevelObjectives() {}

    public static String describe(Level level) {
        LevelType type = level.getLevelType();
        if (type == null) type = LevelType.NORMAL;

        switch (type) {
            case NORMAL:
                return "Don't let the zombies reach your house!";

            case CONVEYOR_BELT:
                return "Plants arrive one at a time on the conveyor belt — grab each one before "
                    + "the zombies get too close, and don't let any zombie reach your house!";

            case DEADLINE:
                return "Don't let a single zombie cross the line at column "
                    + level.getDeadlineColumn() + "!";

            case LOCKED_PLANTS:
                String banned = plantNames(level.getBannedPlants());
                return banned.isEmpty()
                    ? "Some of your plants are locked away for this level — make do with what's left!"
                    : "These plants are locked for this level: " + banned
                      + ". Don't let the zombies reach your house!";

            case LOVE_YOUR_PLANTS:
                return "Protect your plants — you can only afford to lose "
                    + level.getMaxLostPlants() + " of them this level!";

            case NIGHT_OPS:
                return "It's nighttime — mushrooms are available but need sun collected first. "
                    + "Don't let the zombies reach your house!";

            case PLANT_WHAT_YOU_GET:
                String forced = plantNames(level.getForcedPlants());
                return forced.isEmpty()
                    ? "You can only plant what you're given this level — plant it wisely!"
                    : "You'll only have access to: " + forced
                      + ". Plant wisely and don't let the zombies reach your house!";

            case SAVE_QUR_SEEDS:
                return "Protect the pre-placed plants already on the lawn until the wave ends!";

            case TIMED_WAR:
                if (level.isSunProductionMode()) {
                    return "Produce " + level.getTimedWarTargetSun() + " sun within "
                        + formatSeconds(level.getTimedWarDuration()) + "!";
                }
                return "Survive and defeat " + level.getTimedWarTargetZombies() + " zombies within "
                    + formatSeconds(level.getTimedWarDuration()) + "!";

            case BOSS_FIGHT:
                return "Defeat the boss zombie before it's too late!";

            default:
                return "Don't let the zombies reach your house!";
        }
    }

    private static String plantNames(List<Plant> plants) {
        if (plants == null || plants.isEmpty()) return "";
        return plants.stream().map(Plant::getName).collect(Collectors.joining(", "));
    }

    private static String formatSeconds(double seconds) {
        int totalSeconds = (int) Math.round(seconds);
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        if (minutes == 0) return secs + "s";
        return minutes + "m " + secs + "s";
    }
}
