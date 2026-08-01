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
        if (mode != PlantFoodMode.GRANT_ARMOR) return;

        String wallNutType = self.getAbilityParams().get("wallNutType");
        int pRow = self.getRow();
        int pCol = self.getCol();

        switch (wallNutType) {
            case "BLOCKER":              // Wall-nut
                self.heal(4000);
                break;

            case "TALL_BLOCKER":         // Tall-nut
                self.heal(8000);
                break;

            case "REFLECTIVE":           // Endurian: زره فلزی + بازتاب دمیج
                self.heal(4000);
                break;

            case "STACKABLE_COVER":      // Pumpkin: زره فلزی قدرتمند
            case "SUN_GENERATING":       // Sun Bean: زره فلزی قدرتمند
                self.heal(6000);
                break;

            case "LANE_REDIRECT":        // Garlic: انتقال اجباری تمام زامبی‌های لاین
                int maxRows = ctx.getLevel().getRows();
                for (Zombie z : ctx.getAliveZombies()) {
                    if (!z.isDead() && z.getRow() == pRow) {
                        int newRow;
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

            case "LANE_ATTRACT":         // Sweet Potato: جذب اطراف + بازیابی کامل جان
                for (Zombie z : ctx.getAliveZombies()) {
                    if (!z.isDead() && Math.abs(z.getRow() - pRow) <= 1) {
                        z.setY(pRow);
                    }
                }
                int missingHp = self.getBaseHp() - self.getHp();
                if (missingHp > 0) self.heal(missingHp);
                break;

            default:
                break;
        }
    }
}