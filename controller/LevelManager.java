package controller;

import model.level.LevelData;

import java.util.List;

public class LevelManager {
    private LevelData.Level level;
    private List<LevelData.Level> levels;

    public List<LevelData.Level> getLevels() {
        return levels;
    }
    public void unlockLevel(String id){}
    public boolean isLevelUnlocked(String id){return false;}
    public LevelData.Level getNextLevel(LevelData.Level level){return null;}
}
