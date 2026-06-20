package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

public class WallNut implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {

    }
    public void wall(WallNutType wallNutType) {

    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // GRANT_ARMOR: temporary or permanent bonus HP/armor
    }
}
