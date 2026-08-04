package com.workshop.controller.SpecialLevelManager;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

public class DeadLineManager implements LevelManager{
    private int deadlineCol;

    @Override
    public void onUpdate(double deltaTime, GameContext context) {
        for (Zombie zombie : context.getAliveZombies()){
            if (zombie.getX() <= deadlineCol) {
                context.triggerPlayerLoss();
                Console.simplePrint("A zombie crossed the Dead Line at column " + deadlineCol + "!\n");
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
        Console.showMessage("Dead Line Level started! Defend column " + deadlineCol + " at all costs!\n");
    }
}
