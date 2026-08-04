package com.workshop.controller.SpecialLevelManager;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;

public interface LevelManager {
    void onUpdate(double deltaTime, GameContext context);

    boolean canPlant(String plantName, GameContext context);

    void onPlantSuccess(Plant plantedPlant, GameContext context);

    void onLevelStart(GameContext context);

    default boolean disableSkySun() {
        return false;
    }
}
