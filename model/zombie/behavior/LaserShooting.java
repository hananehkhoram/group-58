package model.zombie.behavior;

import model.zombie.Zombie;

public class LaserShooting implements Behaviors {

    private int distance;        // distance from target to start firing
    private int damageRange;     // beam/explosion range
    private int timer;           // cooldown ticks
    private int timesRemain;
    private GunType gunType;

    // CrystalSkull-specific
    private int chargingTime;                    // seconds to charge (default 5)
    private double chargingTimeDecrementPerFiveSun; // 0.2 — charges faster near sun
    private int laserBeamDamage;                 // 4001 (instakill)
    private int laserBeamLength;                 // 220 grid units
    private int laserCooldownTime;               // 5 seconds

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
    public void onTick(Zombie zombie) {}

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public GunType getGunType() { return gunType; }
    public int getLaserBeamDamage() { return laserBeamDamage; }
    public int getChargingTime() { return chargingTime; }

    public enum GunType {
        LASER,     // CrystalSkull: charges then fires a beam (damage 4001)
        DYNAMITE   // Prospector: launches dynamite backward
    }
}
