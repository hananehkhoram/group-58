package com.workshop.controller.SpecialLevelManager;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

public class PlantWhatYouGetManager implements LevelManager{
    @Override
    public void onUpdate(double deltaTime, GameContext context) {

    }

    @Override
    public boolean canPlant(String plantName, GameContext context) {
        if (isSunflower(plantName)) {
            Console.showMessage("You cannot plant Sunflowers in this level!\n");
            return false;
        }
        return true;
    }

    @Override
    public void onPlantSuccess(Plant plantedPlant, GameContext context) {

    }

    @Override
    public void onLevelStart(GameContext context) {
        context.setSetupPhase(true);
        context.setSunAmount(context.getLevel().getSunsGiven());
        Console.showMessage("Plant What You Get started!\n");
        Console.showMessage("You have " + context.getLevel().getSunsGiven() +
                " sun. Plant your defenses wisely.\n");
    }

    @Override
    public boolean disableSkySun() {
        return true;
    }

    public static boolean isSunflower(String plantName) {
        if (plantName == null) {
            return false;
        }
        String compact = plantName.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return compact.contains("sunflower");
    }
}
