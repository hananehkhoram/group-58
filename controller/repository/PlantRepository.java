package controller.repository;

import model.plants.plantsKinds.Plant;

import java.util.HashMap;
import java.util.Map;

public class PlantRepository implements AssetRepository<Plant> {
    private final Map<String, Plant> plantDataMap = new HashMap<>();
    @Override
    public void load(String filePath) {
    }

    @Override
    public Plant get(String id) {
        return null;
    }
}
