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

    private final double dirX;
    private final double dirY;

    private Damageable homingTarget;                 // فقط برای HOMING
    private final Set<Damageable> alreadyHit = new HashSet<>(); // فقط برای PIERCING

    public Projectile(int damage, double x, double y, int row, double speed,
                      BulletType bulletType, TrajectoryType trajectory, boolean isFromZombie) {
        this(damage, x, y, row, speed, bulletType, trajectory, isFromZombie,
                isFromZombie ? -1.0 : 1.0, 0.0);
    }

    /**
     * برای شلیک‌های مورب/رو-به-عقب (QUAD_DIAGONAL, FRONT_AND_BACK, STAR_BURST) که جهت واقعی‌شون
     * با پیش‌فرض ساده‌ی isFromZombie (فقط جلو/عقب در همون row) فرق می‌کنه.
     * dirX/dirY باید یک بردار یکه (unit vector) باشن.
     */
    public Projectile(int damage, double x, double y, int row, double speed,
                      BulletType bulletType, TrajectoryType trajectory, boolean isFromZombie,
                      double dirX, double dirY) {
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
            case LOBBED:
            case STRAIGHT:
            case PIERCING:
            case BOWLING:
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

    /** برای وقتی که تیر (مثلاً یک شلیک مورب) از بالا/پایین صفحه خارج می‌شود */
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
}