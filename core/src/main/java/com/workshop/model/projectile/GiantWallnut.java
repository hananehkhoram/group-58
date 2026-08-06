package com.workshop.model.projectile;

import com.workshop.model.plants.Plant;

public class GiantWallnut extends BowlingWallnut {

    public GiantWallnut(
        int damage,
        double x,
        double y,
        int row,
        double speed,
        Plant ownerPlant
    ) {
        super(damage, x, y, row, speed, ownerPlant);
    }

    @Override
    public void onHit(Damageable target) {
        target.takeArmorPiercingDamage(Integer.MAX_VALUE);
    }
}
