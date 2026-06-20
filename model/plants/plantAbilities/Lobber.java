package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

public class Lobber implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {

    }
    public void lob(LobType lobType) {}

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // BARRAGE_LOB: empowered lobs thrown at multiple random zombies
    }
}
