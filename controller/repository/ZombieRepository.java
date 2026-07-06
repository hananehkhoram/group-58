package controller.repository;

import model.zombie.Zombie;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ZombieRepository implements AssetRepository<Zombie>{
    private final Map<String, Zombie> zombieDataMap = new HashMap<>();
    @Override
    public void load(String filePath) {
        try (var br = new java.io.BufferedReader(new FileReader(filePath))) {
            String header = br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split(",", -1);
                String name     = cols[0].trim();
                double eatDps   = Double.parseDouble(cols[1].trim());
                int hp          = Integer.parseInt(cols[2].trim());
                double speed    = Double.parseDouble(cols[3].trim());
                int waveCost    = Integer.parseInt(cols[4].trim());
                String armorStr = cols[5].trim();
                String id       = cols[6].trim();

                Zombie z = new Zombie(id, name, hp, eatDps, speed, waveCost, 0);
                zombieDataMap.put(name, z);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while reading file " + filePath, e);
        }
    }

    public Map<String , Object> extractParams() {
        Map<String , Object> map = new HashMap<>();
        // load from json
        return null;
    }

    @Override
    public Zombie get(String id) {
        return  zombieDataMap.get(id);
    }

    public Map<String, Zombie> getZombieDataMap() {
        return zombieDataMap;
    }
}
