package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.plants.plantAbilities.BaseAbility;
import model.plants.plantAbilities.ExplosiveType;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

import static model.plants.plantAbilities.ExplosiveType.LANE_FIRE;

public class Explosive implements BaseAbility {


    private void applyDamageToTiles(int damage, List<int[]> targetTiles, GameContext ctx) {
        for (int[] pos : targetTiles) {
            int r = pos[0];
            int c = pos[1];

            if (r >= 0 && c >= 0) {
                List<Zombie> targets = ctx.findTargets(r, c, TargetingMode.NONE);
                if (targets != null && !targets.isEmpty()) {
                    for (Zombie target : targets) {
                        target.setHp(target.getHp() - damage);
                    }
                }
            }
        }
    }

    public void triggerAbility(ExplosiveType type, int damage, Plant plant, GameContext ctx) {
        int pRow = plant.getRow();
        int pCol = plant.getCol();

        List<int[]> areaTiles = new ArrayList<>();

        switch (type) {
            case INSTANT_AOE:
                areaTiles = get3x3Tiles(pRow, pCol);
                applyDamageToTiles(damage, areaTiles, ctx);
                plant.setDamage("99999");
                break;

            case LANE_FIRE:
                areaTiles = getLaneTiles(pRow);
                applyDamageToTiles(damage, areaTiles, ctx);
                plant.setDamage("99999");
                break;

            case BOARD_WIDE:
                areaTiles = getAllBoardTiles();
                applyDamageToTiles(damage, areaTiles, ctx);
                plant.setDamage("99999");
                break;

            case CRUSH:
                List<Zombie> targets = ctx.findTargets(pRow, pCol, TargetingMode.NONE);
                if (targets != null && !targets.isEmpty()) {
                    Zombie firstZombie = targets.get(0);
                    firstZombie.setHp(firstZombie.getHp() - damage);
                    plant.setDamage("99999");
                }
                break;

            case TIMED_MINE:
            case TIMED_MINE_AOE:
                int currentSecond = ctx.getTimeManager().getTotalSeconds();
                int timeAlive = currentSecond - plant.getLastActionSecond();

                int delay = (type == ExplosiveType.TIMED_MINE_AOE) ? 5 : 15;

                    List<Zombie> contactZombies = ctx.findTargets(pRow, pCol, TargetingMode.NONE);
                    if (contactZombies != null && !contactZombies.isEmpty()) {

                        if (type == ExplosiveType.TIMED_MINE) {
                            areaTiles.add(new int[]{pRow, pCol});
                        }
                        else {
                            areaTiles = get3x3Tiles(pRow, pCol);
                        }

                        applyDamageToTiles(damage, areaTiles, ctx);
                        plant.setDamage("99999");}

                break;

            case INSTANT_AOE_SHRAPNEL:
                areaTiles = get3x3Tiles(pRow, pCol);
                applyDamageToTiles(damage, areaTiles, ctx);
                plant.setDamage("99999");
                break;

            case FREEZE_TRAP:
                List<Zombie> stepZombies = ctx.findTargets(pRow, pCol, TargetingMode.NONE);
                if (stepZombies != null && !stepZombies.isEmpty()) {
                    Zombie firstZombie = stepZombies.get(0);
                    plant.setDamage("99999");
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
                forEgyptAndDarkEra(plant, ctx);
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


    public void waterExplosion(Plant plant, GameContext ctx) { }
    public void ice(Plant plant, GameContext ctx) {  }
    public void forIcedCave(Plant plant, GameContext ctx) { }
    public void forEgyptAndDarkEra(Plant plant, GameContext ctx) {  }

    @Override
    public void activate(Plant self, GameContext ctx) {
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
    }
}