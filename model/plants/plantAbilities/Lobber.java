package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.projectile.Projectile;
import model.projectile.BulletType;
import model.projectile.TrajectoryType;
import model.zombie.Zombie;

public class Lobber implements BaseAbility {

    @Override
    public void activate(Plant self, GameContext ctx) {

    }

    public void lob(LobType lobType, String interval, Plant plant, GameContext ctx) {

        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        int intervalOfPlant = Integer.parseInt(interval);

        if (currentSecond - plant.getLastActionSecond() >= intervalOfPlant) {

            if (!isTargetInRow(plant.getRow(), plant.getCol(), ctx)) {
                return;
            }

            int damage = 0;
            try {
                damage = Integer.parseInt(plant.getDamage());
            } catch (NumberFormatException e) {

            }

            boolean hasShot = true;

            switch (lobType) {
                case NORMAL:
                    shootProjectile(ctx, plant, damage, BulletType.NORMAL);
                    break;

                case KERNEL_OR_BUTTER:
                    boolean isButter = Math.random() < 0.25;
                    if (isButter) {
                        shootProjectile(ctx, plant, 40, BulletType.IMMOBILIZE);
                    } else {
                        shootProjectile(ctx, plant, 20, BulletType.NORMAL);
                    }
                    break;

                case AOE:
                    shootProjectile(ctx, plant, damage, BulletType.NORMAL);
                    break;

                case AOE_ICE:
                    shootProjectile(ctx, plant, damage, BulletType.ICE);
                    break;

                case AOE_FIRE:
                    shootProjectile(ctx, plant, damage, BulletType.FIRE);
                    break;
            }

            if (hasShot) {
                plant.setLastActionSecond(currentSecond);
            }
        }
    }


    private void shootProjectile(GameContext ctx, Plant plant, int damage, BulletType type) {
        Projectile p = new Projectile(
                damage,
                plant.getCol(), 0, plant.getRow(),
                4.0,
                type,
                TrajectoryType.LOBBED,
                false,
                plant
        );
        ctx.setNewProjectiles(p);
    }

    private boolean isTargetInRow(int row, int col, GameContext ctx) {
        for (Zombie z : ctx.getAliveZombies()) {
            if (!z.isDead() && z.getRow() == row && z.getX() >= col) {
                return true;
            }
        }
        return false;
    }

}

