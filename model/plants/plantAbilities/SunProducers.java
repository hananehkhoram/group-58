package model.plants.plantAbilities;

import model.GameContext;
import model.mechanisms.SunType;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

public class SunProducers implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {

    }
    public void produceSun(String rate, int amount, SunType sunType) {} //rate can be : a number or everyRound

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // INSTANT_CONSUME: one large immediate sun drop
    }
}
