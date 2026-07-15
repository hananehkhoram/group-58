package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.projectile.BulletType;
import model.projectile.Projectile;
import model.projectile.TrajectoryType;
import model.zombie.Zombie;

public class Shooting implements Behaviors {

    private ShootingType shootingType;
    private int rate;             // shots per interval
    private int amount;           // projectile count
    private int ammo;             // TombRaiser: tombstone count remaining
    private int timeBetweenCasts; // ms between cast sequences

    private int lastShotSecond = -1; // TODO: مقدار اولیه/کولداون دقیق را طبق csv تنظیم کنید

    public Shooting(ShootingType shootingType, int rate, int amount) {
        this.shootingType = shootingType;
        this.rate = rate;
        this.amount = amount;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        switch (shootingType) {
            case HUNTER -> shootIceShard(zombie, ctx);
            // GARGANTUAR, TOMBRAISER, FISHERMAN بعداً
            default -> {}
        }
    }

    private void shootIceShard(Zombie zombie, GameContext ctx) {
        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        int cooldown = 2; // TODO: مقدار دقیق را طبق سند/csv تنظیم کنید

        if (currentSecond - lastShotSecond < cooldown) return;

        Plant target = findNearestPlantInRow(zombie, ctx);
        if (target == null) return;

        Projectile shard = new Projectile(
                10,                     // TODO: دمیج دقیق شرد یخی طبق سند
                zombie.getX(), zombie.getRow(), zombie.getRow(),
                0.15,                   // TODO: سرعت پرتابه طبق سند
                BulletType.ICE,
                TrajectoryType.STRAIGHT,
                true                    // isFromZombie
        );
        ctx.getProjectiles().add(shard);
        lastShotSecond = currentSecond;
    }

    private Plant findNearestPlantInRow(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        Plant nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Plant p : ctx.getPlantGrid()[row]) {
            if (p == null || p.isDead()) continue;
            double dist = Math.abs(zombie.getX() - p.getCol());
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public void makeTomb() {}

    public ShootingType getShootingType() { return shootingType; }
    public int getRate() { return rate; }
    public int getAmmo() { return ammo; }
    public void setAmmo(int ammo) { this.ammo = ammo; }

    public enum ShootingType {
        GARGANTUAR, TOMBRAISER, HUNTER, FISHERMAN
    }
}