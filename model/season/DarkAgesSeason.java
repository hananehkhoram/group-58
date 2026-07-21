package model.season;

import controller.repository.DataManager;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.level.Level;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.List;
import java.util.Random;

public class DarkAgesSeason extends Season{
        private final Random random = new Random();
        private DataManager dm;
        private ZombieFactory zombieFactory;

        private boolean[][] necromancyCells;

        public DarkAgesSeason(List<Level> levels) {
                super("Dark Ages", levels,4);
                this.dm = DataManager.getInstance();
                this.zombieFactory = new ZombieFactory(dm);
        }

        @Override
        public boolean sunFallsFromSky() { return false; }

        @Override
        public void onWaveStart(GameContext ctx, int waveNumber, boolean isLastWave) {
                spawnRandomGraves(ctx);

                triggerNecromancy(ctx);
        }

        @Override
        public void onLevelStart(GameContext ctx) {
                int rows = ctx.getLevel().getRows();
                int cols = ctx.getLevel().getColumns();
                necromancyCells = new boolean[rows][cols];

                for (int i = 0; i < 4; i++) {
                        int r = random.nextInt(rows);
                        int c = 4 + random.nextInt(cols - 4);
                        necromancyCells[r][c] = true;
                }
        }

        private void spawnRandomGraves(GameContext ctx) {
                int rows = ctx.getLevel().getRows();
                int cols = ctx.getLevel().getColumns();

                int gravesToSpawn = 1 + random.nextInt(3);

                for (int i = 0; i < gravesToSpawn; i++) {
                        int r = random.nextInt(rows);
                        int c = 2 + random.nextInt(cols - 2);

                        if (ctx.getPlantGrid()[r][c] == null && ctx.getGraveGrid()[r][c] == null) {

                                Grave.GraveType type = determineRandomGraveType();
                                Grave newGrave = new Grave(type, r, c);

                                ctx.placeGrave(newGrave, r, c);

                                ConsoleView.simplePrint("A new grave of type [" + type + "] has appeared at row " + r + ", col " + c + "!\n");
                        }
                }
        }
        private Grave.GraveType determineRandomGraveType() {
                int chance = random.nextInt(100);
                if (chance < 15) {
                        return Grave.GraveType.HAS_PLANT_FOOD;
                } else if (chance < 40) {
                        return Grave.GraveType.HAS_SUN;
                } else {
                        return Grave.GraveType.NORMAL;
                }
        }

        @Override
        public boolean isNecromancyCell(int row, int col) {
                return necromancyCells != null && row >= 0 && row < necromancyCells.length
                        && col >= 0 && col < necromancyCells[0].length && necromancyCells[row][col];
        }

        private void triggerNecromancy(GameContext ctx) {
                int rows = ctx.getLevel().getRows();
                int cols = ctx.getLevel().getColumns();

                for (int r = 0; r < rows; r++) {
                        for (int c = 0; c < cols; c++) {
                                if (necromancyCells[r][c] && ctx.getGraveGrid()[r][c] != null) {
                                        ConsoleView.simplePrint("Necromancy! A zombie rises from the grave at [" + r + "," + c + "].\n");

                                        Zombie summonedZombie = zombieFactory.create("normal");
                                        summonedZombie.setX(c);
                                        summonedZombie.setY(r);
                                        ctx.addZombie(summonedZombie);
                                }
                        }
                }
        }
}