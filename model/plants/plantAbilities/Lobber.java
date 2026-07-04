package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

public class Lobber implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {

    }
    public void lob(LobType lobType, String interval, Plant plant,  GameContext ctx) {

        int currentSecond = ctx.getTimeManager().getTotalSeconds();


        int intervalOfPlant = Integer.parseInt(interval);
        if (currentSecond - plant.getLastActionSecond() >= intervalOfPlant){
            boolean hasShot = false;

        }

    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // BARRAGE_LOB: empowered lobs thrown at multiple random zombies
    }
}
