package model.level;


import model.mechanisms.Wave;
import model.season.Season;
import model.user.User;

public class Level {
    protected User user;
    protected String name;
    protected int rows;
    protected int columns;
    protected Wave[] waves;
    protected SpecialLevelType specialLevelType;
    protected Season season;
    protected boolean isLocked;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public SpecialLevelType getSpecialLevelType() {
        return specialLevelType;
    }

    public Season getSeason() {
        return season;
    }

    public void unlock() {
    }

    public boolean isLocked() {
        return isLocked;
    }

    protected enum SpecialLevelType {
        NORMAL,//...
    }
}

