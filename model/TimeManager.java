package model;

public class TimeManager {
    private long totalTicks;
    private long currentTick;

    public void advanceTime(int ticks){
        totalTicks += ticks;
        currentTick = ticks;
    }
    public long getTotalTicks() {
        return totalTicks;
    }
    public int getTotalSeconds(){return (int)(totalTicks / 10);}
    public void reset(){totalTicks = 0;}

    public long getCurrentTick() {
        return currentTick;
    }
}

