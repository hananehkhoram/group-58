package model.plants.plantAbilities;

import controller.MenuManager;
import model.GameContext;
import model.mechanisms.GameEngine;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.zombie.Zombie;

import java.util.List;

public class MeleeAttackers implements BaseAbility {

    @Override
    public void activate(Plant self, GameContext ctx) {
    }

    public void melee(String meleeKind, int damage, Plant plant, GameEngine engine) {
        int pRow = plant.getRow();
        int pCol = plant.getCol();
        int currentSecond = engine.getCtx().getTimeManager().getTotalSeconds();

        switch (meleeKind) {
            case "FRONT_BACK":
                Zombie targetFB = findTargetFrontOrBack(pRow, pCol, engine);
                if (targetFB != null) {
                    targetFB.takeDamage(damage);
                    plant.setLastActionSecond(currentSecond);
                }
                break;

            case "FRONT_BACK_FIRE":
                Zombie targetFBF = findTargetFrontOrBack(pRow, pCol, engine);
                if (targetFBF != null) {
                    targetFBF.takeDamage(damage);
                    targetFBF.meltIce();
                    plant.setLastActionSecond(currentSecond);
                }
                break;

            case "AOE_3X3":
            case "GROWING_AOE":
                boolean hitAnyone = applyAoEDamage(pRow, pCol, damage, engine);
                if (hitAnyone) {
                    plant.setLastActionSecond(currentSecond);
                }
                break;
        }
    }

    public void instantEat(Plant plant, GameEngine engine) {
        int currentSecond = engine.getCtx().getTimeManager().getTotalSeconds();

        if (currentSecond - plant.getLastActionSecond() < 40) {
            return;
        }

        int pRow = plant.getRow();
        int pCol = plant.getCol();

        List<Zombie> targets = engine.findTargets(pRow, pCol, TargetingMode.NONE);
        if (targets == null || targets.isEmpty()) {
            targets = engine.findTargets(pRow, pCol + 1, TargetingMode.NONE);
        }

        if (targets != null && !targets.isEmpty()) {
            Zombie target = targets.get(0);
            target.takeDamage(Integer.MAX_VALUE);
            plant.setLastActionSecond(currentSecond);
        }
    }

    private Zombie findTargetFrontOrBack(int r, int c, GameEngine engine) {
        List<Zombie> front = engine.findTargets(r, c + 1, TargetingMode.NONE);
        if (front != null && !front.isEmpty()) return front.get(0);

        List<Zombie> current = engine.findTargets(r, c, TargetingMode.NONE);
        if (current != null && !current.isEmpty()) return current.get(0);

        List<Zombie> back = engine.findTargets(r, c - 1, TargetingMode.NONE);
        if (back != null && !back.isEmpty()) return back.get(0);

        return null;
    }

    private boolean applyAoEDamage(int pRow, int pCol, int damage, GameEngine engine) {
        boolean hit = false;
        int maxRows = engine.getCtx().getLevel().getRows();
        int maxCols = engine.getCtx().getLevel().getColumns();

        for (int r = pRow - 1; r <= pRow + 1; r++) {
            for (int c = pCol - 1; c <= pCol + 1; c++) {
                if (r >= 0 && r < maxRows && c >= 0 && c < maxCols) {
                    List<Zombie> targets = engine.findTargets(r, c, TargetingMode.NONE);
                    if (targets != null && !targets.isEmpty()) {
                        for (Zombie z : targets) {
                            z.takeDamage(damage);
                            //if (z.getHp() <= 0) ctx.recordPlantKill(self);
                            hit = true;
                        }
                    }
                }
            }
        }
        return hit;
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {String meleeKind = self.getAbilityParams().get("meleeKind");
        int pRow = self.getRow();
        int pCol = self.getCol();

        if ("INSTANT_EAT".equals(meleeKind)) {
            int eaten = 0;
            for (Zombie z : ctx.getAliveZombies()) {
                if (!z.isDead() && z.getRow() == pRow && z.getX() >= pCol) {
                    z.takeDamage(Integer.MAX_VALUE);
                    eaten++;
                    if (eaten >= 3) break;
                }
            }
        } else {
            for (Zombie z : ctx.getAliveZombies()) {
                if (!z.isDead() && Math.abs(z.getRow() - pRow) <= 1 && Math.abs(z.getX() - pCol) <= 1) {
                    z.takeDamage(1500);
                }
            }
        }
    }
}