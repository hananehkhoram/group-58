package model.zombie.behavior;

import model.zombie.Zombie;

public class SunThief implements Behaviors {

    private int stolenSuns;
    private int maxClaimedSuns;  // ZombieRa: 250
    private int rate;
    private int distance;

    public SunThief(int maxClaimedSuns, int rate, int distance) {
        this.maxClaimedSuns = maxClaimedSuns;
        this.rate = rate;
        this.distance = distance;
        this.stolenSuns = 0;
    }

    @Override
    public void onTick(Zombie zombie) {
        // steal sun currency from nearby suns on the ground
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public void giveBackSuns() {
        // called on death: drop stolenSuns back on board
        stolenSuns = 0;
    }

    public boolean canStealMore() { return stolenSuns < maxClaimedSuns; }
    public int getStolenSuns() { return stolenSuns; }
    public int getMaxClaimedSuns() { return maxClaimedSuns; }
}
