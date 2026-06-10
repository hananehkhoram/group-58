package model.mechanisms;

import model.TimeManager;

import java.util.List;

public class SunManager{
    private TimeManager timeManager;
    private int nextDropTick;
    private List<SunDrop> activeSunDrops;

    public SunManager(TimeManager timeManager) {
        this.timeManager = timeManager;
        manageNextDrop();
    }
    public void update(){}
    private void manageNextDrop(){}
    public void generateRandom(){}
    public boolean collectSun(int x, int y, GameEngine engine) {
        // check activeSunDrops handle radioactive add sun to player
        return false;//if there is nothing to collect
    }
}
