package model.MiniGame.Beghouled;

import model.GameContext;
import model.mechanisms.GameEngine;
import model.mechanisms.Tile;
import model.plants.Plant;
import view.ConsoleView;

import java.util.*;

public class BeghouledManager {
    private static final int SUN_PER_MATCH = 50;

    /** جدول ارتقا طبق سند: نام گیاه فعلی -> [نام گیاه ارتقایافته، هزینه به خورشید] */
    private static final Map<String, Object[]> UPGRADE_TABLE = new HashMap<>();
    static {
        UPGRADE_TABLE.put("peashooter", new Object[]{"Repeater", 500});
        UPGRADE_TABLE.put("repeater", new Object[]{"Mega Gatling Pea", 500});
        UPGRADE_TABLE.put("wall-nut", new Object[]{"Tall-nut", 500});
        UPGRADE_TABLE.put("puff-shroom", new Object[]{"Fume-shroom", 250});
        UPGRADE_TABLE.put("cabbage-pult", new Object[]{"Melon-pult", 1000});
        UPGRADE_TABLE.put("melon-pult", new Object[]{"Winter Melon", 750});
    }

    private GameContext ctx;
    private GameEngine engine;

    private List<String> activePlantTypes;
    private final boolean[][] craterGrid;

    private int targetMatches;
    private int currentMatches;

    public BeghouledManager(GameContext ctx, GameEngine engine, int targetMatches) {
        this.ctx = ctx;
        this.engine = engine;
        this.targetMatches = targetMatches;
        this.currentMatches = 0;
        this.craterGrid = new boolean[ctx.getLevel().getRows()][ctx.getLevel().getColumns()];

        this.activePlantTypes = new ArrayList<>(Arrays.asList(
                "Peashooter", "Wall-nut", "Puff-shroom", "Cabbage-pult", "Snow Pea"
        ));
    }

    public void initBoard() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Random rand = new Random();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!craterGrid[r][c]) {
                    String randomPlant = activePlantTypes.get(rand.nextInt(activePlantTypes.size()));
                    setPlantDirect(r, c, ctx.getPlantFactory().create(randomPlant));
                }
            }
        }

        while (checkAndRemoveMatches() > 0) {
            applyGravityAndRefill();
        }
    }

    public boolean trySwap(int x1, int y1, int x2, int y2) {
        // بررسی مجاور بودن
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) != 1) return false;

        Tile t1 = engine.getTiles(x1, y1);
        Tile t2 = engine.getTiles(x2, y2);

        if (t1 == null || t2 == null || craterGrid[y1][x1] || craterGrid[y2][x2]) return false;
        if (t1.getPlant() == null || t2.getPlant() == null) return false;

        // جابه‌جایی موقت
        swapPlants(x1, y1, x2, y2);

        // اگر ترکیب ۳تایی ایجاد شد
        if (hasAnyMatch()) {
            checkAndRemoveMatches();
            ctx.setSunAmount(ctx.getSunAmount() + SUN_PER_MATCH);
            currentMatches++;
            applyGravityAndRefill();
            handleCascades();

            checkWinCondition();
            return true;
        } else {
            // برگردوندن جابجایی چون ترکیبی ایجاد نشد
            swapPlants(x1, y1, x2, y2);
            return false;
        }
    }

    /**
     * تبدیل تمام نمونه‌های یک نوع گیاه روی زمین به نوع ارتقایافته، با پرداخت خورشید.
     * مطابق جدول سند (مثلاً peashooter -> repeater با هزینه ۵۰۰).
     */
    public String upgradeAll(String fromPlantName) {
        Object[] upgrade = UPGRADE_TABLE.get(fromPlantName.toLowerCase());
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
                    setPlantDirect(r, c, ctx.getPlantFactory().create(toPlantName));
                    upgradedCount++;
                }
            }
        }

        if (upgradedCount == 0) {
            return "No " + fromPlantName + " found on the board to upgrade.";
        }

        ctx.setSunAmount(ctx.getSunAmount() - cost);
        return "Upgraded " + upgradedCount + "x " + fromPlantName + " to " + toPlantName + " for " + cost + " sun!";
    }

    /** وقتی زامبی گیاهی را در این مرحله می‌خورد، آن خانه برای همیشه گودال (غیرقابل‌کاشت) می‌شود. */
    public void markCrater(int row, int col) {
        craterGrid[row][col] = true;
    }

    private void swapPlants(int x1, int y1, int x2, int y2) {
        Plant[][] grid = ctx.getPlantGrid();
        Plant temp = grid[y1][x1];
        grid[y1][x1] = grid[y2][x2];
        grid[y2][x2] = temp;
    }

    private void setPlantDirect(int row, int col, Plant plant) {
        if (plant != null) {
            plant.setRow(row);
            plant.setCol(col);
        }
        ctx.getPlantGrid()[row][col] = plant;
    }

    // یافتن و حذف ترکیب‌ها (بدون دخالت گارد Tile.setPlant)
    private int checkAndRemoveMatches() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Set<int[]> matchedCells = new HashSet<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 2; c++) {
                if (isMatch(c, r, c + 1, r, c + 2, r)) {
                    matchedCells.add(new int[]{r, c});
                    matchedCells.add(new int[]{r, c + 1});
                    matchedCells.add(new int[]{r, c + 2});
                }
            }
        }

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 2; r++) {
                if (isMatch(c, r, c, r + 1, c, r + 2)) {
                    matchedCells.add(new int[]{r, c});
                    matchedCells.add(new int[]{r + 1, c});
                    matchedCells.add(new int[]{r + 2, c});
                }
            }
        }

        for (int[] cell : matchedCells) {
            setPlantDirect(cell[0], cell[1], null);
        }
        return matchedCells.size();
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
                        setPlantDirect(r, c, grid[aboveR][c]);
                        setPlantDirect(aboveR, c, null);
                        foundPlant = true;
                        break;
                    }
                }

                if (!foundPlant) {
                    String randomPlant = activePlantTypes.get(rand.nextInt(activePlantTypes.size()));
                    setPlantDirect(r, c, ctx.getPlantFactory().create(randomPlant));
                }
            }
        }
    }

    private boolean hasAnyMatch() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 2; c++) {
                if (isMatch(c, r, c + 1, r, c + 2, r)) return true;
            }
        }
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 2; r++) {
                if (isMatch(c, r, c, r + 1, c, r + 2)) return true;
            }
        }
        return false;
    }

    private void handleCascades() {
        while (hasAnyMatch()) {
            checkAndRemoveMatches();
            ctx.setSunAmount(ctx.getSunAmount() + SUN_PER_MATCH);
            currentMatches++;
            applyGravityAndRefill();
        }
        checkWinCondition();
    }

    private void checkWinCondition() {
        if (!ctx.isGameEnded() && currentMatches >= targetMatches) {
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

}