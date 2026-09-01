package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.SunType;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;

public class SunProducers implements BaseAbility {

    private boolean collected = false;

    @Override
    public void activate(Plant self, GameContext ctx) {
    }
    public void produceSun(String rate, int amount, SunType sunType, GameContext ctx, Plant plant) {


        if (sunType == com.workshop.model.mechanisms.SunType.BURST_CONSUME) {
            if (!collected) {
                collected = true;
                ctx.produceSun(plant.getCol(), plant.getRow(), amount, sunType);
                com.workshop.view.Console.showMessage("plant " + plant.getName() +
                        " produced a one-time burst of sun at (" + plant.getRow() + ", " + plant.getCol() + ")");
                plant.takeDamage(Integer.MAX_VALUE);
                ctx.getAlivePlants().remove(plant);
                ctx.getPlantGrid()[plant.getRow()][plant.getCol()] = null;
            }
            return;
        }

        if (!rate.equals("everyRound")){

            int rateOfPlant = Integer.parseInt(rate);
            int currentSecond = ctx.getTimeManager().getTotalSeconds();

            int x = plant.getCol();
            int y = plant.getRow();

            if (currentSecond - plant.getLastActionSecond() >= rateOfPlant ){
                if (!ctx.isSunPresent(x , y)){
                    int finalAmount = amount;
                    if (plant.getName().equalsIgnoreCase("SunShroom")) {
                        if (plant.isPlantFoodActive() || plant.getLastActionSecond() >= rateOfPlant * 2) {
                            finalAmount = 75;
                        } else if (plant.getLastActionSecond() >= rateOfPlant) {
                            finalAmount = 50;
                        } else {
                            finalAmount = 25;
                        }
                    }

                    ctx.produceSun(x , y, finalAmount,  sunType);
                    plant.setLastActionSecond(currentSecond);
                    com.workshop.view.Console.showMessage("plant " + plant.getName() +
                            " produced a sun at (" + x + ", " + y + ")");
                }
            }

        }

    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        if (mode != PlantFoodMode.INSTANT_CONSUME) return;

        int bonusSun = switch (self.getName()) {
            case "Sunflower" -> 150;
            case "Twin Sunflower" -> 250;
            case "SunShroom" -> 225;
            case "Primal Sunflower" -> 225;
            default -> 0;
        };

        if (bonusSun > 0) {
            if (self.getName().equalsIgnoreCase("SunShroom")) {
                self.setPlantFoodActive(true);
            }

            ctx.produceSun(self.getCol(), self.getRow(), bonusSun, SunType.NORMAL);
            com.workshop.view.Console.showMessage("Plant Food: " + self.getName() + " instantly produced " + bonusSun + " sun!");
        }
    }
}
