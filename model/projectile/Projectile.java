package model.projectile;

import model.plants.Plant;
import view.ConsoleView;

import java.util.HashSet;
import java.util.Set;

public class Projectile {
    protected int damage;
    protected double x, y;
    protected int row;
    protected double speed;
    protected boolean isActive;
    private Plant ownerPlant;

    protected final BulletType bulletType;
    protected final TrajectoryType trajectory;
    protected final boolean isFromZombie;

    private final double dirX;
    private final double dirY;

    private int killCount = 0;


    private Damageable homingTarget;                 // فقط برای HOMING
    private final Set<Damageable> alreadyHit = new HashSet<>(); // فقط برای PIERCING

    public Projectile(int damage, double x, double y, int row, double speed,
                      BulletType bulletType, TrajectoryType trajectory, boolean isFromZombie, Plant ownerPlant) {
        this(damage, x, y, row, speed, bulletType, trajectory, isFromZombie,
                isFromZombie ? -1.0 : 1.0, 0.0, ownerPlant);
    }

    public Projectile(int damage, double x, double y, int row, double speed,
                      BulletType bulletType, TrajectoryType trajectory, boolean isFromZombie,
                      double dirX, double dirY, Plant ownerPlant) {
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.row = row;
        this.speed = speed;
        this.isActive = true;
        this.bulletType = bulletType;
        this.trajectory = trajectory;
        this.isFromZombie = isFromZombie;
        this.dirX = dirX;
        this.dirY = dirY;
        this.ownerPlant = ownerPlant;
        ConsoleView.showMessage("Projectile created at " + x + ", " + y + " from "
                + (ownerPlant != null ? ownerPlant.getName() : "zombie"));
    }


    public void update(double time) {
        if (!isActive) return;

        switch (trajectory) {
            case HOMING:
                if (homingTarget == null || homingTarget.isDead()) {
                    isActive = false;
                    return;
                }
                double toTargetX = homingTarget.getX() - x;
                double toTargetY = homingTarget.getRow() - y;
                double dist = Math.hypot(toTargetX, toTargetY);
                if (dist > 1e-6) {
                    x += (toTargetX / dist) * speed * time;
                    y += (toTargetY / dist) * speed * time;
                    row = (int) Math.round(y);
                }
                break;
            default:
                x += dirX * speed * time;
                if (dirY != 0) {
                    y += dirY * speed * time;
                    row = (int) Math.round(y);
                }
                break;
        }
    }

    public void onHit(Damageable target) {
        if (trajectory == TrajectoryType.PIERCING && alreadyHit.contains(target)) {
            return;
        }

        switch (bulletType) {
            case FIRE:
                if (target.name().equals("Imp Dragon")){break;}
                target.takeDamage(damage * 2);
                target.meltIce();
                break;
            case POISON:
                target.takeArmorPiercingDamage(damage);
                break;
            case ICE:
                target.takeDamage(damage);
                target.applySlowOrFreeze();
                break;
            case ELECTRIC:
                target.takeDamage(Integer.MAX_VALUE);
                break;
            case IMMOBILIZE:
                target.applySlowOrFreeze();
                break;
            case MAGIC:
                target.takeDamage(damage);
                break;
            case SMOKE:
                target.takeDamage(damage);
                break;
            case NORMAL:
                target.takeDamage(damage);
                break;
            default:
                target.takeDamage(damage);
                break;
        }

        if (trajectory == TrajectoryType.PIERCING) {
            alreadyHit.add(target);
        } else {
            isActive = false;
        }
    }

    public void setHomingTarget(Damageable target) {
        this.homingTarget = target;
    }

    public void deactivate() { this.isActive = false; }

    public double getX() { return x; }
    public int getRow() { return row; }
    public boolean isActive() { return isActive; }
    public int getDamage() { return damage; }
    public double getSpeed() { return speed; }
    public double getY() { return y; }
    public BulletType getBulletType() { return bulletType; }
    public TrajectoryType getTrajectory() { return trajectory; }
    public boolean isFromZombie() { return isFromZombie; }
    public Plant getOwnerPlant() {return ownerPlant;}
    public void incrementKillCount() { killCount++; }
    public int getKillCount() { return killCount; }
}