package model.level;


import model.mechanisms.Wave;
import model.plants.Plant;
import model.season.Season;
import model.user.User;

import java.util.ArrayList;
import java.util.List;

public class Level {
    public static final int ROWS = 5;
    public static final int COLS = 9;
    protected String name;
    protected int rows;
    protected int columns;
    protected Wave[] waves;
    protected LevelType levelType;
    protected Season season;

    private List<Plant> bannedPlants = new ArrayList<>();
    private List<Plant> forcedPlants = new ArrayList<>();

    private List<Plant> conveyorPlantPool = new ArrayList<>();

    private int maxWaterColumn = 5;

    private List<PrePlacedPlant> saveOurSeedsPlants = new ArrayList<>();

    private double timedWarDuration; // مدت زمان مرحله به ثانیه)
    private int timedWarTargetZombies; // تعداد زامبی که باید کشته شوند
    private int timedWarTargetSun; // تعداد خورشیدی که باید تولید شود
    private boolean isSunProductionMode; // آیا مپ از نوع تولید خورشید است؟

    private int deadlineColumn;

    private int maxLostPlants;

    private int sunsGiven;

    public Level(String name, int rows, int columns, Wave[] waves,
                 LevelType levelType, Season season,List<Plant> bannedPlants,
                 List<Plant> forcedPlants,List<Plant> conveyorPlantPool,
                 List<PrePlacedPlant> saveOurSeedsPlants) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.waves = waves;
        this.levelType = levelType;
        this.season = season;
        this.bannedPlants = bannedPlants;
        if (conveyorPlantPool != null) {
            this.conveyorPlantPool.addAll(conveyorPlantPool);
        }
        this.forcedPlants = forcedPlants;
        this.saveOurSeedsPlants = saveOurSeedsPlants;
    }

    public Level(String name, int rows, int columns, Wave[] waves, LevelType levelType, Season season) {
        this(name, rows, columns, waves, levelType, season, null, null, null,
                null);
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Wave[] getWaves() {
        return waves;
    }

    public Season getSeason() {
        return season;
    }

    public List<Plant> getConveyorPlantPool() {
        return conveyorPlantPool;
    }

    public LevelType getLevelType() {
        return levelType;
    }

    public List<Plant> getBannedPlants() {
        return bannedPlants;
    }

    public List<Plant> getForcedPlants() {
        return forcedPlants;
    }

    public static class PrePlacedPlant {
        private final Plant plantTemplate;
        private final int row;
        private final int col;

        public PrePlacedPlant(Plant plantTemplate, int row, int col) {
            this.plantTemplate = plantTemplate;
            this.row = row;
            this.col = col;
        }

        public Plant getPlantTemplate() { return plantTemplate; }
        public int getRow() { return row; }
        public int getCol() { return col; }
    }

    public List<PrePlacedPlant> getSaveOurSeedsPlants() {
        return saveOurSeedsPlants;
    }

    public double getTimedWarDuration() {
        return timedWarDuration;
    }

    public int getTimedWarTargetZombies() {
        return timedWarTargetZombies;
    }

    public int getTimedWarTargetSun() {
        return timedWarTargetSun;
    }

    public boolean isSunProductionMode() {
        return isSunProductionMode;
    }

    public int getDeadlineColumn() {
        return deadlineColumn;
    }

    public int getMaxLostPlants() {
        return maxLostPlants;
    }

    public int getSunsGiven() {
        return sunsGiven;
    }

    public void setDeadlineColumn(int deadlineColumn) { this.deadlineColumn = deadlineColumn; }
    public void setMaxLostPlants(int maxLostPlants) { this.maxLostPlants = maxLostPlants; }
    public void setSunsGiven(int sunsGiven) { this.sunsGiven = sunsGiven; }
    public void setTimedWarDuration(double d) { this.timedWarDuration = d; }
    public void setTimedWarTargetZombies(int n) { this.timedWarTargetZombies = n; }
    public void setTimedWarTargetSun(int n) { this.timedWarTargetSun = n; }
    public void setSunProductionMode(boolean b) { this.isSunProductionMode = b; }

    public void findCurrentLevelNumber(Level level) {}

    public int getMaxWaterColumn() {
        return maxWaterColumn;
    }
}

