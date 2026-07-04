package controller.SpecialLevelManager;

import model.GameContext;
import model.plants.Plant;

public interface LevelManager {
    void onUpdate(double deltaTime, GameContext context);

    boolean canPlant(String plantName, GameContext context);

    void onPlantSuccess(Plant plantedPlant, GameContext context);

    void onLevelStart(GameContext context);

    default boolean disableSkySun() {
        return false;
    }
}
