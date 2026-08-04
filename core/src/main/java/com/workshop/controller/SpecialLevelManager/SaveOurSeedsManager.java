package com.workshop.controller.SpecialLevelManager;

import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

import java.util.ArrayList;
import java.util.List;

public class SaveOurSeedsManager implements LevelManager{
    private final List<Plant> endangeredPlants = new ArrayList<>();
    private DataManager dm;
    private PlantFactory plantFactory;

    public SaveOurSeedsManager() {
        this.dm = DataManager.getInstance();
        this.plantFactory = new PlantFactory(dm);
    }

    @Override
    public void onUpdate(double deltaTime, GameContext context) {
        for (Plant plant : endangeredPlants) {
            if (plant.getHp() <= 0 || !context.getAlivePlants().contains(plant)) {
                context.triggerPlayerLoss();
                break;
            }
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
        List<Level.PrePlacedPlant> blueprints = context.getLevel().getSaveOurSeedsPlants();

        for (Level.PrePlacedPlant blueprint : blueprints){
            int r = blueprint.getRow();
            int c = blueprint.getCol();
            Plant plant = plantFactory.create(blueprint.getPlantTemplate().getName());

            context.getPlantGrid()[r][c] = plant;
            context.getAlivePlants().add(plant);

            endangeredPlants.add(plant);
        }
        Console.simplePrint("Save Our Seeds: Keep an eye on the endangered plants!\n");
    }
}
