package controller.repository;

import model.zombie.Zombie;

import java.util.HashMap;
import java.util.Map;

public class ZombieRepository implements AssetRepository<Zombie>{
    private final Map<String, Zombie> zombieDataMap = new HashMap<>();
    @Override
    public void load(String filePath) {
    }

    @Override
    public Zombie get(String id) {
        return null;
    }
}
