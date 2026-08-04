package com.workshop.controller.SpecialLevelManager;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

public class LoveYourPlantsManager implements LevelManager{
    private int maxLostPlants;
    @Override
    public void onUpdate(double deltaTime, GameContext context) {
        if (context.getTotalLostPlants() > maxLostPlants){
            Console.showMessage("You lost too many plants! Max allowed was " + maxLostPlants + ".");
            context.triggerPlayerLoss();
        }
    }

    @Override
    public boolean canPlant(String plantName, GameContext context) {
        return true;
    }

    @Override
    public void onPlantSuccess(Plant plantedPlant, GameContext context) {

    }

    @Override
    public void onLevelStart(GameContext context) {
        this.maxLostPlants = context.getLevel().getMaxLostPlants();
        Console.simplePrint("Love Your Plants Level started! Don't let " + maxLostPlants
                + " or more plants die.\n");
    }
}
