package model.projectile;

import model.plants.Plant;

public class BowlingWallnut extends Projectile {

    private double bounceDirY = 0.0;

    public BowlingWallnut(int damage, double x, double y, int row, double speed, Plant ownerPlant) {
        super(damage, x, y, row, speed, BulletType.NORMAL, TrajectoryType.BOWLING, false, ownerPlant);
    }

    @Override
    public void onHit(Damageable target) {
        target.takeDamage(this.damage);

        if (bounceDirY == 0.0) {
            if (this.row <= 0) {
                bounceDirY = 1.0;
            } else if (this.row >= 4) {
                bounceDirY = -1.0;
            } else {
                bounceDirY = 1.0;
            }
        } else {
            bounceDirY = -bounceDirY;
        }
    }

    @Override
    public void update(double time) {
        if (!isActive) return;

        this.x += 1.0 * speed * time;

        if (bounceDirY != 0) {
            this.y += bounceDirY * speed * time;
            this.row = (int) Math.round(this.y);

            if (this.row <= 0 && bounceDirY < 0) {
                bounceDirY = 1.0;
            } else if (this.row >= 4 && bounceDirY > 0) {
                bounceDirY = -1.0;
            }
        }
    }
}