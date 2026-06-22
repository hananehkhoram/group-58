package model.zombie.behavior;

import model.zombie.Zombie;

public class Shooting implements Behaviors {

    private ShootingType shootingType;
    private int rate;             // shots per interval
    private int amount;           // projectile count
    private int ammo;             // TombRaiser: tombstone count remaining
    private int timeBetweenCasts; // ms between cast sequences

    public Shooting(ShootingType shootingType, int rate, int amount) {
        this.shootingType = shootingType;
        this.rate = rate;
        this.amount = amount;
    }

    @Override
    public void onTick(Zombie zombie) {}

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public void makeTomb() {}  // TombRaiser: spawns a tombstone on the board

    public ShootingType getShootingType() { return shootingType; }
    public int getRate() { return rate; }
    public int getAmmo() { return ammo; }
    public void setAmmo(int ammo) { this.ammo = ammo; }

    public enum ShootingType {
        GARGANTUAR,   // throws Imp at half HP
        TOMBRAISER,   // raises tombstones; spawns zombies from them
        HUNTER,       // ice-age: snowball barrage (near/far range)
        FISHERMAN     // beach: casts hook to pull plants/zombies
    }
}
