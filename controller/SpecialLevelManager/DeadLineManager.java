package controller.SpecialLevelManager;

import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;
import view.ConsoleView;

public class DeadLineManager implements LevelManager{
    private int deadlineCol;

    @Override
    public void onUpdate(double deltaTime, GameContext context) {
        for (Zombie zombie : context.getAliveZombies()){
            if (zombie.getX() <= deadlineCol) {
                context.triggerPlayerLoss();
                ConsoleView.simplePrint("A zombie crossed the Dead Line at column " + deadlineCol + "!\n");
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
        this.deadlineCol = context.getLevel().getDeadlineColumn();
        ConsoleView.showMessage("Dead Line Level started! Defend column " + deadlineCol + " at all costs!\n");
    }
}
