package com.workshop.model;

import com.workshop.model.plants.Plant;
import com.workshop.model.plants.PlantFamily;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LevelStats {

    private int multiKillPatternCount;
    private int simultaneousKillPatternCount;
    private int quickKillPatternCount;
    private int killStreakPatternCount;
    private int precisionFinishPatternCount;
    private int currentKillStreak;

    private final Set<PlantFamily> plantFamiliesUsedToKillThisLevel = new HashSet<>();
    private final Set<PlantFamily> plantFamiliesPlantedThisLevel = new HashSet<>();
    private final Set<String> plantNamesThatKilledThisLevel = new HashSet<>();
    private int totalKillsThisLevel;

    private final Set<Integer> plantedColumns = new HashSet<>();
    private final Set<Integer> plantedRows = new HashSet<>();
    private int explosivePlantsPlacedThisLevel;
    private int sunProducerPlantsPlacedThisLevel;
    private int totalPlantsPlacedThisLevel;
    private long firstWaveStartTick = -1;
    private final List<Long> earlyKillTicks = new ArrayList<>();
    private int almostLostKillsThisLevel;
    private int lawnMowerKillsThisLevel;

    private int totalSunProducedInLevel;
    private int totalLostPlants;
    private int totalZombiesKilledInLevel;

    public void incrementZombieKills() {
        totalZombiesKilledInLevel++;
    }

    public void incrementPlantsLost() {
        totalLostPlants++;
        resetKillStreak();
    }

    public void addSunProduced(int amount) {
        if (amount > 0) {
            totalSunProducedInLevel += amount;
        }
    }

    public int getTotalZombiesKilledInLevel() {
        return totalZombiesKilledInLevel;
    }

    public int getTotalSunProducedInLevel() {
        return totalSunProducedInLevel;
    }

    public int getTotalLostPlants() {
        return totalLostPlants;
    }

    public void recordPlantKill(Plant killer) {
        if (killer == null) return;
        totalKillsThisLevel++;
        plantNamesThatKilledThisLevel.add(killer.getName());
        if (killer.getFamily() != null) {
            plantFamiliesUsedToKillThisLevel.add(killer.getFamily());
        }
    }

    public void recordPlantPlaced(Plant plant, int row, int col) {
        totalPlantsPlacedThisLevel++;
        plantedRows.add(row);
        plantedColumns.add(col);

        if (plant.getFamily() == PlantFamily.EXPLOSIVE) {
            explosivePlantsPlacedThisLevel++;
        }
        if (plant.getFamily() == PlantFamily.SUN_PRODUCER) {
            sunProducerPlantsPlacedThisLevel++;
        }
        if (plant.getFamily() != null) {
            plantFamiliesPlantedThisLevel.add(plant.getFamily());
        }
        plant.setRow(row);
        plant.setCol(col);
    }

    public Set<String> getPlantNamesThatKilledThisLevel() {
        return plantNamesThatKilledThisLevel;
    }

    public Set<PlantFamily> getPlantFamiliesUsedToKillThisLevel() {
        return plantFamiliesUsedToKillThisLevel;
    }

    public Set<PlantFamily> getPlantFamiliesPlantedThisLevel() {
        return plantFamiliesPlantedThisLevel;
    }

    public int getTotalKillsThisLevel() {
        return totalKillsThisLevel;
    }

    public Set<Integer> getPlantedColumns() {
        return plantedColumns;
    }

    public Set<Integer> getPlantedRows() {
        return plantedRows;
    }

    public int getExplosivePlantsPlacedThisLevel() {
        return explosivePlantsPlacedThisLevel;
    }

    public int getSunProducerPlantsPlacedThisLevel() {
        return sunProducerPlantsPlacedThisLevel;
    }

    public int getTotalPlantsPlacedThisLevel() {
        return totalPlantsPlacedThisLevel;
    }

    public void recordFirstWaveStart(TimeManager timeManager) {
        if (firstWaveStartTick == -1) {
            firstWaveStartTick = timeManager.getTotalTicks();
        }
    }

    public void recordZombieKillTick(TimeManager timeManager) {
        earlyKillTicks.add(timeManager.getTotalTicks());
    }

    public long getFirstWaveStartTick() {
        return firstWaveStartTick;
    }

    public List<Long> getEarlyKillTicks() {
        return earlyKillTicks;
    }

    public void recordAlmostLostKill() {
        almostLostKillsThisLevel++;
    }

    public int getAlmostLostKillsThisLevel() {
        return almostLostKillsThisLevel;
    }

    public void recordLawnMowerKill() {
        lawnMowerKillsThisLevel++;
    }

    public int getLawnMowerKillsThisLevel() {
        return lawnMowerKillsThisLevel;
    }

    public void incrementMultiKillPattern() {
        multiKillPatternCount++;
    }

    public void incrementSimultaneousKillPattern() {
        simultaneousKillPatternCount++;
    }

    public void incrementQuickKillPattern() {
        quickKillPatternCount++;
    }

    public void incrementPrecisionFinishPattern() {
        precisionFinishPatternCount++;
    }

    public void bumpKillStreak() {
        currentKillStreak++;
        if (currentKillStreak % 5 == 0) {
            killStreakPatternCount++;
        }
    }

    public void resetKillStreak() {
        currentKillStreak = 0;
    }

    public int getMultiKillPatternCount() {
        return multiKillPatternCount;
    }

    public int getSimultaneousKillPatternCount() {
        return simultaneousKillPatternCount;
    }

    public int getQuickKillPatternCount() {
        return quickKillPatternCount;
    }

    public int getKillStreakPatternCount() {
        return killStreakPatternCount;
    }

    public int getPrecisionFinishPatternCount() {
        return precisionFinishPatternCount;
    }
}
