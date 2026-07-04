package controller.LevelManager;

import model.GameContext;
import model.plants.Plant;

import java.util.List;

public class LockedPlantsManager implements LevelManager{
    private List<Plant> bannedPlants;//در نوع اول از یک بعضی خانوادهها یک گیاه برداشته میشود و باقی آن خانواده قفل میشوند
    private List<Plant> forcedPlants;//در نوع دیگر چند گیاه برای بازیکن قفل هستند و بازیکن مجبور است با آن ها بازی را شروع کند

    public LockedPlantsManager(List<Plant> bannedPlants, List<Plant> forcedPlants) {
        if (bannedPlants != null) this.bannedPlants = bannedPlants;
        if (forcedPlants != null) this.forcedPlants = forcedPlants;
    }

    @Override
    public void onUpdate(double deltaTime, GameContext context) {

    }

    @Override
    public boolean canPlant(String plantName, GameContext context) {
        return false;
    }

    @Override
    public void onPlantSuccess(Plant plantedPlant, GameContext context) {

    }

    @Override
    public void onLevelStart(GameContext context) {

    }
}
