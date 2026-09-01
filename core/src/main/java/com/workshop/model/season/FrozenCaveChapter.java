package com.workshop.model.season;

import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.TerrainType;
import com.workshop.model.mechanisms.Tile;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.Tag;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.Jumper;
import com.workshop.view.Console;

import java.util.List;
import java.util.Random;

public class FrozenCaveChapter extends Season {
    private DataManager dm;
    private ZombieFactory zombieFactory;
    private final Random random = new Random();

    private int[][] sliders;

    public FrozenCaveChapter(List<Level> levels) {
        super("FrozenCave", levels, 2);
        this.dm = DataManager.getInstance();
        this.zombieFactory = new ZombieFactory(dm);
    }

    @Override
    public boolean iceEffectiveOnZombies() { return false; }

    @Override
    public void onTick(GameContext ctx, double deltaTime) {
        Plant[][] grid = ctx.getPlantGrid();
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Plant p = grid[r][c];
                if (p != null && p.getFreezeLevel() == 3) {
                    if (hasFirePlantInNeighbors(grid, r, c, rows, cols)) {
                        p.damageIce(60 * deltaTime);
                    }
                }
            }
        }

        for (Zombie zombie : ctx.getAliveZombies()) {
            if (zombie != null && !zombie.isDead() && zombie.isInitialFrozenBlock()) {
                int zCol = (int) Math.floor(zombie.getX());
                int zRow = zombie.getRow();

                if (zRow >= 0 && zRow < rows && zCol >= 0 && zCol < cols) {
                    if (hasFirePlantInNeighborsOrSameTile(grid, zRow, zCol, rows, cols)) {
                        zombie.meltIce(60 * deltaTime);
                    }
                }
            }
        }

        slideZombiesOnIce(ctx);
    }

    private void slideZombiesOnIce(GameContext ctx) {
        if (sliders == null) {
            return;
        }

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();

        for (Zombie zombie : ctx.getAliveZombies()) {
            if (zombie == null
                || zombie.isDead()
                || zombie.isIced()
                || zombie.isInitialFrozenBlock()) {
                continue;
            }

            Jumper jumper = zombie.getJumper();
            if (jumper != null && !jumper.isLanded()) {
                continue;
            }

            int col = (int) Math.floor(zombie.getX());
            int row = zombie.getRow();
            if (col < 0 || col >= cols || row < 0 || row >= rows) {
                continue;
            }

            int nextRow = getSliderNextRow(row, col);
            if (nextRow == row || nextRow < 0 || nextRow >= rows) {
                continue;
            }

            zombie.setY(nextRow);
            zombie.setEating(false);
        }
    }

    @Override
    public void onWaveStart(GameContext ctx, int waveNumber, boolean isLastWave) {
        Console.simplePrint("Icy Wind is blowing!\n");
        int rows = ctx.getLevel().getRows();

        int rowToHit1 = random.nextInt(rows);
        int rowToHit2 = random.nextInt(rows);

        applyIcyWindToRow(ctx, rowToHit1);
        if (rowToHit1 != rowToHit2) {
            applyIcyWindToRow(ctx, rowToHit2);
        }
    }

    @Override
    public void onLevelStart(GameContext ctx) {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        sliders = new int[rows][cols];

        sliders[2][4] = -1;
        sliders[3][5] = 1;

        int frozenZombiesCount = 3;

        for (int i = 0; i < frozenZombiesCount; i++) {
            int randomRow = random.nextInt(rows);

            int minCol = Math.max(5, cols / 2);
            int randomCol = minCol + random.nextInt(cols - minCol);

            Zombie frozenZombie = zombieFactory.create("Default");
            frozenZombie.setX(randomCol);
            frozenZombie.setY(randomRow);

            frozenZombie.setAsInitialFrozenBlock();

            ctx.addZombie(frozenZombie);
        }

        Console.simplePrint("Frozen Caves started: Ice sliders and frozen zombies placed!\n");
    }

    public void applyIcyWindToRow(GameContext ctx, int row) {
        ctx.announceWindRow(row);

        Plant[][] grid = ctx.getPlantGrid();
        for (int col = 0; col < ctx.getLevel().getColumns(); col++) {
            Plant p = grid[row][col];
            if (p != null && !p.hasTheTag(Tag.FIRE)) {
                p.increaseFreezeLevel();
            }
        }
    }

    private boolean hasFirePlantInNeighbors(Plant[][] grid, int row, int col,
                                            int maxRow, int maxCol) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int r = row + i;
                int c = col + j;
                if (r >= 0 && r < maxRow && c >= 0 && c < maxCol) {
                    Plant neighbor = grid[r][c];
                    if (neighbor != null && neighbor.hasTheTag(Tag.FIRE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasFirePlantInNeighborsOrSameTile(Plant[][] grid, int row, int col,
                                                      int maxRow, int maxCol) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int r = row + i;
                int c = col + j;
                if (r >= 0 && r < maxRow && c >= 0 && c < maxCol) {
                    Plant neighbor = grid[r][c];
                    if (neighbor != null && neighbor.hasTheTag(Tag.FIRE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int getSliderNextRow(int row, int col) {
        if (sliders != null && row >= 0 && row < sliders.length && col >= 0 && col < sliders[0].length) {
            int direction = sliders[row][col];
            return row + direction;
        }
        return row;
    }
}
