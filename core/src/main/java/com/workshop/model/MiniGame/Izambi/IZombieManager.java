package com.workshop.model.MiniGame.Izambi;

import com.workshop.controller.SpecialLevelManager.LevelManager;
import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class IZombieManager implements LevelManager {
    public static final int RED_LINE_COLUMN = 6;

    private static final int PRODUCED_SUN_AMOUNT = 25;

    private static final int FIRST_PRODUCTION_DELAY_TICKS = 250;

    private static final int MIN_PRODUCTION_INTERVAL_TICKS = 50;


    private static final List<Map<String, Integer>> LEVEL_ZOMBIES = List.of(
        zombiePool(
            "Default", 50,
            "cone head", 75,
            "bucket head", 125,
            "Imp", 50,
            "Ra", 75
        ),
        zombiePool(
            "Default", 50,
            "cone head", 75,
            "Explorer", 100,
            "Tomb raiser", 125,
            "Dodo", 150
        ),
        zombiePool(
            "bucket head", 125,
            "Imp", 50,
            "Explorer", 100,
            "Hunter", 150,
            "Juggler", 125
        )
    );

    private final int levelIndex;
    private final boolean[] eatenBrains;


    private final Set<Zombie> sunProducerZombies =
        Collections.newSetFromMap(new IdentityHashMap<>());

    private final Map<Zombie, Long> nextProductionTicks =
        new IdentityHashMap<>();

    private long startTick;

    public IZombieManager(int levelIndex, int rows) {
        if (levelIndex < 0 || levelIndex >= LEVEL_ZOMBIES.size()) {
            throw new IllegalArgumentException(
                "Invalid I-Zombie level index: " + levelIndex
            );
        }

        this.levelIndex = levelIndex;
        this.eatenBrains = new boolean[rows];
    }

    private static Map<String, Integer> zombiePool(Object... values) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            String zombieName = (String) values[i];
            int cost = (Integer) values[i + 1];
            result.put(zombieName, cost);
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public void onUpdate(double deltaTime, GameContext context) {
        long currentTick = context.getTimeManager().getTotalTicks();

        sunProducerZombies.removeIf(Zombie::isDead);
        nextProductionTicks.keySet().removeIf(Zombie::isDead);

        for (Zombie producer : sunProducerZombies) {
            long nextTick = nextProductionTicks.getOrDefault(
                producer,
                startTick + FIRST_PRODUCTION_DELAY_TICKS
            );

            if (currentTick < nextTick) {
                continue;
            }

            context.addSun(PRODUCED_SUN_AMOUNT);

            Console.showMessage(
                "Sun Producer Zombie in row "
                    + producer.getRow()
                    + " produced "
                    + PRODUCED_SUN_AMOUNT
                    + " sun."
            );

            long nextInterval = productionIntervalTicks(currentTick);
            nextProductionTicks.put(producer, currentTick + nextInterval);
        }
    }


    private long productionIntervalTicks(long currentTick) {
        long elapsedTicks = Math.max(0, currentTick - startTick);

        long speedUp = elapsedTicks / 20;

        return Math.max(
            MIN_PRODUCTION_INTERVAL_TICKS,
            FIRST_PRODUCTION_DELAY_TICKS - speedUp
        );
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
        startTick = context.getTimeManager().getTotalTicks();
    }


    @Override
    public boolean disableSkySun() {
        return true;
    }

    public void registerSunProducer(
        Zombie zombie,
        GameContext context
    ) {
        sunProducerZombies.add(zombie);

        long firstProductionTick =
            context.getTimeManager().getTotalTicks()
                + FIRST_PRODUCTION_DELAY_TICKS;

        nextProductionTicks.put(zombie, firstProductionTick);
    }

    public boolean isSunProducer(Zombie zombie) {
        return sunProducerZombies.contains(zombie);
    }


    public boolean isValidPlacement(
        int row,
        int column,
        GameContext context
    ) {
        return row >= 0
            && row < context.getLevel().getRows()
            && column >= RED_LINE_COLUMN
            && column < context.getLevel().getColumns();
    }


    public String findCanonicalZombieName(String requestedName) {
        if (requestedName == null) {
            return null;
        }

        String normalized =
            requestedName.trim().toLowerCase(Locale.ROOT);

        for (String availableName
            : getAvailableZombieCosts().keySet()) {

            if (availableName
                .toLowerCase(Locale.ROOT)
                .equals(normalized)) {

                return availableName;
            }
        }

        return null;
    }

    public int getZombieCost(String canonicalName) {
        Integer cost =
            getAvailableZombieCosts().get(canonicalName);

        return cost == null ? -1 : cost;
    }

    public Map<String, Integer> getAvailableZombieCosts() {
        return LEVEL_ZOMBIES.get(levelIndex);
    }

    public int getMinimumZombieCost() {
        return getAvailableZombieCosts()
            .values()
            .stream()
            .mapToInt(Integer::intValue)
            .min()
            .orElse(Integer.MAX_VALUE);
    }

    public void eatBrain(int row) {
        if (row < 0 || row >= eatenBrains.length) {
            return;
        }

        if (!eatenBrains[row]) {
            eatenBrains[row] = true;

            Console.showMessage(
                "The brain in row " + row + " was eaten."
            );
        }
    }

    public boolean isBrainEaten(int row) {
        return row >= 0
            && row < eatenBrains.length
            && eatenBrains[row];
    }

    public int getEatenBrainCount() {
        int count = 0;

        for (boolean eaten : eatenBrains) {
            if (eaten) {
                count++;
            }
        }

        return count;
    }

    public boolean areAllBrainsEaten() {
        return getEatenBrainCount() == eatenBrains.length;
    }


    public boolean shouldPlayerLose(GameContext context) {
        boolean hasLivingZombie =
            context.getAliveZombies()
                .stream()
                .anyMatch(zombie -> !zombie.isDead());

        return !hasLivingZombie
            && context.getSunAmount() < getMinimumZombieCost();
    }
}
