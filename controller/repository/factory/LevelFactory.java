package controller.repository.factory;

import controller.repository.DataManager;
import model.level.Level;
import model.level.LevelType;
import model.mechanisms.Wave;
import model.plants.Plant;
import model.season.*;
import model.season.miniGameSeason.wallnutsSeason;

import java.util.ArrayList;
import java.util.List;

public class LevelFactory {

    private static Plant p(String name) {
        return DataManager.getInstance().plants.get(name);
    }

    private static Wave[] generateWaves(int waveCount, int baseCost, int waveDelayTicks) {
        Wave[] waves = new Wave[waveCount];
        double cost = baseCost;
        for (int i = 0; i < waveCount; i++) {
            boolean isLast = (i == waveCount - 1);
            if (isLast) {
                cost *= 2; // ابرموج flag
            }
            waves[i] = new Wave(i + 1,waveDelayTicks, isLast, (int) Math.round(cost));
            if (!isLast) {
                cost *= 1.25;
            }
        }
        return waves;
    }

    // ---------------- Ancient Egypt ----------------
    public static List<Level> buildAncientEgyptLevels() {
        List<Level> levels = new ArrayList<>();

        levels.add(new Level("Ancient Egypt - Day 1", 5, 9,
                generateWaves(5, 100, 250), LevelType.NORMAL, null));

        List<Plant> conveyorPool = List.of(p("Peashooter"), p("Sunflower"), p("Wall-nut"), p("Snow Pea"));
        levels.add(new Level("Ancient Egypt - Day 2", 5, 9,
                generateWaves(6, 120, 200), LevelType.CONVEYOR_BELT, null,
                null, null, conveyorPool, null));

        List<Plant> forced = List.of(p("Peashooter"), p("Sunflower"), p("Wall-nut"));
        Level lockedLevel = new Level("Ancient Egypt - Day 3", 5, 9,
                generateWaves(6, 140, 200), LevelType.LOCKED_PLANTS, null,
                null, forced, null, null);
        levels.add(lockedLevel);

        levels.add(new Level("Ancient Egypt - Boss", 5, 9,
                generateWaves(8, 180, 200), LevelType.BOSS_FIGHT, null));

        return levels;
    }

    // ---------------- Frozen Caves ----------------
    public static List<Level> buildFrozenCaveLevels() {
        List<Level> levels = new ArrayList<>();

        levels.add(new Level("Frozen Caves - Day 1", 5, 9,
                generateWaves(6, 150, 220), LevelType.NORMAL, null));

        Level deadlineLevel = new Level("Frozen Caves - Day 2", 5, 9,
                generateWaves(6, 160, 200), LevelType.DEADLINE, null);
        deadlineLevel.setDeadlineColumn(2);
        levels.add(deadlineLevel);

        Level timedWarLevel = new Level("Frozen Caves - Day 3", 5, 9,
                generateWaves(6, 170, 200), LevelType.TIMED_WAR, null);
        timedWarLevel.setTimedWarDuration(90);
        timedWarLevel.setSunProductionMode(false);
        timedWarLevel.setTimedWarTargetZombies(15);
        levels.add(timedWarLevel);

        levels.add(new Level("Frozen Caves - Boss", 5, 9,
                generateWaves(8, 220, 200), LevelType.BOSS_FIGHT, null));

        return levels;
    }

    public static List<Level> buildBigWaveBeachLevels() {
        List<Level> levels = new ArrayList<>();

        levels.add(new Level("Big Wave Beach - Day 1", 5, 9,
                generateWaves(6, 170, 220), LevelType.NORMAL, null));

        Level saveSeedsLevel = new Level("Big Wave Beach - Day 2", 5, 9,
                generateWaves(6, 180, 200), LevelType.SAVE_QUR_SEEDS, null,
                null, null, null, new ArrayList<>());
        List<Level.PrePlacedPlant> blueprints = new ArrayList<>();
        blueprints.add(new Level.PrePlacedPlant(p("Sunflower"), 0, 4));
        blueprints.add(new Level.PrePlacedPlant(p("Peashooter"), 2, 4));

        saveSeedsLevel = new Level("Big Wave Beach - Day 2", 5, 9,
                generateWaves(6, 180, 200), LevelType.SAVE_QUR_SEEDS, null,
                null, null, null, blueprints);
        levels.add(saveSeedsLevel);

        Level loveYourPlantsLevel = new Level("Big Wave Beach - Day 3", 5, 9,
                generateWaves(7, 190, 180), LevelType.LOVE_YOUR_PLANTS, null);
        loveYourPlantsLevel.setMaxLostPlants(5);
        levels.add(loveYourPlantsLevel);

        levels.add(new Level("Big Wave Beach - Boss", 5, 9,
                generateWaves(8, 240, 180), LevelType.BOSS_FIGHT, null));

        return levels;
    }

    public static List<Level> buildDarkAgesLevels() {
        List<Level> levels = new ArrayList<>();

        levels.add(new Level("Dark Ages - Day 1", 5, 9,
                generateWaves(7, 200, 200), LevelType.NORMAL, null));

        levels.add(new Level("Dark Ages - Day 2", 5, 9,
                generateWaves(7, 210, 180), LevelType.NIGHT_OPS, null));

        Level plantWhatYouGetLevel = new Level("Dark Ages - Day 3", 5, 9,
                generateWaves(8, 220, 150), LevelType.PLANT_WHAT_YOU_GET, null);
        plantWhatYouGetLevel.setSunsGiven(800);
        levels.add(plantWhatYouGetLevel);

        levels.add(new Level("Dark Ages - Boss", 5, 9,
                generateWaves(10, 260, 150), LevelType.BOSS_FIGHT, null));

        return levels;
    }

    // ---------------- MiniGames ----------------
    public static List<Level> buildWallnutsLevels() {
        List<Level> levels = new ArrayList<>();

        levels.add(new Level("Wallnuts - Day 1", 5, 9, generateWaves(5, 200, 220), LevelType.Wallnuts_MG, null));

        levels.add(new Level("Wallnuts - Day 2", 5, 9, generateWaves(6, 220, 210), LevelType.Wallnuts_MG, null));

        levels.add(new Level("Wallnuts - Day 3", 5, 9, generateWaves(7, 240, 200), LevelType.Wallnuts_MG, null));

        for (int i = 0; i < 3; i++){
            Plant wallnutsTemp = new Plant();
            Plant eNutTemp = new Plant();
            Plant bwallnutsTemp = new Plant();

            wallnutsTemp.setName("Wall-nut");
            eNutTemp.setName("Explode-o-nut");
            bwallnutsTemp.setName("B-Wallnut");

            levels.get(i).getConveyorPlantPool().add(wallnutsTemp);
            levels.get(i).getConveyorPlantPool().add(eNutTemp);
            levels.get(i).getConveyorPlantPool().add(bwallnutsTemp);
        }

        return  levels;
    }

    List<Level> egyptLevels = LevelFactory.buildAncientEgyptLevels();
    Season ancientEgypt = new AncientEgypt(egyptLevels);

    List<Level> bigWaveBeachLevels = LevelFactory.buildBigWaveBeachLevels();
    Season bigWaveBeach = new BigWaveBeachSeason(bigWaveBeachLevels);

    List<Level> darkAgesLevels = LevelFactory.buildDarkAgesLevels();
    Season darkAges = new DarkAgesSeason(darkAgesLevels);

    List<Level> frozenCavesLevels = LevelFactory.buildFrozenCaveLevels();
    Season frozenCaves = new FrozenCaveChapter(frozenCavesLevels);

    List<Level> wallnutsLevels = LevelFactory.buildWallnutsLevels();
    Season wallnutsSeason = new wallnutsSeason(wallnutsLevels);

}
