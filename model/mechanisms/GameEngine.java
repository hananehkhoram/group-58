package model.mechanisms;

import controller.QuestManager;
import model.GameContext;
import model.projectile.Projectile;
import model.level.Level;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.user.UserManager;
import model.zombie.Zombie;
import model.zombie.behavior.Behaviors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private static final double ZOMBIE_SPAWN_X_OFFSET = 1.0;
    private static final double LOSS_X = 0.0;
    private final GameContext ctx;
    private Tile[][] tiles;
    private LawnMower[] lawnMowers;
    private final Random random = new Random();

    public GameEngine(GameContext ctx) {
        this.ctx = ctx;
        this.tiles = buildTiles(ctx);
        this.lawnMowers = buildLawnMowers();
    }

    public GameContext getCtx() {
        return ctx;
    }

    public void update(double deltaTime) {
        if (!ctx.isBattleStarted() || ctx.isGameEnded()) return;

        if (ctx.getLevelManager() != null) {
            ctx.getLevelManager().onUpdate(deltaTime, ctx);
        }
        ctx.getSunManager().update(this);
        updateWave(deltaTime);
        updateZombies(deltaTime);
        updateLawnMowers(deltaTime);
        updatePlants(deltaTime);
        updateProjectiles(deltaTime);
        checkZombiePlantCollisions(deltaTime);
        checkGameEnd();
    }

    private void updateWave(double deltaTime) {
        if (ctx.getLevel().getLevelType() == model.level.LevelType.PLANT_WHAT_YOU_GET) {
            if (!ctx.isManualStartCommandReceived()) {
                return;
            }
        }

        Wave[] waves = ctx.getLevel().getWaves();
        if (waves == null || waves.length == 0) {
            ctx.setWaveSpawningFinished(true);
            return;
        }

        if (ctx.getCurrentWaveIndex() == 0) {
            spawnWave(waves[0]);
            return;
        }

        Wave previousWave = waves[ctx.getCurrentWaveIndex() - 1];

        if (previousWave.isThresholdReached()) {
            if (ctx.getCurrentWaveIndex() < waves.length) {
                spawnWave(waves[ctx.getCurrentWaveIndex()]);
            } else {
                ctx.setWaveSpawningFinished(true);
            }
        }
    }

    private void spawnWave(Wave wave) {
        wave.start(ctx);
        ctx.incrementWaveIndex();
        ctx.setActiveWaveInProgress(true);
        if (ctx.getCurrentWaveIndex() == 1) {
            ctx.recordFirstWaveStart();
        }
    }

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
                for (Behaviors b : z.getBehaviors().values()) {
                    b.onDeath(z, ctx);
                }
                it.remove();
                ctx.incrementZombieKills();
                QuestManager.progress(UserManager.getInstance().getCurrentUser(),
                        "chapter-hunter-" + ctx.getSeason().getName(), 1);

                if (ctx.getFirstWaveStartTick() != -1) {
                    ctx.recordZombieKillTick();
                }

                LawnMower mowerForThisRow = lawnMowers[(int) z.getY()];
                if (z.getX() < 1.0 && !mowerForThisRow.isAvailable()) {
                    ctx.recordAlmostLostKill();
                }
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
                ctx.recordLawnMowerKill();
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

    public void updateProjectiles(double deltaTime) {
        Iterator<Projectile> it = ctx.getProjectiles().iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(deltaTime);
            if (!p.isActive()) {
                it.remove();
                continue;
            }

            int totalRows = ctx.getPlantGrid().length;
            int totalCols = ctx.getPlantGrid()[0].length;
            if (p.getRow() < 0 || p.getRow() >= totalRows || p.getX() < -1 || p.getX() > totalCols) {
                p.deactivate();
                it.remove();
                continue;
            }

            if (p.isFromZombie()) {
                Plant target = ctx.getPlantGrid()[p.getRow()][(int) p.getX()];
                if (target != null && !target.isDead()) {
                    p.onHit(target);
                    if (!p.isActive()) it.remove();
                }
            } else {
                for (Zombie z : ctx.getAliveZombies()) {
                    if (z.getRow() == p.getRow() && Math.abs(z.getX() - p.getX()) < 0.5) {
                        p.onHit(z);
                        if (!p.isActive()) it.remove();
                        break;
                    }
                }
            }
        }
    }

    private void checkZombiePlantCollisions(double deltaTime) {
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
                int damage = z.consumeEatDamage(deltaTime);
                if (damage > 0) {
                    p.takeDamage(damage);
                }
            } else {
                z.setEating(false);
            }
        }
    }

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
        List<Zombie> sameRow = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if ((int) z.getY() == row) sameRow.add(z);
        }

        switch (mode) {
            case FIRST_IN_LANE -> {
                sameRow.sort((a, b) -> Double.compare(a.getX(), b.getX()));
                return sameRow.isEmpty() ? sameRow : sameRow.subList(0, 1);
            }
            case ALL_IN_ROW -> {
                return sameRow;
            }
            case NEAREST -> {
                List<Zombie> result = new ArrayList<>();
                Zombie nearest = null;
                double bestDist = Double.MAX_VALUE;
                for (Zombie z : ctx.getAliveZombies()) {
                    double dRow = z.getY() - row;
                    double dCol = z.getX() - col;
                    double dist = Math.hypot(dRow, dCol);
                    if (dist < bestDist) {
                        bestDist = dist;
                        nearest = z;
                    }
                }
                if (nearest != null) result.add(nearest);
                return result;
            }
            case RANDOM -> {
                List<Zombie> all = ctx.getAliveZombies();
                List<Zombie> result = new ArrayList<>();
                if (!all.isEmpty()) {
                    result.add(all.get(random.nextInt(all.size())));
                }
                return result;
            }
            default -> {
                return sameRow;
            }
        }
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