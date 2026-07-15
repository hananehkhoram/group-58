package model.season;

import model.GameContext;
import model.GridCell;
import model.level.Level;
import model.plants.Plant;
import view.ConsoleView;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class BigWaveBeachSeason extends Season{
        private final Random random = new Random();

        // ستونی که در حال حاضر مرز آب است (مثلاً اگر ۵ باشد، یعنی ستون‌های ۵ تا آخر آب هستند)
        private int currentWaterColumn;

        //private final int maxWaterLimitColumn;

        public BigWaveBeachSeason(List<Level> levels) {
                super("Big Wave Beach", levels,3);
        }

        @Override
        public Set<GridCell> getWaterCells(GameContext ctx) {return null;}

        @Override
        public void onWaveStart(GameContext ctx, int waveNumber, boolean isLastWave) {
                int totalColumns = ctx.getLevel().getColumns();
                int prospectiveWaterColumns = 3 + random.nextInt(3);

                int newWaterColumn = totalColumns - prospectiveWaterColumns;
                this.currentWaterColumn = newWaterColumn;
                ConsoleView.simplePrint("The tide changes! Water now covers columns from " + currentWaterColumn + " to the right.");

                checkPlantsDrowning(ctx);
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
                                                if (!plant.hasWaterTag() && !plant.isHasLilyPadUnderneath()) {
                                                        ConsoleView.simplePrint(plant.getName() + " drowned at [" + r + "," + c + "]!\n");
                                                        grid[r][c] = null;
                                                        ctx.getAlivePlants().remove(plant);
                                                }
                                        }
                                }
                        }
                }
        }
}
