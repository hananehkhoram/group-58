package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.TargetingMode;
import com.workshop.model.plants.enums.ShootType;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.Armor;

import java.util.ArrayList;
import java.util.List;

public class Shooters implements BaseAbility {

    private static final double DEFAULT_PROJECTILE_SPEED = 1.0;
    private static final double PEA_MOUTH_X_FROM_CENTER = 0.34;
    private static final double PEA_MOUTH_Y_FROM_CENTER = 0.10;

    @Override
    public void activate(Plant self, GameContext ctx) {
        if (self == null || ctx == null) return;

        if ("PuffShroom".equalsIgnoreCase(self.getName())) {
            int currentSecond = ctx.getTimeManager().getTotalSeconds();
            if (currentSecond - self.getPlantTimeSecond() >= 60) {
                self.takeDamage(Integer.MAX_VALUE);
                ctx.getActivePlants().remove(self);
            }
        }
    }

    public void shoot(int damage, int amount, String interval, ShootType shootType,
                      BulletType bulletType, Plant self, GameEngine engine) {
        GameContext ctx = engine.getCtx();
        int currentSecond = ctx.getTimeManager().getTotalSeconds();

        boolean everyRound = interval.equals("everyRound");
        boolean canFireNow;
        if (everyRound) {
            canFireNow = true;
        } else {
            int intervalOfPlant = Integer.parseInt(interval);
            canFireNow = currentSecond - self.getLastActionSecond() >= intervalOfPlant;
        }
        if (!canFireNow) return;
        if (self.hasPendingShots()) return;

        if (shootType == ShootType.STRAIGHT
            || shootType == ShootType.STRAIGHT_SEQUENTIAL) {

            if (!hasZombieAhead(self, ctx)) {
                return;
            }
        }

        if ("Magnet-shroom".equalsIgnoreCase(self.getName())
            && (shootType == ShootType.RANDOM_HOMING
            || shootType == ShootType.NEAREST_TARGET
            || shootType == ShootType.RANDOM_INSTANT)) {
            if (handleMagnetShroomAction(self, ctx)) {
                ctx.queuePlantAttackAnimation(self);
                if (!everyRound) {
                    self.setLastActionSecond(currentSecond);
                }
            }
            return;
        }

        List<Projectile> shots;
        if (shootType == ShootType.RANDOM_HOMING || shootType == ShootType.NEAREST_TARGET
            || shootType == ShootType.RANDOM_INSTANT) {
            shots = createHoming(damage, bulletType, shootType, self, ctx, engine);
        } else if (shootType == ShootType.TRI_LANE) {
            shots = createTriLane(damage, bulletType, self, ctx);
        } else {
            shots = createDirectional(damage, amount, shootType, bulletType, self, ctx);
        }

        if (shots.isEmpty()) {
            return;
        }

        if (shootType == ShootType.BOWLING) {
            spawnNow(ctx, shots);
        } else {
            self.armPendingShots(shots, ctx.getTimeManager().getTotalTicks());
            ctx.queuePlantAttackAnimation(self);
        }

        if (!everyRound) {
            self.setLastActionSecond(currentSecond);
        }
    }

    private static void spawnNow(GameContext ctx, List<Projectile> shots) {
        for (Projectile shot : shots) {
            ctx.setNewProjectiles(shot);
        }
        ctx.flushPendingProjectiles();
    }

    private List<Projectile> createTriLane(int damage, BulletType bulletType, Plant self, GameContext ctx) {
        int originRow = self.getRow();
        int totalRows = ctx.getPlantGrid().length;
        double startX = peaLaunchX(self, 1.0);
        double[] headOffsets = {-0.32, 0.0, 0.32};
        int[] laneDeltas = {-1, 0, 1};
        List<Projectile> shots = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            int targetRow = Math.max(0, Math.min(totalRows - 1, originRow + laneDeltas[i]));
            double startY = peaLaunchY(originRow + headOffsets[i], self);
            shots.add(new Projectile(
                damage,
                startX,
                startY,
                targetRow,
                DEFAULT_PROJECTILE_SPEED,
                bulletType,
                TrajectoryType.STRAIGHT,
                false,
                1.0,
                0.0,
                self
            ));
        }
        return shots;
    }

    private List<Projectile> createDirectional(int damage, int amount, ShootType shootType,
                                     BulletType bulletType, Plant self, GameContext ctx) {

        List<Integer> lanes = resolveLanes(shootType, self, ctx);
        if (lanes.isEmpty()) {
            return List.of();
        }

        List<double[]> directions = resolveDirections(shootType);

        TrajectoryType trajectory = switch (shootType) {
            case PIERCING -> TrajectoryType.PIERCING;
            case BOWLING -> TrajectoryType.BOWLING;
            default -> TrajectoryType.STRAIGHT;
        };

        int shotsPerLane = (shootType == ShootType.STRAIGHT_SEQUENTIAL) ? Math.max(1, amount) : 1;

        List<Projectile> shots = new ArrayList<>();
        for (int row : lanes) {
            for (double[] dir : directions) {
                for (int i = 0; i < shotsPerLane; i++) {
                    double startX = peaLaunchX(self, dir[0]) + dir[0] * 0.3 * i;
                    double startY = peaLaunchY(row + dir[1] * 0.3 * i, self);
                    shots.add(new Projectile(damage, startX, startY, row,
                        DEFAULT_PROJECTILE_SPEED, bulletType, trajectory, false, dir[0], dir[1], self));
                }
            }
        }
        return shots;
    }

    private List<double[]> resolveDirections(ShootType shootType) {
        List<double[]> dirs = new ArrayList<>();
        switch (shootType) {
            case QUAD_DIAGONAL -> {
                double d = 1.0 / Math.sqrt(2);
                dirs.add(new double[]{d, d});
                dirs.add(new double[]{d, -d});
                dirs.add(new double[]{-d, d});
                dirs.add(new double[]{-d, -d});
            }
            case FRONT_AND_BACK -> {
                dirs.add(new double[]{1, 0});
                dirs.add(new double[]{-1, 0});
            }
            case STAR_BURST -> {
                for (int i = 0; i < 5; i++) {
                    double angle = Math.toRadians(72 * i);
                    dirs.add(new double[]{Math.cos(angle), Math.sin(angle)});
                }
            }
            default -> dirs.add(new double[]{1, 0});
        }
        return dirs;
    }

    private List<Integer> resolveLanes(ShootType shootType, Plant self, GameContext ctx) {
        List<Integer> lanes = new ArrayList<>();
        int row = self.getRow();
        int totalRows = ctx.getPlantGrid().length;

        if (shootType == ShootType.TRI_LANE) {
            for (int r = row - 1; r <= row + 1; r++) {
                if (r >= 0 && r < totalRows) lanes.add(r);
            }
        } else {
            lanes.add(row);
        }
        return lanes;
    }

    private List<Projectile> createHoming(int damage, BulletType bulletType, ShootType shootType,
                                Plant self, GameContext ctx, GameEngine engine) {

        if ("Magnet-shroom".equalsIgnoreCase(self.getName())) {
            return List.of();
        }

        TargetingMode mode = (shootType == ShootType.NEAREST_TARGET) ? TargetingMode.NEAREST : TargetingMode.RANDOM;
        List<Zombie> candidates = engine.findTargets(self.getRow(), self.getCol(), mode);
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        Zombie target = candidates.get(0);

        Projectile p = new Projectile(damage, self.getX(), self.getRow(), self.getRow(),
            DEFAULT_PROJECTILE_SPEED, bulletType, TrajectoryType.HOMING, false, self);
        p.setHomingTarget(target);
        return List.of(p);
    }

    private boolean handleMagnetShroomAction(Plant self, GameContext ctx) {
        for (Zombie z : ctx.getAliveZombies()) {
            Armor armor = z.getArmor();
            if (armor == null) {
                armor = z.getSecondaryArmor();
            }
            if (armor != null && !armor.isDestroyed() && armor.isMetallic()) {
                armor.setMagetized(true);
                z.removeArmor();
                return true;
            }
        }
        return false;
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        java.util.Map<String, String> p = self.getAbilityParams();
        int amount = Integer.parseInt(p.get("amount"));
        ShootType shootType = ShootType.valueOf(p.get("shootType"));
        BulletType bulletType = BulletType.valueOf(p.get("bulletType"));
        GameEngine engine = ctx.getGameEngine();
        int damage = 20;
        try {
            if (self.getDamage() != null && !self.getDamage().isEmpty()) {
                damage = Integer.parseInt(self.getDamage());
            }
        } catch (NumberFormatException ignored) {
        }

        switch (mode) {
            case BARRAGE -> {
                int burstShots = 8;
                int empoweredDamage = damage * 2;
                for (int i = 0; i < burstShots; i++) {
                    if (shootType == ShootType.RANDOM_HOMING || shootType == ShootType.NEAREST_TARGET) {
                        spawnNow(ctx, createHoming(empoweredDamage, bulletType, shootType, self, ctx, engine));
                    } else if (shootType == ShootType.TRI_LANE) {
                        spawnNow(ctx, createTriLane(empoweredDamage, bulletType, self, ctx));
                    } else {
                        spawnNow(ctx, createDirectional(empoweredDamage, amount, shootType, bulletType, self, ctx));
                    }
                }
            }
            case MULTI_TARGET_BURST -> {
                if ("Magnet-shroom".equalsIgnoreCase(self.getName())) {
                    for (int i = 0; i < 3; i++) handleMagnetShroomAction(self, ctx);
                } else {
                    for (int i = 0; i < 3; i++) {
                        spawnNow(ctx, createHoming(damage * 2, bulletType, ShootType.RANDOM_HOMING, self, ctx, engine));
                    }
                }
            }
            case SELF_RESET -> {
                int currentSecond = ctx.getTimeManager().getTotalSeconds();
                for (Plant other : ctx.getAlivePlants()) {
                    if (other.getName().equalsIgnoreCase(self.getName())) {
                        other.setLastActionSecond(0);
                        other.setPlantTimeSecond(currentSecond);
                    }
                }
            }
            default -> {
            }
        }
        com.workshop.view.Console.showMessage("Plant Food: " + self.getName() + " unleashed a barrage!");
    }

    private static double peaLaunchX(Plant self, double dirX) {
        if (!self.isPeaFamily()) {
            return self.getX();
        }
        double facing = dirX < 0 ? -1.0 : 1.0;
        return self.getX() + 0.5 + facing * PEA_MOUTH_X_FROM_CENTER;
    }

    private static double peaLaunchY(double rowY, Plant self) {
        if (!self.isPeaFamily()) {
            return rowY;
        }
        return rowY - PEA_MOUTH_Y_FROM_CENTER;
    }

    private boolean hasZombieAhead(Plant plant, GameContext ctx) {
        for (Zombie zombie : ctx.getAliveZombies()) {
            if (zombie == null || zombie.isDead()) {
                continue;
            }

            if (zombie.getRow() != plant.getRow()) {
                continue;
            }

            if (zombie.getX() >= plant.getX()) {
                return true;
            }
        }

        return false;
    }
}
