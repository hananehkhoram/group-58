package model.projectile;

import model.plants.Plant;

public class GiantWallnut extends BowlingWallnut {

    public GiantWallnut(int damage, double x, double y, int row, double speed, Plant ownerPlant) {
        super(damage, x, y, row, speed, ownerPlant);
    }

    @Override
    public void onHit(Damageable target) {
        target.takeDamage(this.damage * 10);
    }
}