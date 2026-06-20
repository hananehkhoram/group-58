package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

public interface BaseAbility {
    void activate(Plant self, GameContext ctx);

    /** Triggers this plant's Plant Food behavior. Default no-op for abilities without one. */
    default void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
    }
}
