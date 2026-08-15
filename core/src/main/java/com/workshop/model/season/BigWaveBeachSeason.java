package com.workshop.model.season;

import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.GridCell;
import com.workshop.model.level.Level;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.Tag;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BigWaveBeachSeason extends Season{
        private final Random random = new Random();

        // ستونی که در حال حاضر مرز آب است (مثلاً اگر ۵ باشد، یعنی ستون‌های ۵ تا آخر آب هستند)
        private int currentWaterColumn;

        private final Set<GridCell> lowTideCells = new HashSet<>();//شاید زامبی از زیرشون در بیاد

        private static final double LOW_TIDE_ZOMBIE_CHANCE = 0.25;


        public BigWaveBeachSeason(List<Level> levels) {
                super("Big Wave Beach", levels,3);
        }

        @Override
        public Set<GridCell> getWaterCells(GameContext ctx) {
                Set<GridCell> cells = new HashSet<>();
                int rows = ctx.getLevel().getRows();
                int cols = ctx.getLevel().getColumns();
                for (int r = 0; r < rows; r++) {
                        for (int c = currentWaterColumn; c < cols; c++) {
                                cells.add(new GridCell(r, c));
                        }
                }
                return cells;
        }

        @Override
        public void onWaveStart(GameContext ctx, int waveNumber, boolean isLastWave) {
                int totalColumns = ctx.getLevel().getColumns();
                int maxColumns = ctx.getLevel().getMaxWaterColumn();
                int prospectiveWaterColumns = 3 + random.nextInt(Math.max(1, maxColumns - 2));
                int newWaterColumn = totalColumns - prospectiveWaterColumns;
                this.currentWaterColumn = newWaterColumn;
                Console.simplePrint("The tide changes! Water now covers columns from " +
                        currentWaterColumn + " to the right.");

                checkPlantsDrowning(ctx);
                checkLowTideZombies(ctx);
        }

        @Override
        public void onLevelStart(GameContext ctx) {
                this.currentWaterColumn = ctx.getLevel().getColumns() - 3;
                Console.simplePrint("Big Wave Beach started. Water starts at column: " +
                        currentWaterColumn + "\n");
        }

        @Override
        public boolean isWaterCell(int row, int col, GameContext ctx) {
                return col >= this.currentWaterColumn;
        }

        private void checkPlantsDrowning(GameContext ctx) {
                Plant[][] grid = ctx.getPlantGrid();
                int rows = ctx.getLevel().getRows();
                int cols = ctx.getLevel().getColumns();

                for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                                if (isWaterCell(r, c, ctx)) {
                                        Plant plant = grid[r][c];
                                        if (plant != null) {
                                                if (!plant.hasTheTag(Tag.WATER) && !plant.isHasLilyPadUnderneath()) {
                                                        Console.simplePrint(plant.getName() +
                                                                " drowned at [" + r + "," + c + "]!\n");
                                                        grid[r][c] = null;
                                                        ctx.getAlivePlants().remove(plant);
                                                }
                                        }
                                }
                        }
                }
        }
        private void checkLowTideZombies(GameContext ctx) {
                for (GridCell cell : lowTideCells) {
                        boolean underWater = isWaterCell(cell.getRow(), cell.getCol(), ctx);
                        if (!underWater) continue;

                        if (random.nextDouble() < LOW_TIDE_ZOMBIE_CHANCE) {
                                spawnZombieFromLowTide(ctx, cell.getRow(), cell.getCol());
                        }
                }
        }

        private void spawnZombieFromLowTide(GameContext ctx, int row, int col) {
                ZombieFactory zombieFactory = new ZombieFactory(DataManager.getInstance());
                Zombie zombie = zombieFactory.create("Default");

                zombie.setY(row);
                zombie.setX(col);

                ctx.addZombie(zombie);
                Console.simplePrint("A zombie emerges from the low tide at (" + row + ", " + col + ")!\n");
                ctx.announce("Water’s pulling back zombies are coming right out of the sea!");
        }
}
