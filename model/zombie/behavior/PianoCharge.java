package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

/**
 * ZombiePiano — طبق سند این پروژه (صفحه ۳۷)، نه طبق زامبی واقعیِ PvZ2 که این کلاس اصلش
 * برایش نوشته شده بود (که «rolling» و «breakPlants» داشت). طبق سند:
 *   - با سرعت عادی حرکت می‌کند (بدون حالت rolling سریع)
 *   - هر گیاهی که با آن برخورد کند از بین می‌رود
 *   - همیشه پیانو می‌نوازد؛ هرچند ثانیه یک‌بار، سطر HR همه‌ی زامبی‌های روی زمین رو با یکی از
 *     سطرهای همسایه‌شون به‌صورت تصادفی جابه‌جا می‌کند
 *
 * فیلدهای rolling/rollSpeed/breakPlants و... حفظ شدن (چون امضای سازنده جای دیگه صدا زده میشه)
 * ولی طبق این سند استفاده‌ای ندارن.
 */
public class PianoCharge implements Behaviors {

    private static final int TICKS_PER_SECOND = 10;
    private static final float ROW_SWAP_INTERVAL_SECONDS = 5f; // TODO: «هرچند ثانیه» دقیق تو سند نیست

    private boolean rolling;  // طبق سند این پروژه استفاده نمی‌شه؛ برای سازگاری با سازنده‌ی قبلی نگه داشته شده
    private double rollSpeed;         // 0.4 — استفاده نمی‌شه
    private double normalSpeed;       // 0.12 — استفاده نمی‌شه
    private double rollingEatDps;     // 4000 — استفاده نمی‌شه
    private int streetWidth;          // 3 — استفاده نمی‌شه
    private int streetHeight;         // 2 — استفاده نمی‌شه
    private Set<String> breakPlants;  // استفاده نمی‌شه (طبق سند این پروژه مکانیزم شکستن پیانو وجود ندارد)

    private long lastSwapTick = -1;
    private final Random random = new Random();

    public PianoCharge(double rollSpeed, double normalSpeed, double rollingEatDps,
                       int streetWidth, int streetHeight, List<String> breakPlants) {
        this.rolling = true;
        this.rollSpeed = rollSpeed;
        this.normalSpeed = normalSpeed;
        this.rollingEatDps = rollingEatDps;
        this.streetWidth = streetWidth;
        this.streetHeight = streetHeight;
        this.breakPlants = new HashSet<>(breakPlants);
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        destroyOwnCellPlant(zombie, ctx);
        shuffleZombieRows(ctx);
    }

    private void destroyOwnCellPlant(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int col = (int) zombie.getX();
        int totalCols = ctx.getPlantGrid()[0].length;
        if (col < 0 || col >= totalCols) return;

        Plant target = ctx.getPlantGrid()[row][col];
        if (target != null && !target.isDead()) {
            target.takeDamage(Integer.MAX_VALUE);
        }
    }

    private void shuffleZombieRows(GameContext ctx) {
        long now = ctx.getTimeManager().getTotalTicks();
        if (lastSwapTick >= 0 && (now - lastSwapTick) < (long) (ROW_SWAP_INTERVAL_SECONDS * TICKS_PER_SECOND)) {
            return;
        }

        int totalRows = ctx.getPlantGrid().length;
        for (Zombie z : ctx.getAliveZombies()) {
            z.setRow(pickNeighborRow(z.getRow(), totalRows));
        }
        lastSwapTick = now;
    }

    private int pickNeighborRow(int row, int totalRows) {
        boolean canGoUp = row > 0;
        boolean canGoDown = row < totalRows - 1;
        if (canGoUp && canGoDown) return random.nextBoolean() ? row - 1 : row + 1;
        if (canGoUp) return row - 1;
        if (canGoDown) return row + 1;
        return row;
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public void breakPiano(Zombie zombie) {
        rolling = false;
    }

    public boolean willBreakOn(String plantId) {
        return breakPlants.contains(plantId);
    }

    public boolean isRolling() { return rolling; }
    public double getCurrentSpeed() { return rolling ? rollSpeed : normalSpeed; }
    public double getRollingEatDps() { return rollingEatDps; }
    public int getStreetWidth() { return streetWidth; }
    public int getStreetHeight() { return streetHeight; }
}