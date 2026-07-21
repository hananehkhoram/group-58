package model.projectile;

import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;

public class ExplodeONut extends BowlingWallnut {

    private boolean exploded = false;
    private GameContext context;

    public ExplodeONut(int damage, double x, double y, int row, double speed, Plant ownerPlant, GameContext context) {
        super(damage, x, y, row, speed, ownerPlant);
        this.context = context;
    }

    @Override
    public void onHit(Damageable target) {
        if (exploded) return;
        exploded = true;

        target.takeDamage(this.damage * 4);

        if (context != null && context.getActiveZombies() != null) {
            for (Zombie z : context.getActiveZombies()) {
                if (z != null && !z.isDead()) {
                    double dist = Math.hypot(z.getX() - this.x, z.getRow() - this.row);
                    if (dist <= 1.5) {
                        z.takeDamage(this.damage * 2);
                    }
                }
            }
        }

        this.deactivate();
    }
}