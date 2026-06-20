package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

public class MeleeAttackers implements BaseAbility {

    @Override
    public void activate(Plant self, GameContext ctx) {

    }
    public void melee(int row, int col) {
        //heater here
    }
    public void instantEat(int delay) {}

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // HEAVY_STRIKE: one powerful area hit, or Chomper eating 3 zombies from range
    }

}
