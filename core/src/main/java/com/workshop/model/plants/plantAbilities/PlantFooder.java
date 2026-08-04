package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;

public class PlantFooder implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {
        if (self.getFamily() == null) return;
        int affectedCount = 0;
        for (Plant other : ctx.getAlivePlants()) {
            if (other != self && other.getFamily() == self.getFamily()) {
                other.activatePlantFood(ctx);
                affectedCount++;
            }
        }

        com.workshop.view.Console.showMessage("Plant Food: " + self.getName() +
                " empowered " + affectedCount + " plants in " + self.getFamily() + " family!");
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
    }
}
