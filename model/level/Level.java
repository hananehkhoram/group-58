package model.level;


import model.mechanisms.Wave;
import model.plants.Plant;
import model.season.Season;
import model.user.User;

import java.util.List;

public class Level {
    protected String name;
    protected int rows;
    protected int columns;
    protected Wave[] waves;
    protected LevelType levelType;
    protected Season season;

    private List<Plant> conveyorPlantPool;

    public Level(String name, int rows, int columns, Wave[] waves, LevelType levelType, Season season) {
        this.name = name;
        this.rows = rows;
        this.columns = columns;
        this.waves = waves;
        this.levelType = levelType;
        this.season = season;
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
}

