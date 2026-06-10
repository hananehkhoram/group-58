package model.plants.plantFoodEffect;

import model.GameContext;
import model.plants.plantsKinds.Plant;

public interface PlantFoodEffect {
    void activate (Plant self, GameContext ctx);
}
