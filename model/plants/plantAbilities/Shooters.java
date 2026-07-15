package model.plants.plantAbilities;

import model.GameContext;
import model.mechanisms.GameEngine;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.projectile.BulletType;
import model.projectile.Projectile;
import model.projectile.TrajectoryType;
import model.plants.enums.ShootType;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class Shooters implements BaseAbility {

    private static final double DEFAULT_PROJECTILE_SPEED = 8.0;

    @Override
    public void activate(Plant self, GameContext ctx) {
        //check for striker here
    }

    public void shoot(String damage, int amount, String interval, ShootType shootType, BulletType bulletType, Plant self, GameEngine engine) {
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

        boolean hasShot;
        if (shootType == ShootType.RANDOM_HOMING || shootType == ShootType.NEAREST_TARGET) {
            hasShot = shootHoming(damage, bulletType, shootType, self, ctx, engine);
        } else {
            hasShot = shootDirectional(damage, amount, shootType, bulletType, self, ctx);
        }

        if (hasShot && !everyRound) {
            self.setLastActionSecond(currentSecond);
        }
    }

    /** شلیک‌های مستقیم/چندلاینی/سریالی/موربی (هرکدوم یک Projectile واقعی با جهت درست خودشون می‌فرستن) */
    private boolean shootDirectional(String damage, int amount, ShootType shootType, BulletType bulletType, Plant self, GameContext ctx) {
        int damageOfPlant = Integer.parseInt(damage);

        List<Integer> lanes = resolveLanes(shootType, self, ctx);
        if (lanes.isEmpty()) return false;

        List<double[]> directions = resolveDirections(shootType);

        TrajectoryType trajectory = switch (shootType) {
            case PIERCING -> TrajectoryType.PIERCING;
            case BOWLING -> TrajectoryType.BOWLING;
            default -> TrajectoryType.STRAIGHT;
        };

        // amount فقط برای STRAIGHT_SEQUENTIAL (Repeater) به معنی «چندبار پشت‌سرهم شلیک کن»ه؛
        // برای TRI_LANE به معنی «چند لاین» است (در resolveLanes مدیریت شده)، و برای شلیک‌های چندجهته
        // (QUAD_DIAGONAL/FRONT_AND_BACK/STAR_BURST) هرجهت خودش یک شلیک همزمانه، نه تکراری.
        int shotsPerLane = (shootType == ShootType.STRAIGHT_SEQUENTIAL) ? Math.max(1, amount) : 1;

        boolean firedAny = false;
        for (int row : lanes) {
            for (double[] dir : directions) {
                for (int i = 0; i < shotsPerLane; i++) {
                    double startX = self.getX() + dir[0] * 0.3 * i;
                    double startY = row + dir[1] * 0.3 * i;
                    Projectile p = new Projectile(damageOfPlant, startX, startY, row,
                            DEFAULT_PROJECTILE_SPEED, bulletType, trajectory, false, dir[0], dir[1]);
                    ctx.setNewProjectiles(p);
                    firedAny = true;
                }
            }
        }
        return firedAny;
    }

    /**
     * بردار جهت (یکه) هر شلیک، جدا از لِین. اکثر انواع فقط رو به جلو (۱و۰) شلیک می‌کنن؛
     * سه مورد نیاز به جهت واقعی مورب/رو-به-عقب دارن که حالا Projectile ازش پشتیبانی می‌کنه:
     */
    private List<double[]> resolveDirections(ShootType shootType) {
        List<double[]> dirs = new ArrayList<>();
        switch (shootType) {
            case QUAD_DIAGONAL -> {
                double d = 1.0 / Math.sqrt(2); // Rotobaga: چهار جهت مورب هم‌زمان
                dirs.add(new double[]{d, d});
                dirs.add(new double[]{d, -d});
                dirs.add(new double[]{-d, d});
                dirs.add(new double[]{-d, -d});
            }
            case FRONT_AND_BACK -> {
                dirs.add(new double[]{1, 0});  // Split Pea: یک شلیک رو به جلو
                dirs.add(new double[]{-1, 0}); // و یکی رو به عقب
            }
            case STAR_BURST -> {
                for (int i = 0; i < 5; i++) { // Starfruit: پنج شلیک هم‌زمان به شکل ستاره
                    double angle = Math.toRadians(72 * i);
                    dirs.add(new double[]{Math.cos(angle), Math.sin(angle)});
                }
            }
            default -> dirs.add(new double[]{1, 0}); // بقیه: یک شلیک ساده رو به جلو
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

    private boolean shootHoming(String damage, BulletType bulletType, ShootType shootType, Plant self, GameContext ctx, GameEngine engine) {
        int damageOfPlant = Integer.parseInt(damage);

        TargetingMode mode = (shootType == ShootType.NEAREST_TARGET) ? TargetingMode.NEAREST : TargetingMode.RANDOM;
        List<Zombie> candidates = engine.findTargets(self.getRow(), self.getCol(), mode);
        if (candidates == null || candidates.isEmpty()) return false;
        Zombie target = candidates.get(0);

        Projectile p = new Projectile(damageOfPlant, self.getX(), self.getRow(), self.getRow(),
                DEFAULT_PROJECTILE_SPEED, bulletType, TrajectoryType.HOMING, false);
        p.setHomingTarget(target);
        ctx.setNewProjectiles(p);
        return true;
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // BARRAGE for most, MULTI_TARGET_BURST for Caulipower/Electric Blueberry, SELF_RESET for Sea/Puff-shroom
    }

}