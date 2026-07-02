package model.season;

import controller.repository.DataManager;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class FrozenCaveChapter extends Season {
    private DataManager dm;
    private ZombieFactory zombieFactory;
    private final Random random = new Random();

    // نگهداری جهت زمین‌های لیز: 1 برای پایین، -1 برای بالا، 0 برای بدون لیز
    private int[][] sliders;

    public FrozenCaveChapter(List<Level> levels) {
        super("FrozenCave", levels);
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
    }

    @Override
    public void onWaveStart(GameContext ctx, int waveNumber, boolean isLastWave) {
        ConsoleView.simplePrint("Icy Wind is blowing!\n");
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

            Zombie frozenZombie = zombieFactory.create("normal");
            frozenZombie.setX(randomRow);
            frozenZombie.setY(randomCol);
            //spawn zombie

            frozenZombie.setAsInitialFrozenBlock();

            ctx.addZombie(frozenZombie);
        }

        ConsoleView.simplePrint("Frozen Caves started: Ice sliders and frozen zombies placed!\n");
    }
    private void applyIcyWindToRow(GameContext ctx, int row) {
        Plant[][] grid = ctx.getPlantGrid();
        for (int col = 0; col < ctx.getLevel().getColumns(); col++) {
            Plant p = grid[row][col];
            if (p != null && !p.hasFireTag()) {
                p.increaseFreezeLevel();
            }
        }
    }

    private boolean hasFirePlantInNeighbors(Plant[][] grid, int row, int col, int maxRow, int maxCol) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int r = row + i;
                int c = col + j;
                if (r >= 0 && r < maxRow && c >= 0 && c < maxCol) {
                    Plant neighbor = grid[r][c];
                    if (neighbor != null && neighbor.hasFireTag()) {
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
