package model.zombie.behavior;

import controller.repository.ZombieRepository;
import controller.repository.factory.ZombieFactory;
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
    private boolean imped = false;

    private int lastShotSecond = 1 ;

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
            case GARGANTUAR -> shootImp(zombie, ctx);
            default -> {}
        }
    }

    private void shootImp(Zombie gargantuar, GameContext ctx) {
        if (gargantuar.getHp() > 1800 || imped) {
            return;
        }

        Zombie imp = null;
        for (Zombie z : ctx.getAliveZombies()) {
            if ("Imp".equalsIgnoreCase(z.getName()) && z.getRow() == gargantuar.getRow()) {
                if (z.getJumper() != null && z.getJumper().isLanded()) {
                    imp = z;
                    break;
                }
            }
        }
        boolean isNewImp = false;
        if (imp == null) {
            imp = createNewImp(gargantuar, ctx);
            isNewImp = true;
        }

        if (imp != null) {
            Jumper jumper = imp.getJumper();
            if (jumper == null) {
                jumper = new Jumper(Jumper.JumpVariant.IMP);
                imp.getBehaviors().put("jumper", jumper);
            }

            int targetCol = Math.max(0, (int) gargantuar.getX() - 3);
            float flightTime = 1.0f;
            int apex = 1;

            jumper.startJump(ctx, imp, targetCol, flightTime, apex);

            if (isNewImp) {
                ctx.addZombie(imp);
            }

            imped = true;
        }
    }

    private Zombie createNewImp(Zombie gargantuar, GameContext ctx) {
        if (ctx.getDataManager() == null) return null;

        ZombieFactory zf = new ZombieFactory(ctx.getDataManager());
        Zombie imp = zf.create("Imp");
        if (imp != null) {
            imp.setX(gargantuar.getX());
            imp.setY(gargantuar.getRow());
        }
        return imp;
    }


    private void shootIceShard(Zombie zombie, GameContext ctx) {
        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        int cooldown = 2;

        if (currentSecond - lastShotSecond < cooldown) return;

        Plant target = ctx.findNearestPlantInRow(zombie);
        if (target == null) return;

        Projectile shard = new Projectile(
                10,
                zombie.getX(), zombie.getRow(), zombie.getRow(),
                0.15,
                BulletType.ICE,
                TrajectoryType.STRAIGHT,
                true,
                null
        );
        ctx.getProjectiles().add(shard);
        lastShotSecond = currentSecond;
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