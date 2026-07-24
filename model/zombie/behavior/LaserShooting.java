package model.zombie.behavior;

import model.GameContext;
import model.level.Level;
import model.plants.Plant;
import model.projectile.BulletType;
import model.zombie.Zombie;

public class LaserShooting implements Behaviors {

    private static final int TICKS_PER_SECOND = 10;

    private int distance;
    private int damageRange;
    private int timer;
    private GunType gunType;

    private static final int DYNAMITE_FUSE_SECONDS = 10;
    private long dynamiteSpawnTick = -1;
    private boolean exploded = false;
    private boolean disarmed = false;

    private int chargingTime;
    private double chargingTimeDecrementPerFiveSun;
    private int laserBeamDamage;
    private int laserBeamLength;
    private int laserCooldownTime;
    private static final int LASER_TRIGGER_RANGE = 4;
    private static final int LASER_TARGET_COLS = 4;
    private static final int SUN_STEAL_PER_SECOND = 25;

    private boolean charging = false;
    private long chargeStartTick = -1;
    private int lastStolenSecond = -1;
    private long cooldownUntilTick = 0;
    private SunThief sunThief;

    public LaserShooting(GunType gunType, int distance, int damageRange, int timer) {
        this.gunType = gunType;
        this.distance = distance;
        this.damageRange = damageRange;
        this.timer = timer;
    }

    public LaserShooting(int chargingTime, double chargingTimeDecrementPerFiveSun,
                         int laserBeamDamage, int laserBeamLength, int laserCooldownTime) {
        this.gunType = GunType.LASER;
        this.chargingTime = chargingTime;
        this.chargingTimeDecrementPerFiveSun = chargingTimeDecrementPerFiveSun;
        this.laserBeamDamage = laserBeamDamage;
        this.laserBeamLength = laserBeamLength;
        this.laserCooldownTime = laserCooldownTime;
        this.sunThief = SunThief.forBankDrain(SUN_STEAL_PER_SECOND);
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (zombie.isDead()) return;

        switch (gunType) {
            case DYNAMITE -> tickDynamite(zombie, ctx);
            case LASER -> tickLaser(zombie, ctx);
        }
    }

    private void tickDynamite(Zombie zombie, GameContext ctx) {
        if (exploded || disarmed) return;
        if (dynamiteSpawnTick < 0) {
            dynamiteSpawnTick = ctx.getTimeManager().getTotalTicks();
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
        jumper.throwToColumnAtFixedSpeed(ctx, zombie, 0);
    }

    public void disarmDynamite() {
        if (gunType == GunType.DYNAMITE) {
            this.disarmed = true;
        }
    }

    public void onProjectileHit(BulletType bulletType) {
        if (bulletType == BulletType.ICE) {
            disarmDynamite();
        }
    }

    private void tickLaser(Zombie zombie, GameContext ctx) {
        long now = ctx.getTimeManager().getTotalTicks();
        if (now < cooldownUntilTick) return;

        if (!charging) {
            Plant target = ctx.findNearestPlantInRow(zombie);

            if (target == null) return;
            double dist = zombie.getX() - target.getCol();
            if (dist < 0 || dist > LASER_TRIGGER_RANGE) return;

            charging = true;
            chargeStartTick = now;
            lastStolenSecond = -1;
            return;
        }

        int elapsedSeconds = (int) ((now - chargeStartTick) / TICKS_PER_SECOND);

        if (elapsedSeconds > lastStolenSecond && elapsedSeconds <= chargingTime) {
            if (sunThief != null) {
                sunThief.stealFromBank(ctx);
            }
            lastStolenSecond = elapsedSeconds;
        }

        if (elapsedSeconds >= chargingTime) {
            fireLaser(zombie, ctx);
        }
    }

    private void fireLaser(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int startCol = (int) Math.floor(zombie.getX());

        for (int i = 0; i < LASER_TARGET_COLS; i++) {
            int targetCol = startCol - i;
            if (targetCol >= 0 && targetCol < Level.COLS) {
                Plant p = ctx.getPlantGrid()[row][targetCol];
                if (p != null && !p.isDead()) {
                    p.takeDamage(laserBeamDamage);
                }
            }
        }

        charging = false;
        lastStolenSecond = -1;
        cooldownUntilTick = ctx.getTimeManager().getTotalTicks() + (long) laserCooldownTime * TICKS_PER_SECOND;
    }

    @Override
    public void onDeath(Zombie zombie, GameContext ctx) {
        if (gunType == GunType.LASER && sunThief != null && sunThief.getStolenSuns() > 0) {
            int halfStolen = sunThief.getStolenSuns() / 2;
            ctx.produceSun((int) zombie.getX(), zombie.getRow(), halfStolen);
            sunThief.clearStolen();
        }
    }

    public enum GunType {
        LASER,
        DYNAMITE
    }
}