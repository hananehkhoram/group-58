package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.TargetingMode;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;
import com.workshop.model.zombie.Zombie;

import java.util.List;

public class MeleeAttackers implements BaseAbility {

    public void melee(String meleeKind, int damage, Plant plant, GameEngine engine) {
        int pRow = plant.getRow();
        int pCol = plant.getCol();
        int currentSecond = engine.getCtx().getTimeManager().getTotalSeconds();

        switch (meleeKind) {
            case "FRONT_AND_BACK":
                Zombie targetFB = findTargetFrontOrBack(pRow, pCol, engine);
                if (targetFB != null) {
                    boolean aliveBeforeFB = !targetFB.isDead();
                    targetFB.takeDamage(damage);
                    if ("Wasabi Whip".equalsIgnoreCase(plant.getName())) {
                        targetFB.meltIce();
                    }
                    if (aliveBeforeFB && targetFB.isDead()) {
                        engine.getCtx().recordPlantKill(plant);
                    }
                    plant.setLastActionSecond(currentSecond);
                }
                break;

            case "AOE":
            case "AOE_RAMP_UP":
                boolean hitAnyone = applyAoEDamage(pRow, pCol, damage, plant, engine);
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
            boolean aliveBefore = !target.isDead();
            target.takeDamage(Integer.MAX_VALUE);
            if (aliveBefore && target.isDead()) {
                engine.getCtx().recordPlantKill(plant);
            }
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

    private boolean applyAoEDamage(int pRow, int pCol, int damage, Plant plant, GameEngine engine) {
        boolean hit = false;
        int maxRows = engine.getCtx().getLevel().getRows();
        int maxCols = engine.getCtx().getLevel().getColumns();

        for (int r = pRow - 1; r <= pRow + 1; r++) {
            for (int c = pCol - 1; c <= pCol + 1; c++) {
                if (r >= 0 && r < maxRows && c >= 0 && c < maxCols) {
                    List<Zombie> targets = engine.findTargets(r, c, TargetingMode.NONE);
                    if (targets != null && !targets.isEmpty()) {
                        for (Zombie z : targets) {
                            boolean aliveBefore = !z.isDead();
                            z.takeDamage(damage);
                            if (aliveBefore && z.isDead()) {
                                engine.getCtx().recordPlantKill(plant);
                            }
                            hit = true;
                        }
                    }
                }
            }
        }
        return hit;
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {String meleeKind =
            self.getAbilityParams().get("meleeKind");
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
