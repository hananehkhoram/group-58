package model.zombie.behavior;

import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.level.Level;
import model.plants.Plant;
import model.projectile.BulletType;
import model.projectile.Projectile;
import model.projectile.TrajectoryType;
import model.season.Grave;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shooting implements Behaviors {

    private ShootingType shootingType;
    private int rate;             // shots per interval
    private int amount;           // projectile count
    private int ammo;             // TombRaiser: tombstone count remaining
    private boolean imped = false;
    private boolean hasShot;

    private int lastShotSecond = 1;

    public Shooting(ShootingType shootingType, int rate, int amount) {
        this.shootingType = shootingType;
        this.rate = rate;
        this.amount = amount;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        switch (shootingType) {
            case HUNTER -> handleShooter(zombie, ctx, BulletType.ICE, 5);
            case OCTOPUS -> handleShooter(zombie, ctx, BulletType.OCTOPUS, 6);
            case GARGANTUAR -> shootImp(zombie, ctx);
            case TOMBRAISER -> raiseTomb(zombie, ctx);
            default -> {}
        }
    }

    private void handleShooter(Zombie zombie, GameContext ctx, BulletType bulletType, int cooldownSeconds) {
        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        if (currentSecond - lastShotSecond < cooldownSeconds) return;

        Plant target = findTargetPlant(zombie, ctx, bulletType);
        if (target == null) return;

        shootProjectile(zombie, ctx, bulletType);
        lastShotSecond = currentSecond;
    }

    private void shootProjectile(Zombie zombie, GameContext ctx, BulletType bulletType) {
        double speed = 0.8;

        Projectile projectile = new Projectile(
                10,
                zombie.getX(),
                zombie.getRow(),
                zombie.getRow(),
                speed,
                bulletType,
                TrajectoryType.STRAIGHT,
                true,
                null
        );

        ctx.getProjectiles().add(projectile);
    }

    private Plant findTargetPlant(Zombie zombie, GameContext ctx, BulletType bulletType) {
        int row = zombie.getRow();
        Plant nearestPlant = null;
        double minDistance = Double.MAX_VALUE;

        for (int c = 0; c < Level.COLS; c++) {
            Plant p = ctx.getPlantGrid()[row][c];
            if (p != null && !p.isDead() && c <= zombie.getX()) {

                boolean isAlreadyAffected = (bulletType == BulletType.ICE && p.isIced()) ||
                        (bulletType == BulletType.OCTOPUS && p.isOctopused());

                if (!isAlreadyAffected) {
                    double dist = zombie.getX() - c;
                    if (dist < minDistance) {
                        minDistance = dist;
                        nearestPlant = p;
                    }
                }
            }
        }
        return nearestPlant;
    }

    private void raiseTomb(Zombie zombie, GameContext ctx) {
        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        int cooldown = 7;
        if (currentSecond - lastShotSecond < cooldown) return;

        List<int[]> emptyTiles = new ArrayList<>();
        for (int r = 0; r < Level.ROWS; r++) {
            for (int c = 0; c < Level.COLS; c++) {
                boolean hasPlant = ctx.getPlantGrid()[r][c] != null;
                boolean hasGrave = ctx.getGraveGrid()[r][c] != null;

                if (!hasPlant && !hasGrave) {
                    emptyTiles.add(new int[]{r, c});
                }
            }
        }

        if (emptyTiles.isEmpty()) return;

        Random rand = new Random();

        int tombsToCreate = Math.min(rate, emptyTiles.size());
        for (int i = 0; i < tombsToCreate; i++) {
            int[] tile = emptyTiles.remove(rand.nextInt(emptyTiles.size()));
            int r = tile[0];
            int c = tile[1];

            ctx.getGraveGrid()[r][c] = new Grave(Grave.GraveType.NORMAL, r, c);

            view.ConsoleView.showMessage(String.format(
                    "Tomb Raiser Zombie raised a tombstone at (%d, %d).\n", r, c));
        }

        lastShotSecond = currentSecond;
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

    public enum ShootingType {
        GARGANTUAR, TOMBRAISER, HUNTER, OCTOPUS
    }
}