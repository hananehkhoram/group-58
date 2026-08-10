package com.workshop.model.MiniGame.Beghouled;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.mechanisms.Tile;
import com.workshop.model.plants.Plant;

import java.util.*;

public class BeghouledManager {
    private static final int SUN_VALUE = 50;

    /** جدول ارتقا طبق سند: نام گیاه فعلی -> [نام گیاه ارتقایافته، هزینه به خورشید] */
    private final Map<String, Object[]> upgradeTable;

    private GameContext ctx;
    private GameEngine engine;

    private List<String> activePlantTypes;
    private final boolean[][] craterGrid;

    private int targetMatches;
    private int currentMatches;

    private static class MatchInfo {
        private final Set<Integer> matchedCells = new HashSet<>();
        private int combinationCount;
        private int sunUnits;

        public boolean hasMatch() {
            return combinationCount > 0;
        }

        public boolean contains(int row, int col, int cols) {
            return matchedCells.contains(row * cols + col);
        }
    }

    public BeghouledManager(
        GameContext ctx,
        GameEngine engine,
        int targetMatches,
        int levelNumber
    ){
        this.ctx = ctx;
        this.engine = engine;
        this.targetMatches = targetMatches;
        this.currentMatches = 0;

        this.craterGrid =
            new boolean[ctx.getLevel().getRows()]
                [ctx.getLevel().getColumns()];

        this.activePlantTypes =
            new ArrayList<>(getPlantTypesForLevel(levelNumber));

        this.upgradeTable = getUpgradeTableForLevel(levelNumber);
    }

    private List<String> getPlantTypesForLevel(int levelNumber) {
        return switch (levelNumber) {
            case 1 -> List.of(
                "Peashooter",
                "Wall-nut",
                "Puff-shroom",
                "Cabbage-pult",
                "Snow Pea"
            );

            case 2 -> List.of(
                "Peashooter",
                "Wall-nut",
                "Puff-shroom",
                "Melon-pult",
                "Snow Pea"
            );

            case 3 -> List.of(
                "Repeater",
                "Wall-nut",
                "Fume-shroom",
                "Cabbage-pult",
                "Snow Pea"
            );

            default -> throw new IllegalArgumentException(
                "Invalid Beghouled level: " + levelNumber
            );
        };
    }

    public void initBoard() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Random rand = new Random();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!craterGrid[r][c]) {
                    String randomPlant = activePlantTypes.get(rand.nextInt(activePlantTypes.size()));
                    placePlantDirect(
                        r,
                        c,
                        ctx.getPlantFactory().create(randomPlant)
                    );
                }
            }
        }

        while (true) {
            MatchInfo initialMatches = findMatches();

            if (!initialMatches.hasMatch()) {
                break;
            }

            removeMatches(initialMatches);
            applyGravityAndRefill();
        }
        if (!hasPossibleMove()) {
            resetBoard();
        }
    }

    public boolean trySwap(int x1, int y1, int x2, int y2) {
        // بررسی مجاور بودن
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) != 1) return false;

        Tile t1 = engine.getTiles(x1, y1);
        Tile t2 = engine.getTiles(x2, y2);

        if (t1 == null || t2 == null || craterGrid[y1][x1] || craterGrid[y2][x2]) return false;
        if (t1.getPlant() == null || t2.getPlant() == null) return false;

        swapPlants(x1, y1, x2, y2);

        MatchInfo matches = findMatches();

        boolean swapCreatedMatch =
            matches.contains(y1, x1, ctx.getLevel().getColumns())
                || matches.contains(y2, x2, ctx.getLevel().getColumns());

        if (!swapCreatedMatch) {
            swapPlants(x1, y1, x2, y2);
            return false;
        }

        resolveMatches(matches, false);
        applyGravityAndRefill();
        handleCascades();

        checkWinCondition();
        return true;
    }

    /**
     * تبدیل تمام نمونه‌های یک نوع گیاه روی زمین به نوع ارتقایافته، با پرداخت خورشید.
     * مطابق جدول سند (مثلاً peashooter -> repeater با هزینه ۵۰۰).
     */
    public String upgradeAll(String fromPlantName) {
        Object[] upgrade =
            upgradeTable.get(
                fromPlantName.toLowerCase()
            );
        if (upgrade == null) {
            return "No upgrade available for " + fromPlantName + ".";
        }

        String toPlantName = (String) upgrade[0];
        int cost = (int) upgrade[1];

        if (ctx.getSunAmount() < cost) {
            return "Not enough sun! Need " + cost + " sun to upgrade " + fromPlantName + " to " + toPlantName + ".";
        }

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        int upgradedCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Plant p = ctx.getPlantGrid()[r][c];
                if (p != null && p.getName().equalsIgnoreCase(fromPlantName)) {
                    placePlantDirect(
                        r,
                        c,
                        ctx.getPlantFactory().create(toPlantName)
                    );                    upgradedCount++;
                }
            }
        }

        if (upgradedCount == 0) {
            return "No " + fromPlantName + " found on the board to upgrade.";
        }

        for (int i = 0; i < activePlantTypes.size(); i++) {
            if (activePlantTypes.get(i)
                .equalsIgnoreCase(fromPlantName)) {

                activePlantTypes.set(i, toPlantName);
            }
        }

        ctx.setSunAmount(ctx.getSunAmount() - cost);
        return "Upgraded " + upgradedCount + "x " + fromPlantName + " to " + toPlantName + " for " + cost + " sun!";
    }

    public void markCrater(int row, int col) {
        craterGrid[row][col] = true;
    }

    public boolean isCrater(int row, int col) {
        if (row < 0 || row >= craterGrid.length) {
            return false;
        }

        if (col < 0 || col >= craterGrid[row].length) {
            return false;
        }

        return craterGrid[row][col];
    }

    private void swapPlants(
        int x1,
        int y1,
        int x2,
        int y2
    ) {
        Plant[][] grid = ctx.getPlantGrid();

        Plant firstPlant = grid[y1][x1];
        Plant secondPlant = grid[y2][x2];

        grid[y1][x1] = secondPlant;
        grid[y2][x2] = firstPlant;

        if (secondPlant != null) {
            secondPlant.setRow(y1);
            secondPlant.setCol(x1);
        }

        if (firstPlant != null) {
            firstPlant.setRow(y2);
            firstPlant.setCol(x2);
        }
    }

    private void placePlantDirect(
        int row,
        int col,
        Plant plant
    ) {
        Plant oldPlant =
            ctx.getPlantGrid()[row][col];

        if (oldPlant != null && oldPlant != plant) {
            ctx.getAlivePlants().remove(oldPlant);
        }

        ctx.getPlantGrid()[row][col] = plant;

        if (plant != null) {
            plant.setRow(row);
            plant.setCol(col);

            if (!ctx.getAlivePlants().contains(plant)) {
                ctx.getAlivePlants().add(plant);
            }
        }
    }

    private void removePlantDirect(
        int row,
        int col
    ) {
        Plant plant =
            ctx.getPlantGrid()[row][col];

        ctx.getPlantGrid()[row][col] = null;

        if (plant != null) {
            ctx.getAlivePlants().remove(plant);
        }
    }

    private void movePlantDirect(
        int fromRow,
        int fromCol,
        int toRow,
        int toCol
    ) {
        Plant plant =
            ctx.getPlantGrid()[fromRow][fromCol];

        ctx.getPlantGrid()[fromRow][fromCol] = null;
        ctx.getPlantGrid()[toRow][toCol] = plant;

        if (plant != null) {
            plant.setRow(toRow);
            plant.setCol(toCol);
        }
    }

    // یافتن و حذف ترکیب‌ها (بدون دخالت گارد Tile.setPlant)
    private MatchInfo findMatches() {
        MatchInfo result = new MatchInfo();

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Plant[][] grid = ctx.getPlantGrid();

        for (int row = 0; row < rows; row++) {
            int col = 0;

            while (col < cols) {
                Plant plant = grid[row][col];

                if (plant == null) {
                    col++;
                    continue;
                }

                int end = col + 1;

                while (end < cols
                    && grid[row][end] != null
                    && grid[row][end].getName()
                    .equalsIgnoreCase(plant.getName())) {
                    end++;
                }

                int length = end - col;

                if (length >= 3) {
                    result.combinationCount++;
                    result.sunUnits += length - 2;

                    for (int c = col; c < end; c++) {
                        result.matchedCells.add(row * cols + c);
                    }
                }

                col = end;
            }
        }

        for (int col = 0; col < cols; col++) {
            int row = 0;

            while (row < rows) {
                Plant plant = grid[row][col];

                if (plant == null) {
                    row++;
                    continue;
                }

                int end = row + 1;

                while (end < rows
                    && grid[end][col] != null
                    && grid[end][col].getName()
                    .equalsIgnoreCase(plant.getName())) {
                    end++;
                }

                int length = end - row;

                if (length >= 3) {
                    result.combinationCount++;
                    result.sunUnits += length - 2;

                    for (int r = row; r < end; r++) {
                        result.matchedCells.add(r * cols + col);
                    }
                }

                row = end;
            }
        }

        return result;
    }

    private void removeMatches(MatchInfo matches) {
        int cols = ctx.getLevel().getColumns();

        for (int cell : matches.matchedCells) {
            int row = cell / cols;
            int col = cell % cols;

            removePlantDirect(row, col);
        }
    }

    private void resolveMatches(
        MatchInfo matches,
        boolean cascade
    ) {
        int sunUnits = matches.sunUnits;

        if (cascade) {
            sunUnits += matches.combinationCount;
        }

        int gainedSun = sunUnits * SUN_VALUE;

        ctx.setSunAmount(
            ctx.getSunAmount() + gainedSun
        );

        currentMatches += matches.combinationCount;

        removeMatches(matches);
    }

    private void applyGravityAndRefill() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Random rand = new Random();
        Plant[][] grid = ctx.getPlantGrid();

        for (int c = 0; c < cols; c++) {
            for (int r = rows - 1; r >= 0; r--) {
                if (craterGrid[r][c] || grid[r][c] != null) continue;

                boolean foundPlant = false;
                for (int aboveR = r - 1; aboveR >= 0; aboveR--) {
                    if (grid[aboveR][c] != null) {
                        movePlantDirect(
                            aboveR,
                            c,
                            r,
                            c
                        );
                        foundPlant = true;
                        break;
                    }
                }

                if (!foundPlant) {
                    String randomPlant = activePlantTypes.get(rand.nextInt(activePlantTypes.size()));
                    placePlantDirect(
                        r,
                        c,
                        ctx.getPlantFactory().create(randomPlant)
                    );
                }
            }
        }
    }

    private void handleCascades() {
        while (true) {
            MatchInfo matches = findMatches();

            if (!matches.hasMatch()) {
                break;
            }

            resolveMatches(matches, true);
            applyGravityAndRefill();
        }

        checkWinCondition();

        if (!ctx.isGameEnded() && !hasPossibleMove()) {
            resetBoard();
        }
    }

    private void checkWinCondition() {
        if (!ctx.isGameEnded() && currentMatches >= targetMatches) {
            ctx.getAliveZombies().clear();
            ctx.triggerPlayerWin();
        }
    }

    private boolean isMatch(int c1, int r1, int c2, int r2, int c3, int r3) {
        Plant[][] grid = ctx.getPlantGrid();
        Plant p1 = grid[r1][c1];
        Plant p2 = grid[r2][c2];
        Plant p3 = grid[r3][c3];

        if (p1 == null || p2 == null || p3 == null) return false;
        return p1.getName().equals(p2.getName()) && p2.getName().equals(p3.getName());
    }

    private boolean hasPossibleMove() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                if (craterGrid[row][col]
                    || ctx.getPlantGrid()[row][col] == null) {
                    continue;
                }

                if (col + 1 < cols
                    && canSwapCreateMatch(col, row, col + 1, row)) {
                    return true;
                }

                if (row + 1 < rows
                    && canSwapCreateMatch(col, row, col, row + 1)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canSwapCreateMatch(
        int x1,
        int y1,
        int x2,
        int y2
    ) {
        if (craterGrid[y1][x1] || craterGrid[y2][x2]) {
            return false;
        }

        Plant first = ctx.getPlantGrid()[y1][x1];
        Plant second = ctx.getPlantGrid()[y2][x2];

        if (first == null || second == null) {
            return false;
        }

        swapPlants(x1, y1, x2, y2);

        MatchInfo matches = findMatches();

        boolean createsMatch =
            matches.contains(
                y1,
                x1,
                ctx.getLevel().getColumns()
            )
                || matches.contains(
                y2,
                x2,
                ctx.getLevel().getColumns()
            );

        swapPlants(x1, y1, x2, y2);

        return createsMatch;
    }

    private void resetBoard() {
        int attempts = 0;
        int maxAttempts = 100;

        do {
            fillRandomBoard();
            removeResetMatches();
            attempts++;
        } while (!hasPossibleMove() && attempts < maxAttempts);

        System.out.println(
            "No possible moves remained. Beghouled board was reset."
        );
    }

    private void fillRandomBoard() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Random random = new Random();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                removePlantDirect(row, col);

                if (craterGrid[row][col]) {
                    continue;
                }

                String plantName = activePlantTypes.get(
                    random.nextInt(activePlantTypes.size())
                );

                placePlantDirect(
                    row,
                    col,
                    ctx.getPlantFactory().create(plantName)
                );
            }
        }
    }

    private void removeResetMatches() {
        while (true) {
            MatchInfo matches = findMatches();

            if (!matches.hasMatch()) {
                break;
            }

            removeMatches(matches);
            applyGravityAndRefill();
        }
    }

    public int getCurrentMatches() {
        return currentMatches;
    }

    public int getTargetMatches() {
        return targetMatches;
    }

    private Map<String, Object[]> getUpgradeTableForLevel(
        int levelNumber
    ) {
        Map<String, Object[]> upgrades = new HashMap<>();

        switch (levelNumber) {
            case 1 -> {
                upgrades.put(
                    "peashooter",
                    new Object[]{"Repeater", 500}
                );
                upgrades.put(
                    "repeater",
                    new Object[]{"Mega Gatling Pea", 1500}
                );
                upgrades.put(
                    "wall-nut",
                    new Object[]{"Tall-nut", 500}
                );
                upgrades.put(
                    "cabbage-pult",
                    new Object[]{"Melon-pult", 1000}
                );
                upgrades.put(
                    "melon-pult",
                    new Object[]{"Winter Melon", 750}
                );
            }

            case 2 -> {
                upgrades.put(
                    "peashooter",
                    new Object[]{"Repeater", 500}
                );
                upgrades.put(
                    "wall-nut",
                    new Object[]{"Tall-nut", 500}
                );
                upgrades.put(
                    "puff-shroom",
                    new Object[]{"Fume-shroom", 250}
                );
                upgrades.put(
                    "melon-pult",
                    new Object[]{"Winter Melon", 750}
                );
            }

            case 3 -> {
                upgrades.put(
                    "repeater",
                    new Object[]{"Mega Gatling Pea", 1500}
                );
                upgrades.put(
                    "wall-nut",
                    new Object[]{"Tall-nut", 500}
                );
                upgrades.put(
                    "cabbage-pult",
                    new Object[]{"Melon-pult", 1000}
                );
                upgrades.put(
                    "melon-pult",
                    new Object[]{"Winter Melon", 750}
                );
            }

            default -> throw new IllegalArgumentException(
                "Invalid Beghouled level: " + levelNumber
            );
        }

        return upgrades;
    }

}
