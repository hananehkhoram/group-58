package com.workshop.model.mechanisms;

import com.workshop.controller.MenuManager;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.level.LevelType;
import com.workshop.model.zombie.Zombie;

public final class WinLoseChecker {

    private final GameContext ctx;
    private final MenuManager menuManager;
    private final Tile[][] tiles;

    public WinLoseChecker(GameContext ctx, MenuManager menuManager, Tile[][] tiles) {
        this.ctx = ctx;
        this.menuManager = menuManager;
        this.tiles = tiles;
    }

    public void check() {
        if (ctx.isGameEnded()) {
            ctx.setBattleStarted(false);

            LevelType type = ctx.getLevel().getLevelType();

            if (type == LevelType.Wallnuts_MG
                || type == LevelType.Vase_MG
                || type == LevelType.Izambie_MG
                || type == LevelType.Beghouled_MG
                || type == LevelType.Zombotany_MG) {

                menuManager.forceChangeMenu("travelmenu");
            } else {
                menuManager.forceChangeMenu("gamemenu");
            }

            ctx.clearLoots();
            return;
        }

        IZombieManager iZombieManager =
            getIZombieManager();

        if (iZombieManager != null) {
            if (ctx.isExternalWinLossHandling()) {
                // A networked/couch I-Zombie match decides win/lose itself
                // (2-player rules differ from the single-player campaign).
                return;
            }
            if (iZombieManager.areAllBrainsEaten()) {
                ctx.triggerPlayerWin();
            } else if (iZombieManager.shouldPlayerLose(ctx)) {
                ctx.triggerPlayerLoss();
            }

            return;
        }

        if (ctx.getLevel().getLevelType()
            == LevelType.Vase_MG) {

            boolean noZombiesRemain = ctx.getAliveZombies().isEmpty();
            boolean allVasesAreBroken = !hasUnbrokenVases();

            if (noZombiesRemain && allVasesAreBroken) {
                ctx.triggerPlayerWin();
            }

            return;
        }

        if (ctx.getLevel().getLevelType()
            == LevelType.Beghouled_MG) {
            return;
        }

        boolean allSpawned = ctx.isWaveSpawningFinished()
            || (ctx.getLevel().getWaves() != null
            && ctx.getCurrentWaveIndex()
            >= ctx.getLevel().getWaves().length);

        if (allSpawned && !hasRemainingEnemyZombies()) {
            ctx.triggerPlayerWin();
        }
    }

    private boolean hasRemainingEnemyZombies() {
        for (Zombie zombie : ctx.getAliveZombies()) {
            if (zombie == null || zombie.isDead()) {
                continue;
            }
            if (zombie.isInitialFrozenBlock()) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean hasUnbrokenVases() {
        int rows = ctx.getLevel().getRows();
        int columns = ctx.getLevel().getColumns();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Tile tile = getTiles(column, row);

                if (tile != null
                    && tile.getVase() != null
                    && !tile.getVase().isBroken()) {
                    return true;
                }
            }
        }

        return false;
    }

    private Tile getTiles(int x, int y) {
        if (y < 0 || y >= tiles.length || x < 0 || x >= tiles[0].length) return null;
        return tiles[y][x];
    }

    private IZombieManager getIZombieManager() {
        if (ctx.getLevelManager() instanceof IZombieManager manager) {
            return manager;
        }
        return null;
    }
}
