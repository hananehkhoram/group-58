package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.projectile.ProjectileVisualVariant;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lobber implements BaseAbility {

    private static final double LOB_SPEED = 4.0;
    private static final double PLANT_FOOD_LOB_SPEED = 2.4;
    private static final int PLANT_FOOD_DAMAGE_MULTIPLIER = 3;
    private static final int BARRAGE_GAP_TICKS = 3;
    private static final int STANDARD_BARRAGE_SHOTS = 3;

    private final List<Projectile> barrageQueue = new ArrayList<>();
    private int ticksUntilNextLob;

    public void lob(LobType lobType, Plant plant, GameContext ctx) {
        tickPlantFoodBarrage(plant, ctx);
        if (isBarraging()) {
            return;
        }

        int currentSecond = ctx.getTimeManager().getTotalSeconds();

        double intervalVal = plant.getActionInterval() != null ? plant.getActionInterval() : 3.0;
        int intervalOfPlant = (int) intervalVal;

        if (currentSecond - plant.getLastActionSecond() >= intervalOfPlant) {

            Zombie targetZombie = findFirstZombieInRow(plant.getRow(), plant.getCol(), ctx);
            if (targetZombie == null) {
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

            shot.setHomingTarget(targetZombie);

            plant.armPendingShots(
                List.of(shot),
                ctx.getTimeManager().getTotalTicks()
            );
            ctx.queuePlantAttackAnimation(plant);
            plant.setLastActionSecond(currentSecond);
        }
    }
    private Zombie findFirstZombieInRow(int row, int col, GameContext ctx) {
        Zombie closest = null;
        double minX = Double.MAX_VALUE;

        for (Zombie z : ctx.getAliveZombies()) {
            if (z != null && !z.isDead() && z.occupiesRow(row) && z.getX() >= col) {
                if (z.getX() < minX) {
                    minX = z.getX();
                    closest = z;
                }
            }
        }
        return closest;
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
            LOB_SPEED,
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
        if (mode != PlantFoodMode.BARRAGE_LOB) {
            return;
        }

        int baseDamage = parseDamage(self);
        String lobType = self.getAbilityParams() == null
            ? null
            : self.getAbilityParams().get("lobType");

        List<Zombie> alive = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if (z != null && !z.isDead()) {
                alive.add(z);
            }
        }
        Collections.shuffle(alive);

        self.discardPendingShots();
        barrageQueue.clear();
        ticksUntilNextLob = 0;

        boolean kernel = "KERNEL_OR_BUTTER".equals(lobType);
        int shotCount = kernel ? Math.max(1, alive.size()) : STANDARD_BARRAGE_SHOTS;
        int empoweredDamage = Math.max(1, baseDamage) * PLANT_FOOD_DAMAGE_MULTIPLIER;

        for (int i = 0; i < shotCount; i++) {
            Zombie target = alive.isEmpty() ? null : alive.get(i % alive.size());
            boolean uniqueTarget = i < alive.size();
            barrageQueue.add(createPlantFoodLob(
                self,
                ctx,
                target,
                uniqueTarget,
                i,
                lobType,
                empoweredDamage
            ));
        }

        self.setPlantFoodActive(true);
        self.startPlantFoodGlow(shotCount * 0.3f + 0.6f);
        com.workshop.view.Console.showMessage(
            "Plant Food: " + self.getName() + " launched a barrage of lobs!"
        );
    }

    private void tickPlantFoodBarrage(Plant plant, GameContext ctx) {
        if (barrageQueue.isEmpty()) {
            return;
        }
        if (ticksUntilNextLob > 0) {
            ticksUntilNextLob--;
            return;
        }

        Projectile shot = barrageQueue.remove(0);
        ctx.setNewProjectiles(shot);
        ctx.flushPendingProjectiles();
        ctx.queuePlantAttackAnimation(plant);
        ticksUntilNextLob = barrageQueue.isEmpty() ? 0 : BARRAGE_GAP_TICKS;
        if (barrageQueue.isEmpty()) {
            plant.setPlantFoodActive(false);
        }
    }

    private boolean isBarraging() {
        return !barrageQueue.isEmpty();
    }

    private Projectile createPlantFoodLob(
        Plant plant,
        GameContext ctx,
        Zombie target,
        boolean uniqueTarget,
        int shotIndex,
        String lobType,
        int damage
    ) {
        int rows = ctx.getPlantGrid() == null ? 5 : ctx.getPlantGrid().length;
        double startX = plant.getCol();
        double startY = plant.getRow();
        double targetX = target != null ? target.getX() : startX + 5.0;
        double targetY = target != null ? target.getRow() : startY;

        if (!uniqueTarget || target == null) {
            int fan = (shotIndex % 3) - 1;
            targetY = clampRow(targetY + fan, rows);
            if (Math.abs(targetY - startY) < 0.05) {
                targetX = startX + 5.0;
            }
        }

        double dx = targetX - startX;
        double dy = targetY - startY;
        if (Math.abs(dx) < 0.2) {
            dx = 1.0;
        }
        double length = Math.hypot(dx, dy);
        dx /= length;
        dy /= length;

        BulletType bulletType = barrageBulletType(lobType);
        ProjectileVisualVariant variant = barrageVariant(lobType);

        Projectile shot = new Projectile(
            damage,
            startX,
            startY,
            plant.getRow(),
            PLANT_FOOD_LOB_SPEED,
            bulletType,
            TrajectoryType.LOBBED,
            false,
            dx,
            dy,
            plant,
            variant
        );
        if (uniqueTarget && target != null && !target.isDead()) {
            shot.setHomingTarget(target);
        }
        return shot;
    }

    private static BulletType barrageBulletType(String lobType) {
        if ("AOE_FIRE".equals(lobType)) {
            return BulletType.FIRE;
        }
        if ("AOE_ICE".equals(lobType)) {
            return BulletType.ICE;
        }
        return BulletType.NORMAL;
    }

    private static ProjectileVisualVariant barrageVariant(String lobType) {
        if ("KERNEL_OR_BUTTER".equals(lobType)) {
            return ProjectileVisualVariant.BUTTER;
        }
        return ProjectileVisualVariant.GIANT;
    }

    private static double clampRow(double row, int rows) {
        if (rows <= 1) {
            return 0;
        }
        return Math.max(0, Math.min(rows - 1, row));
    }

    private static int parseDamage(Plant self) {
        String raw = self == null ? null : self.getDamage();
        if (raw == null || raw.isBlank()) {
            return 20;
        }
        raw = raw.trim();
        if (raw.contains("x")) {
            raw = raw.substring(0, raw.indexOf('x'));
        }
        if (raw.contains("/")) {
            raw = raw.split("/")[0];
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return 20;
        }
    }
}
