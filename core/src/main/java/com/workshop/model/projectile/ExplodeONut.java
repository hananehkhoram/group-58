package com.workshop.model.projectile;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

public class ExplodeONut extends BowlingWallnut {

    private boolean exploded;
    private final GameContext context;

    public ExplodeONut(int damage, double x, double y, int row,
                       double speed, Plant ownerPlant,
                       GameContext context) {
        super(damage, x, y, row, speed, ownerPlant);
        this.context = context;
    }

    @Override
    public void onHit(Damageable target) {
        if (exploded) {
            return;
        }

        exploded = true;

        if (context != null) {
            for (Zombie zombie : context.getAliveZombies()) {
                if (zombie == null || zombie.isDead()) {
                    continue;
                }

                boolean isInsideExplosion =
                    Math.abs(zombie.getRow() - row) <= 1
                        && Math.abs(zombie.getX() - x) <= 1.0;

                if (isInsideExplosion) {
                    zombie.takeDamage(damage);
                }
            }
        } else {
            target.takeDamage(damage);
        }

        Console.showMessage(
            "Explode-o-nut exploded at (%.1f, %d).",
            x,
            row
        );

        deactivate();
    }
}
