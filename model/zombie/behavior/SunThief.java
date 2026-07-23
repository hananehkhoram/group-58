package model.zombie.behavior;

import model.GameContext;
import model.mechanisms.Sun;
import model.zombie.Zombie;

import java.util.ArrayList;

public class SunThief implements Behaviors {

    private static final int TICKS_PER_SECOND = 10;

    private int stolenSuns;
    private int maxClaimedSuns;
    private int rate;
    private int distance;
    private long lastStealTick = -1;

    public SunThief(int maxClaimedSuns, int rate, int distance) {
        this.maxClaimedSuns = maxClaimedSuns;
        this.rate = rate;
        this.distance = distance;
        this.stolenSuns = 0;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (zombie.isDead()){
            onDeath(zombie, ctx);
            return;
        }
        if (!canStealMore()) return;

        long now = ctx.getTimeManager().getTotalTicks();
        if (lastStealTick >= 0 && (now - lastStealTick) < (long) rate * TICKS_PER_SECOND) return;

        for (Sun sun : new ArrayList<>(ctx.getSunManager().getActiveSunDrops())) {
            if (!sun.isOnGround()) continue;

            double dRow = sun.getY() - zombie.getRow();
            double dCol = sun.getX() - zombie.getX();
            if (Math.hypot(dRow, dCol) > distance) continue;

            int amount = ctx.getSunManager().stealSun(sun);
            if (amount > 0) {
                stolenSuns = Math.min(maxClaimedSuns, stolenSuns + amount);
                lastStealTick = now;
                if (!canStealMore()) break;
            }
        }
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }
    @Override
    public void onDeath(Zombie zombie, GameContext ctx) {
        if (stolenSuns > 0) {
            ctx.addSun(stolenSuns);
            stolenSuns = 0;
        }
    }


    public boolean canStealMore() { return stolenSuns < maxClaimedSuns; }
    public int getStolenSuns() { return stolenSuns; }
    public int getMaxClaimedSuns() { return maxClaimedSuns; }
}