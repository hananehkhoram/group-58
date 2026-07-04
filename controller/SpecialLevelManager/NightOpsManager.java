package controller.SpecialLevelManager;

import model.GameContext;
import model.plants.Plant;
import view.ConsoleView;

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
        ConsoleView.showMessage("Night Ops started! No sun will fall from the sky. Rely on your Sunflowers!\n");
    }

    @Override
    public boolean disableSkySun() {
        return true;
    }
}
