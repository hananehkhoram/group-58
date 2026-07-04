package model.plants.plantAbilities;

import model.GameContext;
import model.mechanisms.SunType;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

public class SunProducers implements BaseAbility {

    private boolean collected = false;

    @Override
    public void activate(Plant self, GameContext ctx) {
    }
    public void produceSun(String rate, int amount, SunType sunType, GameContext ctx, Plant plant) {


        if (!rate.equals("everyRound")){

            int rateOfPlant = Integer.parseInt(rate);
            int currentSecond = ctx.getTimeManager().getTotalSeconds();

            int x = plant.getRow();
            int y = plant.getCol();

            if (currentSecond - plant.getLastActionSecond() >= rateOfPlant ){
                if (!ctx.isSunPresent(x , y)){
                    ctx.produceSun(x , y, amount);
                    plant.setLastActionSecond(currentSecond);
                    view.ConsoleView.showMessage("plant " + plant.getName() + "produced a sun at (" + x + ", " + y + ")");
                }
            }

        }

    } //rate can be : a number or everyRound

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // INSTANT_CONSUME: one large immediate sun drop
    }
}
