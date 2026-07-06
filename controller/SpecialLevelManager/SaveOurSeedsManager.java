package controller.SpecialLevelManager;

import controller.repository.DataManager;
import controller.repository.factory.PlantFactory;
import model.GameContext;
import model.level.Level;
import model.plants.Plant;
import view.ConsoleView;

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
        ConsoleView.simplePrint("Save Our Seeds: Keep an eye on the endangered plants!\n");
    }
}
