package com.workshop.model.mechanisms;

import com.workshop.controller.MenuManager;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.VaseGame.Vase;
import com.workshop.model.MiniGame.VaseGame.VaseContent;
import com.workshop.model.plants.TargetingMode;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.MiniGame.Izambi.IZombieManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private final GameContext ctx;
    private Tile[][] tiles;
    private LawnMower[] lawnMowers;
    private final Random random = new Random();

    private final WaveSystem waveSystem;
    private final ZombieSystem zombieSystem;
    private final PlantSystem plantSystem;
    private final ProjectileSystem projectileSystem;
    private final LawnMowerSystem lawnMowerSystem;
    private final WinLoseChecker winLoseChecker;

    public GameEngine(GameContext ctx, MenuManager menuManager) {
        this.ctx = ctx;
        this.tiles = Tile.buildTiles(ctx);
        this.lawnMowers = LawnMower.buildLawnMowers();

        this.waveSystem = new WaveSystem(ctx);
        this.zombieSystem = new ZombieSystem(ctx, lawnMowers);
        this.plantSystem = new PlantSystem(ctx, this);
        this.projectileSystem = new ProjectileSystem(ctx);
        this.lawnMowerSystem = new LawnMowerSystem(ctx, lawnMowers);
        this.winLoseChecker = new WinLoseChecker(ctx, menuManager, tiles);
    }

    public GameContext getCtx() {
        return ctx;
    }

    public void update(double deltaTime) {
        if (ctx.isPaused()) {
            return;
        }

        int passedTicks = (int)(deltaTime * 10);
        int rows = this.ctx.getLevel().getRows();
        int columns = this.ctx.getLevel().getColumns();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Tile tile = this.getTiles(j, i);
                if (tile != null && tile.hasDroppedSeed()) {
                    tile.updateSeedTimer(passedTicks);
                }
            }
        }

        if (!ctx.isBattleStarted()) {
            return;
        }

        if (ctx.isGameEnded()) {
            winLoseChecker.check();
            return;
        }

        if (ctx.getLevelManager() != null) {
            ctx.getLevelManager().onUpdate(deltaTime, ctx);
        }
        ctx.getSunManager().update(this);
        waveSystem.update(deltaTime);
        zombieSystem.update(deltaTime);
        if (ctx.getSeason() != null) {
            ctx.getSeason().onTick(ctx, deltaTime);
        }

        if (ctx.isGameEnded()) {
            winLoseChecker.check();
            return;
        }

        if (getIZombieManager() == null) {
            lawnMowerSystem.update(deltaTime);
        }

        plantSystem.update(deltaTime);
        projectileSystem.update(deltaTime);
        winLoseChecker.check();
    }

    public void removePlant(int row, int col) {
        plantSystem.removePlant(row, col);
    }

    public List<Zombie> findTargets(int row, int col, TargetingMode mode) {
        List<Zombie> sameRow = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if (z.occupiesRow(row)) sameRow.add(z);
        }
        switch (mode) {
            case FIRST_IN_LANE -> {
                sameRow.sort((a, b) -> Double.compare(a.getX(), b.getX()));
                return sameRow.isEmpty() ? sameRow : sameRow.subList(0, 1);
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
            case IN_SAME_PLACE -> {
                List<Zombie> inSameTile = new ArrayList<>();
                for (Zombie z : sameRow) {
                    if (Math.floor(z.getX()) == col) {
                        inSameTile.add(z);
                    }
                }
                return inSameTile;
            }
            default -> {
                return sameRow;
            }
        }
    }

    private IZombieManager getIZombieManager() {
        if (
            ctx.getLevelManager()
                instanceof IZombieManager manager
        ) {
            return manager;
        }

        return null;
    }

    public Tile getTiles(int x, int y) {
        if (y < 0 || y >= tiles.length || x < 0 || x >= tiles[0].length) return null;
        return tiles[y][x];
    }

    public void smashVase(int col, int row, GameContext ctx) {
        Tile tile = this.getTiles(col, row);

        if (tile == null){
            com.workshop.view.Console.simplePrint("Invalid coordinates!\n");
            return;
        }

        Vase vase = tile.getVase();

        if (vase == null){
            com.workshop.view.Console.simplePrint("There is no vase at (" +  row + ", " + col + ")!\n");
            return;
        }

        if (vase.isBroken()){
            com.workshop.view.Console.simplePrint("Vase broken!\n");
            return;
        }

        vase.setBroken(true);
        tile.setVase(null); //DebugF
        com.workshop.view.Console.simplePrint("Crash! you smashed the vase at (" +  col + ", " + row + ")!\n");

        if (vase.getContent() == VaseContent.ZOMBIE) {
            String zombieName = vase.getHiddenEntityName();

            if (zombieName == null || zombieName.isEmpty()) {
                zombieName = "Default";
            } else if (zombieName.equalsIgnoreCase("Zombie")) {
                zombieName = "Default";
            }

            try {
                com.workshop.model.zombie.Zombie newZombie = ctx.getZombieFactory().create(zombieName);

                newZombie.setX(col);
                newZombie.setRow(row);
                newZombie.setY(row); //DebugF

                ctx.getAliveZombies().add(newZombie);

                if (zombieName.equalsIgnoreCase("Gargantuar")) {
                    com.workshop.view.Console.simplePrint("ROAR! A Gargantuar emerged from the vase at " +
                        "(" + col + ", " + row + ")!\n");
                } else {
                    com.workshop.view.Console.simplePrint("A " + zombieName + " popped out of the vase at " +
                        "(" + col + ", " + row + ")!\n");
                }

            } catch (IllegalArgumentException e) {
                com.workshop.view.Console.simplePrint("Failed to spawn zombie: " + e.getMessage() + "\n");
            }

        } else if (vase.getContent() == VaseContent.PLANT) {
            tile.setDroppedSeed(vase.getHiddenEntityName(), 100);
            com.workshop.view.Console.simplePrint("A seed packet for " + vase.getHiddenEntityName() + "dropped at" +
                " (" + col + ", " + row + ")!\n");
        }
    }

    public LawnMower[] getLawnMowers() {return lawnMowers;}
}
