package model.plants.plantAbilities;

import model.GameContext;
import model.mechanisms.GameEngine;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.projectile.Projectile;
import model.projectile.BulletType;
import model.projectile.TrajectoryType;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class Explosive implements BaseAbility {

    private void applyDamageToTiles(int damage, List<int[]> targetTiles, GameEngine engine) {
        for (int[] pos : targetTiles) {
            int r = pos[0];
            int c = pos[1];

            List<Zombie> targets = engine.findTargets(r, c, TargetingMode.NONE);
            if (targets != null && !targets.isEmpty()) {
                for (Zombie target : targets) {
                    target.takeDamage(damage);
                }
            }
        }
    }


    public void triggerAbility(ExplosiveType type, int damage, Plant plant, GameEngine engine) {
        GameContext ctx = engine.getCtx();
        int pRow = plant.getRow();
        int pCol = plant.getCol();

        switch (type) {
            case INSTANT_AOE:
                applyAreaDamageAndRemove(get3x3Tiles(pRow, pCol, ctx), damage, plant, engine);
                break;
            case LANE_FIRE:
                applyAreaDamageAndRemove(getLaneTiles(pRow, ctx), damage, plant, engine);
                break;
            case BOARD_WIDE:
                applyAreaDamageAndRemove(getAllBoardTiles(ctx), damage, plant, engine);
                break;
            case CRUSH:
                executeCrush(damage, plant, engine);
                break;
            case TIMED_MINE:
            case TIMED_MINE_AOE:
                executeTimedMine(type, damage, plant, engine);
                break;
            case INSTANT_AOE_SHRAPNEL:
                executeShrapnel(damage, plant, engine);
                break;
            case FREEZE_TRAP:
                executeFreezeTrap(plant, engine);
                break;
            case WATER_TRAP:
                waterExplosion(plant, ctx, engine);
                break;
            case BOARD_WIDE_FREEZE:
                ice(plant, ctx, engine);
                break;
            case MELT_AREA:
                forIcedCave(plant, ctx, engine);
                break;
            case GRAVE_DESTROY:
                executeGraveDestroy(plant, engine);
                break;
        }
    }

    private void applyAreaDamageAndRemove(List<int[]> tiles, int damage, Plant plant, GameEngine engine) {
        applyDamageToTiles(damage, tiles, engine);
        engine.removePlant(plant.getRow(), plant.getCol());
    }

    private void executeCrush(int damage, Plant plant, GameEngine engine) {
        List<Zombie> targets = engine.findTargets(plant.getRow(), plant.getCol(), TargetingMode.NONE);
        if (targets != null && !targets.isEmpty()) {
            Zombie firstZombie = targets.get(0);
            firstZombie.takeDamage(damage);
            engine.removePlant(plant.getRow(), plant.getCol());
        }
    }

    private void executeTimedMine(ExplosiveType type, int damage, Plant plant, GameEngine engine) {
        GameContext ctx = engine.getCtx();
        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        int timeAlive = currentSecond - plant.getLastActionSecond();
        int delay = (type == ExplosiveType.TIMED_MINE_AOE) ? 5 : 15;

        if (timeAlive >= delay) {
            List<Zombie> contactZombies = engine.findTargets(plant.getRow(), plant.getCol(), TargetingMode.NONE);
            if (contactZombies != null && !contactZombies.isEmpty()) {
                List<int[]> areaTiles = new ArrayList<>();
                if (type == ExplosiveType.TIMED_MINE) {
                    areaTiles.add(new int[]{plant.getRow(), plant.getCol()});
                } else {
                    areaTiles = get3x3Tiles(plant.getRow(), plant.getCol(), ctx);
                }
                applyAreaDamageAndRemove(areaTiles, damage, plant, engine);
            }
        }
    }

    private void executeShrapnel(int damage, Plant plant, GameEngine engine) {
        GameContext ctx = engine.getCtx();
        int pRow = plant.getRow();
        int pCol = plant.getCol();

        List<int[]> areaTiles = get3x3Tiles(pRow, pCol, ctx);
        applyDamageToTiles(damage, areaTiles, engine);

        int maxRows = ctx.getLevel().getRows();
        for (int r = pRow - 1; r <= pRow + 1; r++) {
            if (r >= 0 && r < maxRows) {
                Projectile shrapnel = new Projectile(
                        damage / 2, pCol, 0, r, 4.0,
                        BulletType.NORMAL, TrajectoryType.LOBBED, false, plant
                );
                ctx.setNewProjectiles(shrapnel);
            }
        }
        engine.removePlant(pRow, pCol);
    }

    private void executeFreezeTrap(Plant plant, GameEngine engine) {
        List<Zombie> stepZombies = engine.findTargets(plant.getRow(), plant.getCol(), TargetingMode.NONE);
        if (stepZombies != null && !stepZombies.isEmpty()) {
            Zombie firstZombie = stepZombies.get(0);
            firstZombie.applySlowOrFreeze();
            engine.removePlant(plant.getRow(), plant.getCol());
        }
    }

    private void executeGraveDestroy(Plant plant, GameEngine engine) {
        GameContext ctx = engine.getCtx();
        int pRow = plant.getRow();
        int pCol = plant.getCol();

        if (pRow >= 0 && pRow < ctx.getLevel().getRows() && pCol >= 0 && pCol < ctx.getLevel().getColumns()) {
            if (ctx.getGraveGrid()[pRow][pCol] != null) {
                ctx.removeGrave(pRow, pCol);
                engine.removePlant(pRow, pCol);
            }
        }
    }

    private List<int[]> get3x3Tiles(int pRow, int pCol, GameContext ctx) {
        List<int[]> tiles = new ArrayList<>();
        int maxRows = ctx.getLevel().getRows();
        int maxCols = ctx.getLevel().getColumns();

        for (int r = pRow - 1; r <= pRow + 1; r++) {
            for (int c = pCol - 1; c <= pCol + 1; c++) {
                if (r >= 0 && r < maxRows && c >= 0 && c < maxCols) {
                    tiles.add(new int[]{r, c});
                }
            }
        }
        return tiles;
    }

    private List<int[]> getLaneTiles(int pRow, GameContext ctx) {
        List<int[]> tiles = new ArrayList<>();
        int maxCols = ctx.getLevel().getColumns();
        for (int c = 0; c < maxCols; c++) {
            tiles.add(new int[]{pRow, c});
        }
        return tiles;
    }

    private List<int[]> getAllBoardTiles(GameContext ctx) {
        List<int[]> tiles = new ArrayList<>();
        int maxRows = ctx.getLevel().getRows();
        int maxCols = ctx.getLevel().getColumns();
        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < maxCols; c++) {
                tiles.add(new int[]{r, c});
            }
        }
        return tiles;
    }

    public void waterExplosion(Plant plant, GameContext ctx, GameEngine engine) {
        int r = plant.getRow();
        int c = plant.getCol();
        List<Zombie> targets = engine.findTargets(r, c, TargetingMode.NONE);
        if (targets != null && !targets.isEmpty()) {
            Zombie target = targets.get(0);
            target.takeDamage(9999);
            engine.removePlant(r, c);
        }
    }

    public void ice(Plant plant, GameContext ctx, GameEngine engine) {
        for (Zombie z : ctx.getAliveZombies()) {
            z.applySlowOrFreeze();
        }
        engine.removePlant(plant.getRow(), plant.getCol());
    }

    public void forIcedCave(Plant plant, GameContext ctx, GameEngine engine) {
        int pRow = plant.getRow();
        int pCol = plant.getCol();

        for (int[] pos : get3x3Tiles(pRow, pCol, ctx)) {
            int r = pos[0];
            int c = pos[1];
            if (ctx.getGameEngine().getTiles(r, c) != null) {
                ctx.getGameEngine().getTiles(r, c).meltIce();
            }
        }
        engine.removePlant(pRow, pCol);
    }


    @Override
    public void activate(Plant self, GameContext ctx) {}

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        String type = self.getAbilityParams().get("explosiveType");

        switch (mode) {
            case INSTANT_CONSUME:
                self.setLastActionSecond(ctx.getTimeManager().getTotalSeconds() - 999);
                spawnClones(self, ctx, 2);
                break;

            case MULTI_TARGET_BURST:
                if ("CRUSH".equals(type)) { // Squash: له کردن ۲ زامبی تصادفی
                    crushRandomZombies(ctx, 2);
                } else if ("WATER_TRAP".equals(type)) { // Tangle Kelp: کشیدن چند زامبی تصادفی زیر آب
                    drownRandomWaterZombies(ctx, 3);
                } else if ("FREEZE_TRAP".equals(type)) { // Iceberg Lettuce: یخ زدن تمام زامبی‌های موجود
                    for (Zombie z : ctx.getAliveZombies()) {
                        if (!z.isDead()) z.applySlowOrFreeze();
                    }
                }
                break;

            case GRANT_ARMOR:
                self.heal(4000);
                break;

            default:
                break;
        }
        view.ConsoleView.showMessage("Plant Food: " + self.getName() + " activated!");
    }

    private void crushRandomZombies(GameContext ctx, int count) {
        List<Zombie> alive = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) if (!z.isDead()) alive.add(z);
        java.util.Collections.shuffle(alive);
        for (int i = 0; i < Math.min(count, alive.size()); i++) {
            alive.get(i).takeDamage(Integer.MAX_VALUE);
        }
    }

    private void drownRandomWaterZombies(GameContext ctx, int count) {
        List<Zombie> inWater = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if (!z.isDead() && ctx.getSeason().isWaterCell(z.getRow(), (int) z.getX(), ctx)) {
                inWater.add(z);
            }
        }
        java.util.Collections.shuffle(inWater);
        for (int i = 0; i < Math.min(count, inWater.size()); i++) {
            inWater.get(i).takeDamage(9999);
        }
    }

    private void spawnClones(Plant self, GameContext ctx, int count) {
        int pRow = self.getRow();
        int pCol = self.getCol();
        int maxRows = ctx.getLevel().getRows();
        int maxCols = ctx.getLevel().getColumns();
        int placed = 0;

        for (int dr = -1; dr <= 1 && placed < count; dr++) {
            for (int dc = -1; dc <= 1 && placed < count; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = pRow + dr, c = pCol + dc;
                if (r < 0 || r >= maxRows || c < 0 || c >= maxCols) continue;
                if (ctx.getPlantGrid()[r][c] != null) continue;

                Plant clone = ctx.getPlantFactory().create(self.getName());
                if (clone == null) continue;
                clone.setRow(r);
                clone.setCol(c);
                clone.setLastActionSecond(ctx.getTimeManager().getTotalSeconds() - 999);
                ctx.getPlantGrid()[r][c] = clone;
                ctx.getAlivePlants().add(clone);
                ctx.recordPlantPlaced(clone, r, c);
                placed++;
            }
        }
    }

}