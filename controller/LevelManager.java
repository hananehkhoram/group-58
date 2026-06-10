package controller;

import model.level.Level;

import java.util.List;

public class LevelManager {
    private Level level;
    private List<Level> levels;

    public List<Level> getLevels() {
        return levels;
    }
    public void unlockLevel(String id){}
    public boolean isLevelUnlocked(String id){return false;}
    public Level getNextLevel(Level level){return null;}
}
