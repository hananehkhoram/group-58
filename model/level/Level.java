package model.level;


import model.mechanisms.Wave;
import model.season.Season;
import model.user.User;

public class Level {
    protected String name;
    protected int rows;
    protected int columns;
    protected Wave[] waves;
    protected LevelType levelType;
    protected Season season;

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



}

