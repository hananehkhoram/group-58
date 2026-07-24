package model.season;

import controller.repository.DataManager;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.GridCell;
import model.level.Level;
import model.plants.Plant;
import model.plants.Tag;
import model.zombie.Zombie;
import view.ConsoleView;

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

        public void addLowTideCell(int row, int col) {
                lowTideCells.add(new GridCell(row, col));
        }

        public boolean isLowTideCell(int row, int col) {
                return lowTideCells.contains(new GridCell(row, col));
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
                ConsoleView.simplePrint("The tide changes! Water now covers columns from " +
                        currentWaterColumn + " to the right.");

                checkPlantsDrowning(ctx);
                checkLowTideZombies(ctx);
        }

        @Override
        public void onLevelStart(GameContext ctx) {
                this.currentWaterColumn = ctx.getLevel().getColumns() - 3;
                ConsoleView.simplePrint("Big Wave Beach started. Water starts at column: " +
                        currentWaterColumn + "\n");
        }

        private void updateWaterLevel(GameContext ctx, int newLevel) {
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
                                                        ConsoleView.simplePrint(plant.getName() + " drowned at [" + r + "," + c + "]!\n");
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
                ConsoleView.simplePrint("A zombie emerges from the low tide at (" + row + ", " + col + ")!\n");
        }
}
