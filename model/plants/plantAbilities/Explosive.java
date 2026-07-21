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

            if (r >= 0 && c >= 0) {
                List<Zombie> targets = engine.findTargets(r, c, TargetingMode.NONE);
                if (targets != null && !targets.isEmpty()) {
                    for (Zombie target : targets) {
                        target.takeDamage(damage);
                    }
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
                areaTiles = get3x3Tiles(pRow, pCol);
                applyDamageToTiles(damage, areaTiles, engine);
                engine.removePlant(pRow, pCol);
                break;

            case LANE_FIRE:
                areaTiles = getLaneTiles(pRow);
                applyDamageToTiles(damage, areaTiles, engine);
                engine.removePlant(pRow, pCol);
                break;

            case BOARD_WIDE:
                areaTiles = getAllBoardTiles();
                applyDamageToTiles(damage, areaTiles, engine);
                engine.removePlant(pRow, pCol);
                break;

            case CRUSH:
                List<Zombie> targets = engine.findTargets(pRow, pCol, TargetingMode.NONE);
                if (targets != null && !targets.isEmpty()) {
                    Zombie firstZombie = targets.get(0);
                    firstZombie.takeDamage(damage);
                    engine.removePlant(pRow, pCol);
                }
                break;

            case TIMED_MINE:
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
                            areaTiles = get3x3Tiles(pRow, pCol);
                        }
                        applyDamageToTiles(damage, areaTiles, engine);
                        engine.removePlant(pRow, pCol);
                    }
                }
                break;

            case INSTANT_AOE_SHRAPNEL:
                areaTiles = get3x3Tiles(pRow, pCol);
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
                waterExplosion(plant, ctx);
                break;
            case BOARD_WIDE_FREEZE:
                ice(plant, ctx);
                break;
            case MELT_AREA:
                forIcedCave(plant, ctx);
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

    private List<int[]> get3x3Tiles(int pRow, int pCol) {
        List<int[]> tiles = new ArrayList<>();
        for (int r = pRow - 1; r <= pRow + 1; r++) {
            for (int c = pCol - 1; c <= pCol + 1; c++) {
                tiles.add(new int[]{r, c});
            }
        }
        return tiles;
    }

    private List<int[]> getLaneTiles(int pRow) {
        List<int[]> tiles = new ArrayList<>();
        int maxCols = 9;
        for (int c = 0; c < maxCols; c++) {
            tiles.add(new int[]{pRow, c});
        }
        return tiles;
    }

    private List<int[]> getAllBoardTiles() {
        List<int[]> tiles = new ArrayList<>();
        int maxRows = 5;
        int maxCols = 9;
        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < maxCols; c++) {
                tiles.add(new int[]{r, c});
            }
        }
        return tiles;
    }

    public void waterExplosion(Plant plant, GameContext ctx) {}
    public void ice(Plant plant, GameContext ctx) {}
    public void forIcedCave(Plant plant, GameContext ctx) {}
    public void forEgyptAndDarkEra(Plant plant, GameContext ctx) {}

    @Override
    public void activate(Plant self, GameContext ctx) {}

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {}
}