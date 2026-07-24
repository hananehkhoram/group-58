package controller.SpecialLevelManager;

import model.GameContext;
import model.plants.Plant;
import view.ConsoleView;

import java.util.List;

public class LockedPlantsManager implements LevelManager{
    private List<Plant> bannedPlants;
    private List<Plant> forcedPlants;

    public LockedPlantsManager(List<Plant> bannedPlants, List<Plant> forcedPlants) {
        if (bannedPlants != null) this.bannedPlants = bannedPlants;
        if (forcedPlants != null) this.forcedPlants = forcedPlants;
    }

    @Override
    public void onUpdate(double deltaTime, GameContext context) {

    }

    @Override
    public boolean canPlant(String plantName, GameContext context) {
        if (bannedPlants == null) return true;
        for (Plant bannedPlant : bannedPlants) {
            if (bannedPlant.getName().equalsIgnoreCase(plantName)) {
                // اگر بازیکن خواست این گیاه را بکارد، سیستم بهش اجازه نمی‌دهد
                return false;
            }
        }
        return true;
    }

    @Override
    public void onPlantSuccess(Plant plantedPlant, GameContext context) {

    }

    @Override
    public void onLevelStart(GameContext context) {
        if (!forcedPlants.isEmpty()) {
            for (Plant forcedPlant : forcedPlants) {
                context.getActivePlants().add(forcedPlant);
            }
            ConsoleView.showMessage("Locked Plants: Forced plants have been locked into your slots!");
        }
    }

    public List<Plant> getBannedPlants() {
        return bannedPlants;
    }

    public List<Plant> getForcedPlants() {
        return forcedPlants;
    }
}
