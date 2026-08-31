package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.TargetingMode;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;
import com.workshop.model.zombie.Zombie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallNut implements BaseAbility {

    private static final long ONE_SECOND_TICKS = 10;
    private static final long COOLDOWN_TICKS = 30;
    private static final int MAX_SUN_BATCH = 5;

    private static class SunState {
        int count = 0;
        long lastGenTick = -1;
        long cooldownStartTick = -1;
    }

    private final Map<Plant, SunState> sunStates = new HashMap<>();

    public void triggerAbility(WallNutType wallNutType, int damage, Plant self, GameEngine engine) {
        switch (wallNutType) {
            case BLOCKER:
            case TALL_BLOCKER:
            case STACKABLE_COVER:
                break;
            case REFLECTIVE:
                executeReflective(damage, self, engine);
                break;
            case LANE_REDIRECT:
                executeLaneRedirect(self, engine);
                break;
            case LANE_ATTRACT:
                executeLaneAttract(self, engine.getCtx());
                break;
            case SUN_GENERATING:
                if (hasZombieOrProjectileAt(self.getRow(), self.getCol(), engine.getCtx())) {
                    executeSunGenerating(self, engine.getCtx());
                }
                break;
        }
    }

    public boolean hasZombieOrProjectileAt(int row, int col, GameContext ctx) {
        boolean zombiePresent = ctx.getAliveZombies().stream()
            .anyMatch(z -> z != null && !z.isDead() && z.getRow() == row && (int) z.getX() == col);

        if (zombiePresent) {
            return true;
        }

        return ctx.getProjectiles().stream()
            .anyMatch(p -> p != null && p.isFromZombie() && p.getRow() == row && (int) p.getX() == col);
    }

    public void wall(WallNutType wallNutType, Plant plant, GameContext ctx) {
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        if (mode != PlantFoodMode.GRANT_ARMOR) return;

        String wallNutType = self.getAbilityParams().get("wallNutType");
        if (wallNutType == null) return;

        switch (wallNutType) {
            case "BLOCKER":              // Wall-nut
            case "REFLECTIVE":           // Endurian
                self.heal(4000);
                break;
            case "TALL_BLOCKER":         // Tall-nut
                self.heal(8000);
                break;
            case "STACKABLE_COVER":      // Pumpkin
            case "SUN_GENERATING":       // Sun Bean
                self.heal(6000);
                break;
            case "LANE_REDIRECT":        // Garlic
                plantFoodLaneRedirect(self, ctx);
                break;
            case "LANE_ATTRACT":         // Sweet Potato
                plantFoodLaneAttract(self, ctx);
                break;
            default:
                break;
        }
    }

    private void executeReflective(int damage, Plant self, GameEngine engine) {
        List<Zombie> attackers = engine.findTargets(self.getRow(), self.getCol(), TargetingMode.NONE);
        if (attackers != null && !attackers.isEmpty()) {
            for (Zombie z : attackers) {
                z.takeDamage(damage);
            }
        }
    }

    private void executeLaneRedirect(Plant self, GameEngine engine) {
        List<Zombie> biters = engine.findTargets(self.getRow(), self.getCol(), TargetingMode.NONE);
        if (biters != null && !biters.isEmpty()) {
            int maxRows = engine.getCtx().getLevel().getRows();
            for (Zombie z : biters) {
                z.setY(calculateNewRow(self.getRow(), maxRows));
            }
        }
    }

    private void executeLaneAttract(Plant self, GameContext ctx) {
        int pRow = self.getRow();
        int pCol = self.getCol();
        for (Zombie z : ctx.getAliveZombies()) {
            if (!z.isDead() && Math.abs(z.getRow() - pRow) == 1 && Math.abs(z.getX() - pCol) <= 1.0) {
                z.setY(pRow);
            }
        }
    }

    private void executeSunGenerating(Plant self, GameContext ctx) {
        long currentTick = ctx.getTimeManager().getTotalTicks();
        SunState state = sunStates.computeIfAbsent(self, k -> new SunState());
        if (state.cooldownStartTick > 0) {
            if (currentTick - state.cooldownStartTick >= COOLDOWN_TICKS) {
                state.cooldownStartTick = -1;
                state.count = 0;
            } else {
                return;
            }
        }

        if (state.count < MAX_SUN_BATCH) {
            if (state.lastGenTick < 0 || (currentTick - state.lastGenTick) >= ONE_SECOND_TICKS) {
                ctx.produceSun(self.getCol(), self.getRow(), 1);
                state.lastGenTick = currentTick;
                state.count++;

                if (state.count >= MAX_SUN_BATCH) {
                    state.cooldownStartTick = currentTick;
                }
            }
        }
    }

    private void plantFoodLaneRedirect(Plant self, GameContext ctx) {
        int pRow = self.getRow();
        int maxRows = ctx.getLevel().getRows();
        for (Zombie z : ctx.getAliveZombies()) {
            if (!z.isDead() && z.getRow() == pRow) {
                z.setY(calculateNewRow(pRow, maxRows));
            }
        }
    }

    private void plantFoodLaneAttract(Plant self, GameContext ctx) {
        int pRow = self.getRow();
        for (Zombie z : ctx.getAliveZombies()) {
            if (!z.isDead() && Math.abs(z.getRow() - pRow) <= 1) {
                z.setY(pRow);
            }
        }

        int missingHp = self.getBaseHp() - self.getHp();
        if (missingHp > 0) {
            self.heal(missingHp);
        }
    }

    private int calculateNewRow(int currentRow, int maxRows) {
        if (currentRow == 0) {
            return 1;
        } else if (currentRow == maxRows - 1) {
            return currentRow - 1;
        } else {
            return (Math.random() < 0.5) ? currentRow - 1 : currentRow + 1;
        }
    }
}
