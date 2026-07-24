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
        int pRow = plant.getRow();
        int pCol = plant.getCol();
        GameContext ctx = engine.getCtx();

        List<int[]> areaTiles = new ArrayList<>();

        switch (type) {
            case INSTANT_AOE:
                areaTiles = get3x3Tiles(pRow, pCol, ctx);
                applyDamageToTiles(damage, areaTiles, engine);
                engine.removePlant(pRow, pCol);
                break;

            case LANE_FIRE:
                areaTiles = getLaneTiles(pRow, ctx);
                applyDamageToTiles(damage, areaTiles, engine);
                engine.removePlant(pRow, pCol);
                break;

            case BOARD_WIDE:
                areaTiles = getAllBoardTiles(ctx);
                applyDamageToTiles(damage, areaTiles, engine);
                engine.removePlant(pRow, pCol);
                break;

            case CRUSH: // مثل Squash
                List<Zombie> targets = engine.findTargets(pRow, pCol, TargetingMode.NONE);
                if (targets != null && !targets.isEmpty()) {
                    Zombie firstZombie = targets.get(0);
                    firstZombie.takeDamage(damage);
                    engine.removePlant(pRow, pCol);
                }
                break;

            case TIMED_MINE: // Potato Mine
            case TIMED_MINE_AOE:
                int currentSecond = ctx.getTimeManager().getTotalSeconds();
                int timeAlive = currentSecond - plant.getLastActionSecond();
                int delay = (type == ExplosiveType.TIMED_MINE_AOE) ? 5 : 15;

                if (timeAlive >= delay) {
                    List<Zombie> contactZombies = engine.findTargets(pRow, pCol, TargetingMode.NONE);
                    if (contactZombies != null && !contactZombies.isEmpty()) {
                        if (type == ExplosiveType.TIMED_MINE) {
                            areaTiles.add(new int[]{pRow, pCol});
                        } else {
                            areaTiles = get3x3Tiles(pRow, pCol, ctx);
                        }
                        applyDamageToTiles(damage, areaTiles, engine);
                        engine.removePlant(pRow, pCol);
                    }
                }
                break;

            case INSTANT_AOE_SHRAPNEL:
                areaTiles = get3x3Tiles(pRow, pCol, ctx);
                applyDamageToTiles(damage, areaTiles, engine);

                int maxRows = ctx.getLevel().getRows();
                for (int r = pRow - 1; r <= pRow + 1; r++) {
                    if (r >= 0 && r < maxRows) {
                        Projectile shrapnel = new Projectile(
                                damage / 2,
                                pCol, 0, r,
                                4.0,
                                BulletType.NORMAL,
                                TrajectoryType.LOBBED,
                                false,
                                plant
                        );
                        ctx.setNewProjectiles(shrapnel);
                    }
                }
                engine.removePlant(pRow, pCol);
                break;

            case FREEZE_TRAP:
                List<Zombie> stepZombies = engine.findTargets(pRow, pCol, TargetingMode.NONE);
                if (stepZombies != null && !stepZombies.isEmpty()) {
                    Zombie firstZombie = stepZombies.get(0);
                    firstZombie.applySlowOrFreeze();
                    engine.removePlant(pRow, pCol);
                }
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
                if (pRow >= 0 && pRow < ctx.getLevel().getRows() && pCol >= 0 && pCol < ctx.getLevel().getColumns()) {
                    if (ctx.getGraveGrid()[pRow][pCol] != null) {
                        ctx.removeGrave(pRow, pCol);
                        engine.removePlant(pRow, pCol);
                    }
                }
                break;
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
        int r = plant.getRow();
        int c = plant.getCol();
        if (ctx.getGameEngine().getTiles(r, c) != null) {
            ctx.getGameEngine().getTiles(r, c).meltIce();
        }
        engine.removePlant(r, c);
    }


    @Override
    public void activate(Plant self, GameContext ctx) {}

}