package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;

public interface BaseAbility {
    default void activate(Plant self, GameContext ctx){};

    /** Triggers this plant's Plant Food behavior. Default no-op for abilities without one. */
    default void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
    }
}
