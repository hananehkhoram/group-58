package com.workshop.controller.SpecialLevelManager;

import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

public class TimedWarManager implements LevelManager{
    private double timeRemaining;
    private boolean isSunMode;
    private int targetAmount;

    @Override
    public void onUpdate(double deltaTime, GameContext context) {
        if (timeRemaining > 0) {
            timeRemaining -= deltaTime;
        }

        int currentProgress = isSunMode ? context.getTotalSunProducedInLevel() :
                context.getTotalZombiesKilledInLevel();

        if (currentProgress >= targetAmount) {
            context.triggerPlayerWin();
            return;
        }

        if (timeRemaining <= 0) {
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
        Level level = context.getLevel();
        this.timeRemaining = level.getTimedWarDuration();
        this.isSunMode = level.isSunProductionMode();

        this.targetAmount = isSunMode ? level.getTimedWarTargetSun() : level.getTimedWarTargetZombies();

        Console.showMessage("Timed War started! Time: " + timeRemaining + "s  Target: " + targetAmount);
    }
    public double getTimeRemaining() {
        return Math.max(0, timeRemaining);
    }
}
