package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public class PianoCharge implements Behaviors {

    private static final int TICKS_PER_SECOND = 10;
    private float rowSwapIntervalSeconds = 5.0f;

    private boolean rolling = true;
    private double rollSpeed;
    private double normalSpeed;
    private double rollingEatDps;
    private int streetWidth;
    private int streetHeight;
    private Set<String> breakPlants;

    private long lastSwapTick = -1;
    private final Random random = new Random();

    public PianoCharge(double rollSpeed, double normalSpeed, double rollingEatDps,
                       int streetWidth, int streetHeight, List<String> breakPlants) {
        this.rolling = true;
        this.rollSpeed = rollSpeed;
        this.normalSpeed = normalSpeed;
        this.rollingEatDps = rollingEatDps;
        this.streetWidth = streetWidth;
        this.streetHeight = streetHeight;
        this.breakPlants = new HashSet<>(breakPlants);
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        destroyOwnCellPlant(zombie, ctx);

        shuffleZombieRows(zombie, ctx);
    }

    private void destroyOwnCellPlant(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int col = (int) zombie.getX();
        int totalCols = ctx.getPlantGrid()[0].length;

        if (col < 0 || col >= totalCols) return;

        Plant target = ctx.getPlantGrid()[row][col];
        if (target != null && !target.isDead()) {
            target.takeDamage(Integer.MAX_VALUE);
        }
    }

    private void shuffleZombieRows(Zombie selfZombie, GameContext ctx) {
        long now = ctx.getTimeManager().getTotalTicks();
        if (lastSwapTick >= 0 && (now - lastSwapTick) < (long) (rowSwapIntervalSeconds * TICKS_PER_SECOND)) {
            return;
        }

        int totalRows = ctx.getPlantGrid().length;

        for (Zombie z : ctx.getAliveZombies()) {
            if (z == selfZombie) continue;
            int newRow = pickNeighborRow(z.getRow(), totalRows);
            z.setRow(newRow);
            z.setY(newRow);
        }

        lastSwapTick = now;
    }

    private int pickNeighborRow(int row, int totalRows) {
        boolean canGoUp = row > 0;
        boolean canGoDown = row < totalRows - 1;

        if (canGoUp && canGoDown) return random.nextBoolean() ? row - 1 : row + 1;
        if (canGoUp) return row - 1;
        if (canGoDown) return row + 1;

        return row;
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

}