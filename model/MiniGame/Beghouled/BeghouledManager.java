package model.MiniGame.Beghouled;

import model.GameContext;
import model.mechanisms.GameEngine;
import model.mechanisms.Tile;
import model.plants.Plant;
import java.util.*;

public class BeghouledManager {
    private GameContext ctx;
    private GameEngine engine;

    private List<String> activePlantTypes;

    private int targetMatches;
    private int currentMatches;

    public BeghouledManager(GameContext ctx, GameEngine engine, int targetMatches) {
        this.ctx = ctx;
        this.engine = engine;
        this.targetMatches = targetMatches;
        this.currentMatches = 0;

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
                Tile tile = engine.getTiles(c, r);
                if (tile != null && !isCrater(tile)) {
                    String randomPlant = activePlantTypes.get(rand.nextInt(activePlantTypes.size()));
                    tile.setPlant(ctx.getPlantFactory().create(randomPlant));
                }
            }
        }

        while (checkAndRemoveMatches(false) > 0) {
            applyGravityAndRefill();
        }
    }

    public boolean trySwap(int x1, int y1, int x2, int y2) {
        // بررسی مجاور بودن
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) != 1) return false;

        Tile t1 = engine.getTiles(x1, y1);
        Tile t2 = engine.getTiles(x2, y2);

        if (t1 == null || t2 == null || isCrater(t1) || isCrater(t2)) return false;
        if (t1.getPlant() == null || t2.getPlant() == null) return false;

        // جابه‌جایی موقت
        swapPlants(t1, t2);

        // اگر ترکیب ۳تایی ایجاد شد
        if (hasAnyMatch()) {
            int score = checkAndRemoveMatches(false);
            ctx.setSunAmount(ctx.getSunAmount() + score);
            currentMatches++;
            applyGravityAndRefill();
            handleCascades();

            checkWinCondition();
            return true;
        } else {
            swapPlants(t1, t2);
            return false;
        }
    }

    private void swapPlants(Tile t1, Tile t2) {
        Plant temp = t1.getPlant();
        t1.setPlant(t2.getPlant());
        t2.setPlant(temp);
    }

    // ۳. یافتن و حذف ترکیب‌ها و محاسبه خورشید
    private int checkAndRemoveMatches(boolean isCascade) {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Set<Tile> matchedTiles = new HashSet<>();

        // بررسی افقی
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 2; c++) {
                if (isMatch(c, r, c + 1, r, c + 2, r)) {
                    matchedTiles.add(engine.getTiles(c, r));
                    matchedTiles.add(engine.getTiles(c + 1, r));
                    matchedTiles.add(engine.getTiles(c + 2, r));
                }
            }
        }

        // بررسی عمودی
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 2; r++) {
                if (isMatch(c, r, c, r + 1, c, r + 2)) {
                    matchedTiles.add(engine.getTiles(c, r));
                    matchedTiles.add(engine.getTiles(c, r + 1));
                    matchedTiles.add(engine.getTiles(c, r + 2));
                }
            }
        }

        for (Tile tile : matchedTiles){
            if (tile != null) {
                tile.setPlant(null);
            }
        }
        return matchedTiles.size();
    }

    private boolean isCrater(Tile tile) {
        return false;
    }

    private void applyGravityAndRefill() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Random rand = new Random();

        // بررسی ستون به ستون
        for (int c = 0; c < cols; c++) {
            // از پایین به بالا حرکت می‌کنیم تا خانه‌های خالی را پیدا کنیم
            for (int r = rows - 1; r >= 0; r--) {
                Tile tile = engine.getTiles(c, r);
                if (tile != null && !isCrater(tile) && tile.getPlant() == null) {

                    // بگردیم بالای این خانه را پیدا کنیم تا یک گیاه را به پایین بکشیم
                    boolean foundPlant = false;
                    for (int aboveR = r - 1; aboveR >= 0; aboveR--) {
                        Tile aboveTile = engine.getTiles(c, aboveR);
                        if (aboveTile != null && aboveTile.getPlant() != null) {
                            // گیاه بالای سر را می‌آوریم پایین
                            tile.setPlant(aboveTile.getPlant());
                            aboveTile.setPlant(null);
                            foundPlant = true;
                            break;
                        }
                    }

                    // اگر هیچ گیاهی بالای سرش نبود (بالای صفحه بود)، یک گیاه رندوم جدید از بالا بساز
                    if (!foundPlant) {
                        String randomPlant = activePlantTypes.get(rand.nextInt(activePlantTypes.size()));
                        tile.setPlant(ctx.getPlantFactory().create(randomPlant));
                    }
                }
            }
        }
    }

    // بررسی اینکه آیا اصلاً ترکیب ۳تایی وجود دارد یا خیر
    private boolean hasAnyMatch() {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();

        // بررسی افقی
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 2; c++) {
                if (isMatch(c, r, c + 1, r, c + 2, r)) {
                    return true;
                }
            }
        }

        // بررسی عمودی
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 2; r++) {
                if (isMatch(c, r, c, r + 1, c, r + 2)) {
                    return true;
                }
            }
        }

        return false;
    }

    // هندل کردن ترکیب‌های پشت سر هم
    private void handleCascades() {
        while (hasAnyMatch()) {
            int score = checkAndRemoveMatches(true);
            ctx.setSunAmount(ctx.getSunAmount() + score);
            applyGravityAndRefill();
        }
    }

    // بررسی شرط پیروزی بازی
    private void checkWinCondition() {
        if (currentMatches >= targetMatches) {

        }
    }

    // بررسی اینکه آیا سه خانه با هم مچ هستند یا خیر
    private boolean isMatch(int c1, int r1, int c2, int r2, int c3, int r3) {
        Tile t1 = engine.getTiles(c1, r1);
        Tile t2 = engine.getTiles(c2, r2);
        Tile t3 = engine.getTiles(c3, r3);

        if (t1 == null || t2 == null || t3 == null) return false;
        Plant p1 = t1.getPlant();
        Plant p2 = t2.getPlant();
        Plant p3 = t3.getPlant();

        if (p1 == null || p2 == null || p3 == null) return false;

        // مقایسه نام یا نوع گیاهان
        return p1.getName().equals(p2.getName()) && p2.getName().equals(p3.getName());
    }

}