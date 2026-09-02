package com.workshop.model;

import com.workshop.model.mechanisms.LootItem;
import com.workshop.model.mechanisms.PlantFoodDrop;
import com.workshop.model.mechanisms.SunType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * On-board sun drops, loot, plant-food drops, and plant seed cooldowns.
 */
final class BoardPickups {

    private final Map<String, Integer> producedSuns = new HashMap<>();
    private final Map<String, SunType> producedSunTypes = new HashMap<>();
    private final List<LootItem> activeLoots = new ArrayList<>();
    private final List<PlantFoodDrop> activePlantFoodDrops = new ArrayList<>();
    private final Map<String, Long> plantCooldowns = new HashMap<>();

    void produceSun(int x, int y, int amount, SunType type) {
        String key = x + ", " + y;
        producedSuns.put(key, producedSuns.getOrDefault(key, 0) + amount);
        producedSunTypes.put(key, type);
    }

    boolean isSunPresent(int x, int y) {
        return producedSuns.containsKey(x + ", " + y);
    }

    int collectSunAt(int x, int y, IntConsumer addSun) {
        String key = x + ", " + y;
        if (!producedSuns.containsKey(key)) {
            return 0;
        }
        int amount = producedSuns.remove(key);
        producedSunTypes.remove(key);
        addSun.accept(amount);
        return amount;
    }

    Map<String, Integer> getProducedSuns() {
        return producedSuns;
    }

    SunType getProducedSunTypeAt(int x, int y) {
        return producedSunTypes.getOrDefault(x + ", " + y, SunType.NORMAL);
    }

    boolean isOnCooldown(String plantName, TimeManager timeManager) {
        long availableAt = plantCooldowns.getOrDefault(plantName, 0L);
        return timeManager.getTotalTicks() < availableAt;
    }

    void setCooldown(String plantName, double rechargeSeconds, TimeManager timeManager) {
        long ticksFromNow = (long) (rechargeSeconds * 10);
        plantCooldowns.put(plantName, timeManager.getTotalTicks() + ticksFromNow);
    }

    void clearAllCooldowns() {
        plantCooldowns.clear();
    }

    double getRemainingCooldownSeconds(String plantName, TimeManager timeManager) {
        long availableAt = plantCooldowns.getOrDefault(plantName, 0L);
        long remainingTicks = availableAt - timeManager.getTotalTicks();
        if (remainingTicks <= 0) {
            return 0;
        }
        return remainingTicks / 10.0;
    }

    List<LootItem> getActiveLoots() {
        return activeLoots;
    }

    void addLoot(LootItem loot) {
        if (loot != null) {
            activeLoots.add(loot);
        }
    }

    void clearLoots() {
        activeLoots.clear();
    }

    List<PlantFoodDrop> getActivePlantFoodDrops() {
        return activePlantFoodDrops;
    }

    void addPlantFoodDrop(PlantFoodDrop drop) {
        if (drop != null) {
            activePlantFoodDrops.add(drop);
        }
    }

    boolean collectPlantFoodDrop(PlantFoodDrop drop) {
        if (drop == null || !activePlantFoodDrops.remove(drop)) {
            return false;
        }
        com.workshop.model.user.UserManager.getInstance().addPlantFood(1);
        return true;
    }
}
