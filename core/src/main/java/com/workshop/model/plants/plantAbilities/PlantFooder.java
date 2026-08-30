package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;

public class PlantFooder implements BaseAbility {

    private static final int DISPLAY_SECONDS = 3;

    @Override
    public void activate(Plant self, GameContext ctx) {
        if (self.getFamily() == null) {
            return;
        }

        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        if (self.getLastActionSecond() > 0) {
            if (currentSecond - self.getLastActionSecond() >= DISPLAY_SECONDS) {
                removeSelf(self, ctx);
            }
            return;
        }

        self.setLastActionSecond(Math.max(1, currentSecond));
        ctx.queuePlantAttackAnimation(self);

        int affectedCount = 0;
        Plant[][] grid = ctx.getPlantGrid();
        for (Plant[] row : grid) {
            for (Plant other : row) {
                if (other == null || other == self || other.getFamily() != self.getFamily()) {
                    continue;
                }
                other.activatePlantFood(ctx);
                affectedCount++;
            }
        }

        com.workshop.view.Console.showMessage(
            "Plant Food: " + self.getName()
                + " empowered " + affectedCount
                + " plants in " + self.getFamily() + " family!"
        );
    }

    private static void removeSelf(Plant self, GameContext ctx) {
        GameEngine engine = ctx.getGameEngine();
        if (engine != null) {
            engine.removePlant(self.getRow(), self.getCol());
            return;
        }
        ctx.getPlantGrid()[self.getRow()][self.getCol()] = null;
        ctx.getAlivePlants().remove(self);
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
    }
}
