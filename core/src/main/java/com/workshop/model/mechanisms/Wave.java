package com.workshop.model.mechanisms;

import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.LevelType;
import com.workshop.model.user.UserManager;
import com.workshop.model.zombie.BossZombieRegistry;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

import java.util.*;
import java.util.stream.Collectors;

public class Wave {

    private static final double WAVE_DIFFICULTY_GROWTH = 1.25;
    private static final double FINAL_WAVE_MULTIPLIER = 2.0;
    private static final double THRESHOLD_HP_RATIO = 0.25;
    private static final double BASE_DIFFICULTY = 3.0;

    private final int waveNumber;
    private final int waveCost;
    private final int waveDelay;
    private final boolean isLastWave;

    private final List<Zombie> spawnedZombies = new ArrayList<>();
    private int initialTotalHp = 0;
    private boolean started = false;

    public Wave(int waveCost, int waveDelay, boolean isLastWave, int waveNumber) {
        this.waveCost = waveCost;
        this.waveDelay = waveDelay;
        this.isLastWave = isLastWave;
        this.waveNumber = waveNumber;
    }

    public void start(GameContext ctx) {
        if (started) {
            return;
        }
        started = true;

        announceStart(ctx);

        if (ctx.getSeason() != null) {
            ctx.getSeason().onWaveStart(ctx, waveNumber, isLastWave);
        }

        spawnZombies(ctx, calculateEffectiveBudget());

        if (isLastWave && ctx.getLevel().getLevelType() == LevelType.BOSS_FIGHT) {
            spawnBossZombie(ctx);
        }

        initialTotalHp = spawnedZombies.stream()
            .mapToInt(Zombie::getHp)
            .sum();
    }

    private void spawnBossZombie(GameContext ctx) {
        if (ctx.getSeason() == null) return;

        String bossName = BossZombieRegistry.bossNameForSeason(ctx.getSeason().getName());
        if (bossName == null) return;

        Zombie bossTemplate = ctx.getDataManager().zombies.getZombieDataMap().get(bossName);
        if (bossTemplate == null) return; // Zomboss's row not added to zombies.csv yet

        ZombieFactory factory = new ZombieFactory(ctx.getDataManager());
        Zombie boss = factory.create(bossName);

        int rows = ctx.getLevel().getRows();
        int topRow = Math.max(0, Math.min(rows / 2, Math.max(0, rows - 2)));
        boss.setX(Math.max(0, ctx.getLevel().getColumns() - 1.15));
        boss.setY(topRow);

        ctx.addZombie(boss);
        spawnedZombies.add(boss);

        ctx.announce(boss.getName() + " has entered the battle!");
        Console.showMessage(boss.getName() + " (Zomboss) has entered the battle!\n");
    }

    private void announceStart(GameContext ctx) {
        String message = isLastWave
            ? "The final wave has come."
            : "Wave " + waveNumber + " started.";
        Console.showMessage(message + "\n");
        ctx.announce(message);



        if (isLastWave) {
            ctx.playSound("sfx:music/finalwave");
        }
    }

    private int calculateEffectiveBudget() {

        double budget = waveCost;

        for (int i = 2; i <= waveNumber; i++) {

            boolean isFinalStep = (i == waveNumber && isLastWave);

            budget *= isFinalStep ? FINAL_WAVE_MULTIPLIER : WAVE_DIFFICULTY_GROWTH;

        }

        int difficultyLevel = UserManager.getInstance().getCurrentUser().getDifficultyLevel();

        double difficultyMultiplier = (double) difficultyLevel / BASE_DIFFICULTY;

        budget *= difficultyMultiplier;

        return (int) Math.max(waveCost, Math.round(budget));

    }
    private void spawnZombies(GameContext ctx, int budget) {
        if (ctx.getSeason() == null) {
            return;
        }

        if (isAncientEgyptFlagWave(ctx)) {
            ctx.announce("A sandstorm is approaching!");
        }

        Map<String, Zombie> pool = getAvailableZombiePool(ctx);

        System.out.println(pool.keySet());

        if (pool.isEmpty()) {
            throw new IllegalStateException(
                "No zombies are configured for level type: "
                    + ctx.getLevel().getLevelType()
            );
        }

        int minCost = pool.values().stream()
            .mapToInt(Zombie::getWavePointCost)
            .filter(cost -> cost > 0)
            .min()
            .orElse(-1);
        if (minCost < 0) {
            throw new IllegalStateException(
                "All zombies in the wave pool have invalid wave costs."
            );
        }

        Random random = (ctx.getLevel().getLevelType() == LevelType.BONUS)
            ? new Random(waveNumber)
            : new Random();
        ZombieFactory factory = new ZombieFactory(ctx.getDataManager());
        int remainingBudget = budget;

        while (remainingBudget >= minCost) {
            int finalRemainingBudget = remainingBudget;
            List<Map.Entry<String, Zombie>> affordable = pool.entrySet().stream()
                .filter(e -> e.getValue().getWavePointCost() > 0
                    && e.getValue().getWavePointCost() <= finalRemainingBudget)
                .collect(Collectors.toList());

            if (affordable.isEmpty()) {
                break;
            }

            Map.Entry<String, Zombie> chosen = affordable.get(random.nextInt(affordable.size()));
            String name = chosen.getKey();
            int cost = chosen.getValue().getWavePointCost();

            Zombie zombie = factory.create(name);
            placeZombie(ctx, zombie, random);
            ctx.addZombie(zombie);
            spawnedZombies.add(zombie);
            remainingBudget -= cost;

            Console.showMessage(String.format(
                "Zombie %s spawned at wave %d in lane %d which costed %d.\n",
                zombie.getName(), waveNumber, (int) zombie.getY(), cost));
        }
    }

    private Map<String, Zombie> getAvailableZombiePool(GameContext ctx) {
        Map<String, Zombie> allZombies = ctx.getDataManager().zombies.getZombieDataMap();

        if (ctx.getLevel().getLevelType() == LevelType.Zombotany_MG) {
            return allZombies.entrySet().stream()
                .filter(entry -> isZombotanyZombie(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        if (ctx.getLevel().getLevelType() == LevelType.BONUS) {
            return allZombies.entrySet().stream()
                .filter(entry -> !BossZombieRegistry.isBossId(entry.getValue().getId())
                    && !BossZombieRegistry.isBossName(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        if (ctx.getSeason() == null) {
            return Collections.emptyMap();
        }

        String seasonName = ctx.getSeason().getName();
        return allZombies.entrySet().stream()
            .filter(entry -> !BossZombieRegistry.isBossId(entry.getValue().getId())
                && !BossZombieRegistry.isBossName(entry.getKey())
                && ctx.getDataManager().zombies.isAvailableInChapter(entry.getKey(), seasonName))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    private void placeZombie(GameContext ctx, Zombie zombie, Random random) {
        int lane = random.nextInt(ctx.getLevel().getRows());
        double col = ctx.getLevel().getColumns();

        if (isAncientEgyptFlagWave(ctx)) {
            int colOffset = random.nextInt(4) + 1;
            zombie.startSandstormEntry(col - colOffset, random.nextFloat() * 1.4f);
        }

        zombie.setX(col);
        zombie.setY(lane);
    }

    private boolean isAncientEgyptFlagWave(GameContext ctx) {
        return isLastWave
            && ctx.getSeason() != null
            && ctx.getSeason().getName().equalsIgnoreCase("Ancient Egypt");
    }

    public boolean isThresholdReached() {
        if (!started || spawnedZombies.isEmpty() || initialTotalHp <= 0) {
            return false;
        }

        return currentHpRatio() <= THRESHOLD_HP_RATIO;
    }


    public float getProgress() {
        if (!started || initialTotalHp <= 0) {
            return 0f;
        }

        if (spawnedZombies.isEmpty()) {
            // هیچ زامبی‌ای برای این بودجه ساخته نشد؛ موج را کامل در نظر می‌گیریم.
            return 1f;
        }

        return (float) Math.min(1.0, Math.max(0.0, 1.0 - currentHpRatio()));
    }

    private double currentHpRatio() {
        int currentTotalHp = spawnedZombies.stream()
            .filter(z -> !z.isDead())
            .mapToInt(Zombie::getHp)
            .sum();

        return (double) currentTotalHp / initialTotalHp;
    }

    private boolean isZombotanyZombie(Zombie zombie) {
        if (zombie == null || zombie.getId() == null) {
            return false;
        }

        return zombie.getId().startsWith("ZombieZombotany");
    }

    public void reset() {
        started = false;
        spawnedZombies.clear();
        initialTotalHp = 0;
    }

    public int getWaveDelay() {
        return waveDelay;
    }
}
