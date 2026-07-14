package model.mechanisms;

import model.GameContext;
import model.Projectile.Projectile;
import model.level.Level;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameEngine {
    private static final double ZOMBIE_SPAWN_X_OFFSET = 1.0;
    private static final double LOSS_X = 0.0;
    private final GameContext ctx;
    private Tile[][] tiles;
    private LawnMower[] lawnMowers;

    public GameEngine(GameContext ctx) {
        this.ctx = ctx;
        this.tiles = buildTiles(ctx);
        this.lawnMowers = buildLawnMowers();
    }

    public GameContext getCtx() {
        return ctx;
    }

    public void update(double deltaTime) {
        if (ctx.isGameEnded()) return;

//        ctx.getTimeManager().advanceTime(1);
        ctx.getSunManager().update();
        updateWave(deltaTime);
        updateZombies(deltaTime);
        updateLawnMowers(deltaTime);
        updatePlants(deltaTime);
        updateProjectiles(deltaTime);
        checkZombiePlantCollisions();
        checkGameEnd();
    }

    //Wave

    private void updateWave(double deltaTime) {
        if (ctx.getWaveDurationRemaining() > 0) {
            ctx.decrementWaveDelay();
            return;
        }
        Wave[] waves = ctx.getLevel().getWaves();
        if (waves == null || ctx.getCurrentWaveIndex() >= waves.length) {
            ctx.setWaveSpawningFinished(true);
            return;
        }
        spawnWave(waves[ctx.getCurrentWaveIndex()]);
        ctx.incrementWaveIndex();
        ctx.setWaveDurationRemaining(waves[ctx.getCurrentWaveIndex() - 1].getWaveDelay());
    }

    private void spawnWave(Wave wave) {

    }

    //Zombies

    private void updateZombies(double deltaTime) {
        Iterator<Zombie> it = ctx.getAliveZombies().iterator();
        while (it.hasNext()) {
            Zombie z = it.next();
            z.update(ctx, deltaTime);

            if (z.getX() <= LOSS_X) {
                LawnMower mower = lawnMowers[(int) z.getY()];
                if (!mower.isAvailable()) {
                    ctx.triggerPlayerLoss();
                    return;
                }
                if (!mower.isActivated()) {
                    mower.activate();
                }
            }
            if (z.isDead()) {
                it.remove();
                ctx.incrementWaveIndex();
            }
        }
    }


    private void updateLawnMowers(double deltaTime) {
        for (LawnMower l : lawnMowers) {
            if (!l.isActivated() || !l.isAvailable()) continue;

            for (Zombie z : getRowZombies(l.getRow())) {
                l.trigger(z);
            }
            l.advance(deltaTime);

            if (l.isDidKilled()) {
                ctx.incrementZombieKills();
                l.setDidKilled(false);
            }
        }
    }

    public boolean addZombie(Zombie z, int row) {
        if (row < 0 || row >= Level.ROWS) {
            return false;
        }
        z.setX(Level.COLS - 1 + ZOMBIE_SPAWN_X_OFFSET);
        z.setY(row);
        ctx.getAliveZombies().add(z);
        return true;
    }

    public Zombie[] getRowZombies(int row) {
        return ctx.getAliveZombies().stream()
                .filter(z -> z.getY() == row)
                .toArray(Zombie[]::new);
    }

    // Plants
    private void updatePlants(double deltaTime) {
        Iterator<Plant> it = ctx.getAlivePlants().iterator();
        while (it.hasNext()) {
            Plant p = it.next();
            p.update(ctx);
            if (p.getHp() <= 0) {
                ctx.getPlantGrid()[p.getRow()][p.getCol()] = null;
                it.remove();
                ctx.incrementPlantsLost();
            }
        }
    }

    public boolean canPlacePlant(Plant p, int row, int col) {
        if (row < 0 || row >= Level.ROWS) {
            return false;
        }
        if (col < 0 || col >= Level.COLS) {
            return false;
        }
        return ctx.getPlantGrid()[row][col] == null;
    }

    public void removePlant(int row, int col) {
        Plant p = ctx.getPlantGrid()[row][col];
        if (p != null) {
            ctx.getPlantGrid()[row][col] = null;
            ctx.getAlivePlants().remove(p);
        }
    }

    // Projectiles
    public void updateProjectiles(double deltaTime) {
        Iterator<Projectile> it = ctx.getProjectiles().iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(deltaTime);
            if (!p.isActive()) {
                it.remove();
                continue;
            }

            for (Zombie z : ctx.getAliveZombies()) {
                if (z.getY() == p.getRow() && Math.abs(z.getX() - p.getX()) < 0.5) {
                    p.onHit(z);
                    z.takeDamage(p.getDamage());
                    it.remove();
                    break;
                }
            }
        }
    }

    private void checkZombiePlantCollisions() {
        for (Zombie z : ctx.getAliveZombies()) {
            int col = (int) z.getX();
            int row = (int) z.getY();

            if (row < 0 || row >= Level.ROWS
                    || col < 0 || col >= Level.COLS) {
                z.setEating(false);
                continue;
            }
            Plant p = ctx.getPlantGrid()[row][col];
            if (p != null && p.getHp() > 0) {
                z.setEating(true);
                p.takeDamage((int) z.getEatDps());
            } else {
                z.setEating(false);
            }
        }
    }

    // Game End
    private void checkGameEnd() {
        if (ctx.isGameEnded()) {
            return;
        }
        boolean allSpawned = ctx.isWaveSpawningFinished() ||
                (ctx.getLevel().getWaves() != null && ctx.getCurrentWaveIndex() >= ctx.getLevel().getWaves().length);
        if (allSpawned && ctx.getAliveZombies().isEmpty()) {
            ctx.triggerPlayerWin();
        }
    }

    public List<Zombie> findTargets(int row, int col, TargetingMode mode) {
        List<Zombie> targets = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if ((int) z.getY() == row) targets.add(z);
        }
        switch (mode) {
            case FIRST_IN_LANE -> {
                targets.sort((a, b) -> Double.compare(a.getX(), b.getX()));
                return targets.isEmpty() ? targets : targets.subList(0, 1);
            }
            case ALL_IN_ROW -> {
            }
            default -> {
                return targets;
            }
        }
        return targets;
    }

    private Tile[][] buildTiles(GameContext ctx) {
        Tile[][] grid = new Tile[Level.ROWS][Level.COLS];
        for (int r = 0; r < Level.ROWS; r++) {
            for (int c = 0; c < Level.COLS; c++) {
                grid[r][c] = new Tile(r, c, ctx);
            }
        }
        return grid;
    }

    private LawnMower[] buildLawnMowers() {
        LawnMower[] mowers = new LawnMower[Level.ROWS];
        for (int r = 0; r < Level.ROWS; r++) {
            mowers[r] = new LawnMower(r);
        }
        return mowers;
    }

    public Tile getTiles(int x, int y) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) return null;
        return tiles[x][y];
    }

    public Zombie[] getRowsZombies(int row) {
        return null;
    }
}