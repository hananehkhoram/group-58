package controller.SpecialLevelManager;

import model.GameContext;
import model.plants.Plant;
import view.ConsoleView;

public class PlantWhatYouGetManager implements LevelManager{
    @Override
    public void onUpdate(double deltaTime, GameContext context) {

    }

    @Override
    public boolean canPlant(String plantName, GameContext context) {
        if (plantName.equalsIgnoreCase("Sunflower")) {
            ConsoleView.showMessage("You cannot plant Sunflowers in this level!\n");
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
        context.setSunAmount(800);
        ConsoleView.showMessage("Plant What You Get started!\n");
        ConsoleView.showMessage("You have " + 800 + " sun. Plant your defenses wisely.\n");
    }

    @Override
    public boolean disableSkySun() {
        return true;
    }
}
