package model.plants.plantFoodEffect;

import model.GameContext;
import model.plants.Plant;

public interface PlantFoodEffect {
    void activate (Plant self, GameContext ctx);
}
