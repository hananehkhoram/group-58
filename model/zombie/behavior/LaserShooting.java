package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;

public class LaserShooting implements Behaviors {

    private static final int TICKS_PER_SECOND = 10; // مطابق TimeManager

    private int distance;        // distance from target to start firing
    private int damageRange;     // beam/explosion range
    private int timer;           // cooldown ticks
    private int timesRemain;
    private GunType gunType;

    // --- Prospector (DYNAMITE) ---
    private static final int DYNAMITE_FUSE_SECONDS = 10; // طبق سند: بعد از ۱۰ ثانیه منفجر می‌شود
    private long dynamiteSpawnTick = -1;
    private boolean exploded = false;
    private boolean disarmed = false;

    // --- CrystalSkull / Turquoise (LASER) ---
    private int chargingTime;                       // seconds to charge (default 5)
    private double chargingTimeDecrementPerFiveSun;  // 0.2 — TODO: هنوز اعمال نشده، پایین توضیح داده شده
    private int laserBeamDamage;                     // 4001 (instakill)
    private int laserBeamLength;                     // 220 — واحد بصری؛ در منطق شبکه‌ای استفاده نمی‌شود
    private int laserCooldownTime;                   // 5 seconds
    private static final int LASER_TRIGGER_RANGE = 4;    // شعاع دیدن گیاه برای شروع دزدی
    private static final int LASER_TARGET_COLS = 4;      // تعداد خانه‌های جلو که لیزر نابود می‌کند
    private static final int SUN_STEAL_PER_SECOND = 25;

    private boolean charging = false;
    private long chargeStartTick = -1;
    private int lastStolenSecond = -1;
    private int stolenSuns = 0;
    private long cooldownUntilTick = 0;

    public LaserShooting(GunType gunType, int distance, int damageRange, int timer) {
        this.gunType = gunType;
        this.distance = distance;
        this.damageRange = damageRange;
        this.timer = timer;
    }

    // Constructor for CrystalSkull laser
    public LaserShooting(int chargingTime, double chargingTimeDecrementPerFiveSun,
                         int laserBeamDamage, int laserBeamLength, int laserCooldownTime) {
        this.gunType = GunType.LASER;
        this.chargingTime = chargingTime;
        this.chargingTimeDecrementPerFiveSun = chargingTimeDecrementPerFiveSun;
        this.laserBeamDamage = laserBeamDamage;
        this.laserBeamLength = laserBeamLength;
        this.laserCooldownTime = laserCooldownTime;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        switch (gunType) {
            case DYNAMITE -> tickDynamite(zombie, ctx);
            case LASER -> tickLaser(zombie, ctx);
        }
    }

    // ================= Prospector =================

    private void tickDynamite(Zombie zombie, GameContext ctx) {
        if (exploded || disarmed) return;

        if (dynamiteSpawnTick < 0) {
            dynamiteSpawnTick = ctx.getTimeManager().getTotalTicks();
        }

        if (zombie.isIced()) {
            // طبق سند: اگر تیر یخی بهش بخورد، دینامیت خاموش می‌شود و دیگر منفجر نمی‌شود
            disarmed = true;
            return;
        }

        long elapsedTicks = ctx.getTimeManager().getTotalTicks() - dynamiteSpawnTick;
        if (elapsedTicks >= DYNAMITE_FUSE_SECONDS * (long) TICKS_PER_SECOND) {
            explode(zombie, ctx);
        }
    }

    private void explode(Zombie zombie, GameContext ctx) {
        exploded = true;
        Jumper jumper = zombie.getJumper();
        if (jumper == null) return;
        jumper.turnBackward();
        // طبق سند: به انتهای سطر (کنار خانه‌ی بازیکن) پرتاب می‌شود؛ ستون ۰ = کنار خانه
        jumper.startJump(ctx, zombie, 0, 0.6f, 40); // TODO: زمان/ارتفاع دقیق پرش را طبق سند تنظیم کنید
    }

    // ================= Turquoise (CrystalSkull) =================

    private void tickLaser(Zombie zombie, GameContext ctx) {
        long now = ctx.getTimeManager().getTotalTicks();
        if (now < cooldownUntilTick) return;

        if (!charging) {
            Plant target = ctx.findNearestPlantInRow(zombie);
            if (target == null) return;
            double dist = Math.abs(zombie.getX() - target.getCol());
            if (dist > LASER_TRIGGER_RANGE) return;

            charging = true;
            chargeStartTick = now;
            lastStolenSecond = -1;
            return;
        }

        int elapsedSeconds = (int) ((now - chargeStartTick) / TICKS_PER_SECOND);

        // هر ثانیه (تا قبل از پایان شارژ) ۲۵ واحد خورشید از بازیکن می‌دزدد
        if (elapsedSeconds > lastStolenSecond && elapsedSeconds < chargingTime) {
            int stealAmount = Math.min(SUN_STEAL_PER_SECOND, ctx.getSunAmount());
            if (stealAmount > 0) {
                ctx.setSunAmount(ctx.getSunAmount() - stealAmount);
                stolenSuns += stealAmount;
            }
            lastStolenSecond = elapsedSeconds;
        }

        // TODO chargingTimeDecrementPerFiveSun: طبق سند هرچه خورشید بیشتری دزدیده شود شارژ سریع‌تر تمام می‌شود.
        // فعلاً chargingTime ثابت در نظر گرفته شده؛ می‌توان اینجا هر ۵ واحد دزدیده‌شده،
        // chargingTime را به‌اندازه‌ی chargingTimeDecrementPerFiveSun کم کرد.
        if (elapsedSeconds >= chargingTime) {
            fireLaser(zombie, ctx);
        }
    }

    private void fireLaser(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int startCol = (int) Math.floor(zombie.getX()) - 1;

        for (int col = startCol; col >= Math.max(0, startCol - (LASER_TARGET_COLS - 1)); col--) {
            Plant p = ctx.getPlantGrid()[row][col];
            if (p != null && !p.isDead()) {
                p.takeDamage(laserBeamDamage);
            }
        }

        charging = false;
        cooldownUntilTick = ctx.getTimeManager().getTotalTicks() + (long) laserCooldownTime * TICKS_PER_SECOND;
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public void onDeath(Zombie zombie, GameContext ctx) {
        if (gunType == GunType.LASER && stolenSuns > 0) {
            ctx.addSun(stolenSuns / 2);
            stolenSuns = 0;
        }
    }

    @Override
    public boolean isDestroyed() { return false; }

    public GunType getGunType() { return gunType; }
    public int getLaserBeamDamage() { return laserBeamDamage; }
    public int getChargingTime() { return chargingTime; }
    public int getStolenSuns() { return stolenSuns; }
    public boolean isCharging() { return charging; }
    public boolean isDisarmed() { return disarmed; }

    public enum GunType {
        LASER,     // CrystalSkull: charges then fires a beam (damage 4001)
        DYNAMITE   // Prospector: launches dynamite backward
    }
}