package controller.SpecialLevelManager;

import model.GameContext;
import model.plants.Plant;
import view.ConsoleView;

public class LoveYourPlantsManager implements LevelManager{
    private int maxLostPlants;
    @Override
    public void onUpdate(double deltaTime, GameContext context) {
        if (context.getTotalLostPlants() > maxLostPlants){
            ConsoleView.showMessage("You lost too many plants! Max allowed was " + maxLostPlants + ".");
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
        ConsoleView.simplePrint("Love Your Plants Level started! Don't let " + maxLostPlants
                + " or more plants die.\n");
    }
}
