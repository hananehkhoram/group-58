package com.workshop.controller.SpecialLevelManager;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

public class NightOpsManager implements LevelManager{
    @Override
    public void onUpdate(double deltaTime, GameContext context) {

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
        Console.showMessage("Night Ops started! No sun will fall from the sky. Rely on your Sunflowers!\n");
    }

    @Override
    public boolean disableSkySun() {
        return true;
    }
}
