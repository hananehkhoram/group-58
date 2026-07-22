package model.mechanisms;

import controller.MenuManager;
import model.GameContext;
import model.MiniGame.VaseGame.Vase;
import model.MiniGame.VaseGame.VaseContent;
import model.projectile.Projectile;
import model.level.Level;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.zombie.Zombie;
import model.zombie.behavior.Behaviors;
import model.zombie.behavior.ProjectileDeflector;
import model.zombie.behavior.Submerge;
import view.ConsoleView;

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
    private MenuManager menuManager;
    private int waveTimer = 0;
    private boolean isFirstWaveTimerSet = false;

    public GameEngine(GameContext ctx, MenuManager menuManager) {
        this.ctx = ctx;
        this.tiles = buildTiles(ctx);
        this.lawnMowers = buildLawnMowers();
        this.menuManager = menuManager;
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

        // اسپاون موج اول
        if (ctx.getCurrentWaveIndex() == 0) {
            spawnWave(waves[0]);
            return;
        }

        // اگر تمام موج‌ها اسپاون شده‌اند، نیازی به بررسی بیشتر نیست
        if (ctx.getCurrentWaveIndex() >= waves.length) {
            ctx.setWaveSpawningFinished(true);
            return;
        }

        // اگر موج فعلی در حال شمارش معکوس/تاخیر برای شروع است
        if (waveTimer > 0) {
            waveTimer -= deltaTime; // یا براساس تیک: waveTimer--
            return;
        }

        Wave previousWave = waves[ctx.getCurrentWaveIndex() - 1];

        // چک کردن شرط رسیدن جان زامبی‌ها به حد نصاب
        if (previousWave.isThresholdReached()) {
            Wave nextWave = waves[ctx.getCurrentWaveIndex()];

            // اگر موج جدید تاخیر دارد، تایمر را تنظیم می‌کنیم تا بلافاصله اسپاون نشود
            if (!isFirstWaveTimerSet && nextWave.getWaveDelay() > 0) {
                this.waveTimer = nextWave.getWaveDelay();
                this.isFirstWaveTimerSet = true;
                return; // منتظر می‌مانیم تا waveTimer صفر شود
            }

            // اسپاون موج بعدی پس از پایان تاخیر
            spawnWave(nextWave);
            this.isFirstWaveTimerSet = false; // ریست کردن برای موج بعدی
        }
    }
    private void spawnWave(Wave wave) {
        wave.start(ctx);
        ctx.incrementWaveIndex();
        ctx.setActiveWaveInProgress(true);
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
                        ProjectileDeflector deflector = z.getDeflector();
                        Submerge submerge = z.getSubmerge();

                        if (deflector != null && deflector.canDeflect(p)) {
                            deflector.deflect(p, ctx, z);
                            it.remove();
                        } else if (submerge != null
                                && !submerge.isVulnerableTo(p.getOwnerPlant().getName(),p.getOwnerPlant().isPlantFoodActive())) {
                            // زیر آبه و این پرتابه (طبق لیست damageWhileSubmerged) بهش نمی‌رسه؛
                            // بدون اثر رد میشه، نه نابود میشه و نه دمیجی میزنه
                        } else {
                            p.onHit(z);
                            if (!p.isActive()) it.remove();
                        }
                        break;
                    }
                }
            }
        }
    }



    private void checkGameEnd() {
        if (ctx.isGameEnded()) {
            ConsoleView.showMessage("You are now in Game Menu.");
            menuManager.forceChangeMenu("gamemenu");
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
                // برخلاف FIRST_IN_LANE، اینجا محدود به سطر خودِ گیاه نیست — نزدیک‌ترین زامبی در کل صفحه
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
                // یک زامبی تصادفی از کل صفحه (نه فقط همون سطر)
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

    public void smashVase(int row, int col) {
        Tile tile = this.getTiles(row, col);

        if (tile == null){
            view.ConsoleView.simplePrint("Invalid coordinates!");
            return;
        }

        Vase vase = tile.getVase();

        if (vase == null){
            view.ConsoleView.simplePrint("There is no vase at (" +  row + ", " + col + ")!");
            return;
        }

        if (vase.isBroken()){
            view.ConsoleView.simplePrint("Vase broken!");
            return;
        }

        vase.setBroken(true);
        view.ConsoleView.simplePrint("Crash! you smashed the vase at (" +  row + ", " + col + ")!");

        if (vase.getContent() == VaseContent.ZOMBIE){
            //spawn zombie
            view.ConsoleView.simplePrint("Zombie popped out!");
        } else if (vase.getContent() == VaseContent.PLANT) {
            view.ConsoleView.simplePrint("Plant seed popped!");
            // plant plant
        }
    }

    public Zombie[] getRowsZombies(int row) {
        return null;
    }

    public LawnMower[] getLawnMowers() {return lawnMowers;}

}