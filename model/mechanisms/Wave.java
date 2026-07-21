package model.mechanisms;

import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.user.UserManager;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Wave {

    private static final double WAVE_DIFFICULTY_GROWTH = 1.25;
    private static final double FINAL_WAVE_MULTIPLIER = 2.0;
    private static final double THRESHOLD_HP_RATIO = 0.25;
    private static final int BASE_DIFFICULTY = 3;

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

        announceStart();

        if (ctx.getSeason() != null) {
            ctx.getSeason().onWaveStart(ctx, waveNumber, isLastWave);
        }

        spawnZombies(ctx, calculateEffectiveBudget());
        initialTotalHp = spawnedZombies.stream().mapToInt(Zombie::getHp).sum();
    }

    private void announceStart() {
        ConsoleView.showMessage(isLastWave
                ? "The final wave has come.\n"
                : "Wave " + waveNumber + " started.\n");
    }

    private int calculateEffectiveBudget() {
        double budget = waveCost;

        for (int i = 2; i <= waveNumber; i++) {
            boolean isFinalStep = (i == waveNumber && isLastWave);
            budget *= isFinalStep ? FINAL_WAVE_MULTIPLIER : WAVE_DIFFICULTY_GROWTH;
        }

        int difficultyLevel = UserManager.getInstance().getCurrentUser().getDifficultyLevel();
        budget *= (double) BASE_DIFFICULTY / difficultyLevel;

        return (int) Math.max(1, Math.round(budget));
    }

    private void spawnZombies(GameContext ctx, int budget) {
        if (ctx.getSeason() == null) {
            return;
        }

        Map<String, Zombie> pool = getAvailableZombiePool(ctx);
        if (pool.isEmpty()) {
            return;
        }

        int minCost = pool.values().stream()
                .mapToInt(Zombie::getWavePointCost)
                .filter(cost -> cost > 0)
                .min()
                .orElse(-1);
        if (minCost < 0) {
            return;
        }

        Random random = new Random();
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

            ConsoleView.showMessage(String.format(
                    "Zombie %s spawned at wave %d in lane %d which costed %d.\n",
                    zombie.getName(), waveNumber, (int) zombie.getY(), cost));
        }
    }

    private Map<String, Zombie> getAvailableZombiePool(GameContext ctx) {
        String seasonName = ctx.getSeason().getName();
        return ctx.getDataManager().zombies.getZombieDataMap().entrySet().stream()
                .filter(e -> ctx.getDataManager().zombies.isAvailableInChapter(e.getKey(), seasonName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void placeZombie(GameContext ctx, Zombie zombie, Random random) {
        int lane = random.nextInt(ctx.getLevel().getRows());
        double col = ctx.getLevel().getColumns() - 1;

        if (isAncientEgyptFlagWave(ctx)) {
            int colOffset = random.nextInt(4) + 1;
            col -= colOffset;
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

        int currentTotalHp = spawnedZombies.stream()
                .filter(z -> !z.isDead())
                .mapToInt(Zombie::getHp)
                .sum();

        return (double) currentTotalHp / initialTotalHp <= THRESHOLD_HP_RATIO;
    }

    public int getWaveNumber() { return waveNumber; }
    public boolean isLastWave() { return isLastWave; }
    public int getWaveCost() { return waveCost; }
    public int getWaveDelay() { return waveDelay; }
    public boolean isStarted() { return started; }
    public List<Zombie> getSpawnedZombies() { return spawnedZombies; }
}