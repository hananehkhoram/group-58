package model;

public class TimeManager {
    private long totalTicks;

    public void advanceTime(int ticks){}
    public long getTotalTicks() {
        return totalTicks;
    }
    public int getTotalSeconds(){return (int)(totalTicks / 10);}
    public void reset(){totalTicks = 0;}
}

