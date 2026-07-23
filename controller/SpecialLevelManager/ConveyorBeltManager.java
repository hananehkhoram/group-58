package controller.SpecialLevelManager;

import controller.repository.DataManager;
import controller.repository.factory.PlantFactory;
import model.GameContext;
import model.plants.Plant;
import view.ConsoleView;

import java.util.ArrayList;
import java.util.List;

public class ConveyorBeltManager implements LevelManager{
    private final List<Plant> conveyorBelt = new ArrayList<>();
    private double conveyorTimer = 0.0;
    private static final double CONVEYOR_INTERVAL = 12.0;
    private static final int MAX_CAPACITY = 5;

    private DataManager dm;
    private PlantFactory plantFactory;

    public ConveyorBeltManager() {
        this.plantFactory = new PlantFactory(DataManager.getInstance());
    }

    @Override
    public void onLevelStart(GameContext context) {
        spawnPlantOnConveyor(context);
    }

    @Override
    public void onUpdate(double deltaTime, GameContext context) {
        conveyorTimer += deltaTime;
        if (conveyorTimer >= CONVEYOR_INTERVAL) {
            spawnPlantOnConveyor(context);
            conveyorTimer = 0.0;
        }
    }

    @Override
    public boolean canPlant(String plantName, GameContext context) {
        // چک می‌کند آیا این گیاه روی نوار نقاله هست یا نه
        return conveyorBelt.stream().anyMatch(p -> p.getName().equalsIgnoreCase(plantName));
    }

    @Override
    public void onPlantSuccess(Plant plantedPlant, GameContext context) {
        // حذف گیاه از روی نوار پس از کاشت موفق
        conveyorBelt.removeIf(p -> p.getName().equalsIgnoreCase(plantedPlant.getName()));
    }

    private void spawnPlantOnConveyor(GameContext context) {
        if (conveyorBelt.size() < MAX_CAPACITY) {
            List<Plant> pool = context.getLevel().getConveyorPlantPool();
            if (pool != null && !pool.isEmpty()) {
                java.util.Random random = new java.util.Random();
                Plant randomPlantTemplate = pool.get(random.nextInt(pool.size()));

                Plant newPlantCard = plantFactory.create(randomPlantTemplate.getName());

                conveyorBelt.add(newPlantCard);
                ConsoleView.showMessage("New plant added to conveyor belt: " + newPlantCard.getName());
            }
        }
    }

    public List<Plant> getConveyorBelt() {
        return conveyorBelt;
    }
}
