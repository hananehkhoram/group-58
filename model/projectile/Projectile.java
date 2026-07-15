package model.projectile;

import java.util.HashSet;
import java.util.Set;

public class Projectile {
    protected int damage;
    protected double x, y;
    protected int row;
    protected double speed;
    protected boolean isActive;

    protected final BulletType bulletType;
    protected final TrajectoryType trajectory;
    protected final boolean isFromZombie;

    private Damageable homingTarget;                 // فقط برای HOMING
    private final Set<Damageable> alreadyHit = new HashSet<>(); // فقط برای PIERCING

    public Projectile(int damage, double x, double y, int row, double speed,
                      BulletType bulletType, TrajectoryType trajectory, boolean isFromZombie) {
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.row = row;
        this.speed = speed;
        this.isActive = true;
        this.bulletType = bulletType;
        this.trajectory = trajectory;
        this.isFromZombie = isFromZombie;
    }

    public void update(double time) {
        if (!isActive) return;

        double direction = isFromZombie ? -1 : 1;

        switch (trajectory) {
            case HOMING:
                if (homingTarget == null || homingTarget.isDead()) {
                    isActive = false;
                    return;
                }
                x += direction * speed * time;
                break;
            case LOBBED:
            case STRAIGHT:
            case PIERCING:
            case BOWLING:
            default:
                x += direction * speed * time;
                break;
        }
    }

    public void onHit(Damageable target) {
        if (trajectory == TrajectoryType.PIERCING && alreadyHit.contains(target)) {
            return; // این هدف قبلاً از این تیر ضربه خورده
        }

        switch (bulletType) {
            case FIRE:
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
                target.takeDamage(Integer.MAX_VALUE); // نابودی آنی
                break;
            case IMMOBILIZE:
                target.applySlowOrFreeze(); // یا متد جدا مثل target.immobilize() اگر رفتارش با ICE فرق دارد
                break;
            case MAGIC:
            case SMOKE:
            case NORMAL:
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

    public double getX() { return x; }
    public int getRow() { return row; }
    public boolean isActive() { return isActive; }
    public int getDamage() { return damage; }
    public double getSpeed() { return speed; }
    public double getY() { return y; }
    public BulletType getBulletType() { return bulletType; }
    public TrajectoryType getTrajectory() { return trajectory; }
    public boolean isFromZombie() { return isFromZombie; }
}