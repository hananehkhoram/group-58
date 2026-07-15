package model.plants.plantAbilities;

import model.GameContext;
import model.mechanisms.GameEngine;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class WallNut implements BaseAbility {

    @Override
    public void activate(Plant self, GameContext ctx) {
    }

    public void triggerAbility(WallNutType wallNutType, int damage, Plant self, GameEngine engine) {

        int pRow = self.getRow();
        int pCol = self.getCol();
        GameContext ctx = engine.getCtx();

        switch (wallNutType) {

            case BLOCKER:
            case TALL_BLOCKER:
            case STACKABLE_COVER:
                break;

            case REFLECTIVE:
                List<Zombie> attackers = engine.findTargets(pRow, pCol, TargetingMode.NONE);
                if (attackers != null && !attackers.isEmpty()) {
                    for (Zombie z : attackers) {
                        z.takeDamage(damage);
                    }
                }
                break;

            case LANE_REDIRECT:
                List<Zombie> biters = engine.findTargets(pRow, pCol, TargetingMode.NONE);
                if (biters != null && !biters.isEmpty()) {
                    int maxRows = ctx.getLevel().getRows();
                    for (Zombie z : biters) {
                        int newRow = pRow;
                        if (pRow == 0) {
                            newRow = 1;
                        } else if (pRow == maxRows - 1) {
                            newRow = pRow - 1;
                        } else {
                            newRow = (Math.random() < 0.5) ? pRow - 1 : pRow + 1;
                        }
                        z.setY(newRow);
                    }
                }
                break;

            case LANE_ATTRACT:
                for (Zombie z : ctx.getAliveZombies()) {
                    if (!z.isDead() && Math.abs(z.getRow() - pRow) == 1) {
                        if (Math.abs(z.getX() - pCol) <= 1.0) {
                            z.setY(pRow);
                        }
                    }
                }
                break;

            case SUN_GENERATING:
                ctx.produceSun(pCol, pRow, 5);
                break;
        }
    }

    public void wall(WallNutType wallNutType, Plant plant, GameContext ctx) {
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        if (mode == PlantFoodMode.GRANT_ARMOR) {
            self.heal(self.getHp() + 8000);
        }
    }
}