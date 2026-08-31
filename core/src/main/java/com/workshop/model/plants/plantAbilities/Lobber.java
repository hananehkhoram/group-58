package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.projectile.ProjectileVisualVariant;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;

import java.util.List;

public class Lobber implements BaseAbility {

    public void lob(LobType lobType, Plant plant, GameContext ctx) {
        int currentSecond = ctx.getTimeManager().getTotalSeconds();

        double intervalVal = plant.getActionInterval() != null ? plant.getActionInterval() : 3.0;
        int intervalOfPlant = (int) intervalVal;

        if (currentSecond - plant.getLastActionSecond() >= intervalOfPlant) {

            if (!isTargetInRow(plant.getRow(), plant.getCol(), ctx)) {
                return;
            }

            int damage = 20;
            try {
                if (plant.getDamage() != null && !plant.getDamage().isEmpty()) {
                    damage = Integer.parseInt(plant.getDamage());
                }
            } catch (NumberFormatException e) {
            }

            if (plant.hasPendingShots()) {
                return;
            }

            Projectile shot = switch (lobType) {
                case NORMAL -> createProjectile(plant, damage, BulletType.NORMAL);
                case KERNEL_OR_BUTTER -> Math.random() < 0.25
                    ? createProjectile(plant, 40, BulletType.NORMAL, ProjectileVisualVariant.BUTTER)
                    : createProjectile(plant, 20, BulletType.NORMAL);
                case AOE -> createProjectile(plant, damage, BulletType.NORMAL);
                case AOE_ICE -> createProjectile(plant, damage, BulletType.ICE);
                case AOE_FIRE -> createProjectile(plant, damage, BulletType.FIRE);
                default -> null;
            };
            if (shot == null) {
                return;
            }
            plant.armPendingShots(
                java.util.List.of(shot),
                ctx.getTimeManager().getTotalTicks()
            );
            ctx.queuePlantAttackAnimation(plant);
            plant.setLastActionSecond(currentSecond);
        }
    }

    private Projectile createProjectile(
        Plant plant,
        int damage,
        BulletType type
    ) {
        return createProjectile(plant, damage, type, ProjectileVisualVariant.DEFAULT);
    }

    private Projectile createProjectile(
        Plant plant,
        int damage,
        BulletType type,
        ProjectileVisualVariant visualVariant
    ) {
        return new Projectile(
            damage,
            plant.getCol(),
            plant.getRow(),
            plant.getRow(),
            4.0,
            type,
            TrajectoryType.LOBBED,
            false,
            plant,
            visualVariant
        );
    }

    private boolean isTargetInRow(int row, int col, GameContext ctx) {
        for (Zombie z : ctx.getAliveZombies()) {
            if (!z.isDead() && z.occupiesRow(row) && z.getX() >= col) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        if (mode != PlantFoodMode.BARRAGE_LOB) return;

        int baseDamage = 20;
        try {
            if (self.getDamage() != null && !self.getDamage().isEmpty()) {
                baseDamage = Integer.parseInt(self.getDamage());
            }
        } catch (NumberFormatException ignored) {
        }

        List<Zombie> alive = new java.util.ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if (!z.isDead()) alive.add(z);
        }
        if (alive.isEmpty()) return;
        java.util.Collections.shuffle(alive);

        String lobType = self.getAbilityParams().get("lobType");
        int targetCount = "KERNEL_OR_BUTTER".equals(lobType) ? alive.size() : Math.min(3, alive.size());

        for (int i = 0; i < targetCount; i++) {
            Zombie z = alive.get(i);
            z.takeDamage(baseDamage * 3);
            if ("KERNEL_OR_BUTTER".equals(lobType)) {
                z.applyButter();
            } else if ("AOE_ICE".equals(lobType)) {
                z.applySlowOrFreeze();
            }
        }

        com.workshop.view.Console.showMessage("Plant Food: " + self.getName() + " launched a barrage of lobs!");
    }
}
