package com.workshop.controller.repository;

import com.workshop.model.zombie.Zombie;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ZombieRepository implements AssetRepository<Zombie>{
    private Map<String, Zombie> zombieDataMap = new HashMap<>();
    @Override
    public void load(String filePath) {
        try (var br = ResourceFileReader.openUtf8(filePath)) {
            String header = br.readLine(); // رد کردن عنوان ستون‌ها

            String line;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] cols = line.split(",", -1);

                String name = cols[0].trim();
                double eatDps = Double.parseDouble(cols[1].trim());
                int hp = Integer.parseInt(cols[2].trim());
                double speed = Double.parseDouble(cols[3].trim());
                int waveCost = Integer.parseInt(cols[4].trim());
                String armorStr = cols[5].trim();
                String id = cols[6].trim();

                Zombie zombie = new Zombie(
                    id,
                    name,
                    hp,
                    eatDps,
                    speed,
                    waveCost,
                    0
                );

                zombieDataMap.put(name, zombie);
                System.out.println(name + " -> " + id);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                "Error while reading file " + filePath,
                e
            );
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

    private static final Map<String, Set<String>> CHAPTER_EXCLUSIVE_ZOMBIES =
        Map.ofEntries(
            Map.entry("Ra", Set.of("Ancient Egypt")),
            Map.entry("Explorer", Set.of("Ancient Egypt")),
            Map.entry("Tomb raiser", Set.of("Ancient Egypt")),

            Map.entry("Dodo", Set.of("Frozen Caves")),
            Map.entry("Hunter", Set.of("Frozen Caves")),
            Map.entry("Troglobite", Set.of("Frozen Caves")),

            Map.entry("Fisherman", Set.of("Big Wave Beach")),
            Map.entry("Snorkel", Set.of("Big Wave Beach")),
            Map.entry("Octopus", Set.of("Big Wave Beach")),

            Map.entry("Juggler", Set.of("Dark Ages")),
            Map.entry("Wizard", Set.of("Dark Ages")),
            Map.entry("King", Set.of("Dark Ages")),
            Map.entry("Imp Dragon", Set.of("Dark Ages")),

            Map.entry(
                "Peashooter Zombie",
                Set.of("Zombotany")
            ),
            Map.entry(
                "Wall-nut Zombie",
                Set.of("Zombotany")
            ),
            Map.entry(
                "Jalapeno Zombie",
                Set.of("Zombotany")
            ),
            Map.entry(
                "Squash Zombie",
                Set.of("Zombotany")
            )
        );

    public boolean isAvailableInChapter(String zombieName, String chapterName) {
        Set<String> exclusiveTo = CHAPTER_EXCLUSIVE_ZOMBIES.get(zombieName);

        if (exclusiveTo == null) {
            return true;
        }

        if (chapterName == null) {
            return false;
        }

        return exclusiveTo.contains(chapterName);
    }
}
