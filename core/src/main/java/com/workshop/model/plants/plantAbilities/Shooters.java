package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.TargetingMode;
import com.workshop.model.plants.enums.ShootType;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.ProjectileVisualVariant;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.Armor;

import java.util.ArrayList;
import java.util.List;

public class Shooters implements BaseAbility {

    private static final double DEFAULT_PROJECTILE_SPEED = 1.0;
    private static final double PEA_MOUTH_X_FROM_CENTER = 0.34;
    private static final double PEA_MOUTH_Y_FROM_CENTER = 0.10;
    private static final int BARRAGE_VOLLEYS = 28;
    private static final int GIANT_PEA_DAMAGE_MULTIPLIER = 20;

    private int barrageVolleysLeft;
    private int barrageGiantLeft;
    private int barrageDamage;
    private int barrageAmount;
    private ShootType barrageShootType;
    private BulletType barrageBulletType;

    @Override
    public void activate(Plant self, GameContext ctx) {
        if (self == null || ctx == null) return;

        tickPlantFoodBarrage(self, ctx);

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
        if (isBarraging() || self.hasPendingShots()) return;

        if (shootType == ShootType.STRAIGHT
            || shootType == ShootType.STRAIGHT_SEQUENTIAL
            || shootType == ShootType.SHORT_RANGE
            || shootType == ShootType.PIERCING) {

            if (!hasTargetAhead(self, ctx)) {
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
        double startX = launchX(self, 1.0);
        double[] headOffsets = {-0.32, 0.0, 0.32};
        int[] laneDeltas = {-1, 0, 1};
        List<Projectile> shots = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            int targetRow = Math.max(0, Math.min(totalRows - 1, originRow + laneDeltas[i]));
            double startY = launchY(originRow + headOffsets[i], self);
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

        boolean radial = shootType == ShootType.QUAD_DIAGONAL
            || shootType == ShootType.STAR_BURST;

        List<Projectile> shots = new ArrayList<>();
        for (int row : lanes) {
            for (double[] dir : directions) {
                for (int i = 0; i < shotsPerLane; i++) {
                    double startX;
                    double startY;
                    if (radial) {
                        startX = self.getX() + 0.5 + dir[0] * 0.32;
                        startY = row + dir[1] * 0.20;
                    } else {
                        startX = launchX(self, dir[0]) + dir[0] * 0.3 * i;
                        startY = launchY(row, self) + dir[1] * 0.3 * i;
                    }
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
        int damage = parseDamage(self);

        switch (mode) {
            case BARRAGE -> startBarrage(self, amount, shootType, bulletType, damage);
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

    private void startBarrage(
        Plant self,
        int amount,
        ShootType shootType,
        BulletType bulletType,
        int damage
    ) {
        self.discardPendingShots();
        barrageAmount = Math.max(1, amount);
        barrageShootType = shootType;
        barrageBulletType = bulletType;
        barrageDamage = Math.max(1, damage);
        barrageVolleysLeft = BARRAGE_VOLLEYS;
        barrageGiantLeft = giantPeaCount(shootType, barrageAmount);
        self.setPlantFoodActive(true);
        self.startPlantFoodGlow((barrageVolleysLeft + barrageGiantLeft) * 0.1f + 0.4f);
    }

    private static int giantPeaCount(ShootType shootType, int amount) {
        if (shootType == ShootType.STRAIGHT_SEQUENTIAL) {
            return amount >= 4 ? 4 : 1;
        }
        return 0;
    }

    private boolean isBarraging() {
        return barrageVolleysLeft > 0 || barrageGiantLeft > 0;
    }

    private void tickPlantFoodBarrage(Plant self, GameContext ctx) {
        if (!isBarraging()) {
            return;
        }

        GameEngine engine = ctx.getGameEngine();
        List<Projectile> shots;
        if (barrageVolleysLeft > 0) {
            shots = createBarrageVolley(self, ctx, engine);
            barrageVolleysLeft--;
        } else {
            shots = createGiantPea(self, barrageDamage * GIANT_PEA_DAMAGE_MULTIPLIER);
            barrageGiantLeft--;
        }

        if (!shots.isEmpty()) {
            spawnNow(ctx, shots);
            ctx.queuePlantAttackAnimation(self);
        }

        if (!isBarraging()) {
            self.setPlantFoodActive(false);
        }
    }

    private List<Projectile> createBarrageVolley(
        Plant self,
        GameContext ctx,
        GameEngine engine
    ) {
        int empoweredDamage = barrageDamage * 2;
        if (barrageShootType == ShootType.RANDOM_HOMING
            || barrageShootType == ShootType.NEAREST_TARGET) {
            return createHoming(
                empoweredDamage,
                barrageBulletType,
                barrageShootType,
                self,
                ctx,
                engine
            );
        }
        if (barrageShootType == ShootType.TRI_LANE) {
            return createTriLane(empoweredDamage, barrageBulletType, self, ctx);
        }
        return createDirectional(
            empoweredDamage,
            barrageAmount,
            barrageShootType,
            barrageBulletType,
            self,
            ctx
        );
    }

    private List<Projectile> createGiantPea(Plant self, int damage) {
        double startX = launchX(self, 1.0);
        double startY = launchY(self.getRow(), self);
        return List.of(new Projectile(
            damage,
            startX,
            startY,
            self.getRow(),
            DEFAULT_PROJECTILE_SPEED,
            barrageBulletType,
            TrajectoryType.STRAIGHT,
            false,
            1.0,
            0.0,
            self,
            ProjectileVisualVariant.GIANT
        ));
    }

    private static double launchX(Plant self, double dirX) {
        double facing = dirX < 0 ? -1.0 : 1.0;
        return self.getX() + 0.5 + facing * mouthXFromCenter(self);
    }

    private static double launchY(double rowY, Plant self) {
        return rowY - mouthYFromCenter(self);
    }

    private static double mouthXFromCenter(Plant self) {
        if (self.isPeaFamily()) {
            return PEA_MOUTH_X_FROM_CENTER;
        }
        String compact = compactPlantName(self);
        if (compact.contains("PUFFSHROOM")) {
            return 0.34;
        }
        if (compact.contains("SEASHROOM")) {
            return 0.32;
        }
        if (compact.contains("FUMESHROOM")) {
            return 0.40;
        }
        if (compact.contains("CACTUS")) {
            return 0.36;
        }
        return 0.28;
    }

    private static double mouthYFromCenter(Plant self) {
        if (self.isPeaFamily()) {
            return PEA_MOUTH_Y_FROM_CENTER;
        }
        String compact = compactPlantName(self);
        if (compact.contains("PUFFSHROOM")) {
            return 0.08;
        }
        if (compact.contains("SEASHROOM")) {
            return -0.10;
        }
        if (compact.contains("FUMESHROOM")) {
            return 0.30;
        }
        if (compact.contains("CACTUS")) {
            return 0.22;
        }
        return 0.10;
    }

    private static String compactPlantName(Plant self) {
        if (self == null || self.getName() == null) {
            return "";
        }
        return self.getName().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private boolean hasTargetAhead(Plant plant, GameContext ctx) {
        return ctx.hasHostileAhead(plant.getRow(), plant.getX());
    }
}
