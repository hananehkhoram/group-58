package com.workshop.controller.SpecialLevelManager;

import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.PlantFamily;
import com.workshop.model.plants.Tag;
import com.workshop.view.Console;

import javax.xml.crypto.Data;

public class PlantWhatYouGetManager implements LevelManager{
    @Override
    public void onUpdate(double deltaTime, GameContext context) {

    }

    @Override
    public boolean canPlant(String plantName, GameContext context) {
        if (isSunflower(plantName)) {
            Console.showMessage("You cannot plant Sun Producer in this level!\n");
            return false;
        }
        return true;
    }

    @Override
    public void onPlantSuccess(Plant plantedPlant, GameContext context) {

    }

    @Override
    public void onLevelStart(GameContext context) {
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
        Plant plant = DataManager.getInstance().plants.get(plantName);
        return plant.getFamily().equals(PlantFamily.SUN_PRODUCER) || plant.getTags().contains(Tag.SUN);
    }
}
