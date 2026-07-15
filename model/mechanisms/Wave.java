package model.mechanisms;

import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.user.UserManager;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Wave {
    private final int waveNumber;
    private final int waveCost;
    private final int waveDelay;
    private boolean isLastWave;

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
        this.started = true;

        if (this.isLastWave) {
            ConsoleView.showMessage("The final wave has come.\n");
        } else {
            ConsoleView.showMessage("Wave " + this.waveNumber + " started.\n");
        }

        if (ctx.getSeason() != null) {
            ctx.getSeason().onWaveStart(ctx, this.waveNumber, this.isLastWave);
        }

        int effectiveBudget = calculateEffectiveBudget();
        spawnZombies(ctx, effectiveBudget);

        this.initialTotalHp = spawnedZombies.stream().mapToInt(Zombie::getHp).sum();
    }

    private int calculateEffectiveBudget() {
        double budget = this.waveCost;

        for (int i = 2; i <= this.waveNumber; i++) {
            if (i == this.waveNumber && this.isLastWave) {
                budget *= 2.0;
            } else {
                budget *= 1.25;
            }
        }

        int dl = UserManager.getInstance().getCurrentUser().getDifficultyLevel();
        budget = budget * (dl / 3.0);

        return (int) Math.max(1, budget);
    }

    private void spawnZombies(GameContext ctx, int budget) {
        int remainingBudget = budget;
        Random random = new Random();

        List<String> availableZombieNames = new ArrayList<>(ctx.getDataManager().zombies.getZombieDataMap().keySet());
        if (availableZombieNames.isEmpty()) return;

        ZombieFactory factory = new ZombieFactory(ctx.getDataManager());

        while (remainingBudget > 0) {
            String randomName = availableZombieNames.get(random.nextInt(availableZombieNames.size()));
            Zombie template = ctx.getDataManager().zombies.get(randomName);

            if (template == null) continue;
            int cost = template.getWavePointCost();

            if (cost <= remainingBudget) {
                Zombie zombie = factory.create(randomName);

                int lane = random.nextInt(ctx.getLevel().getRows());
                double col = ctx.getLevel().getColumns() - 1;

                if (ctx.getSeason() != null && ctx.getSeason().getName().equalsIgnoreCase("Ancient Egypt") && this.isLastWave) {
                    int colOffset = random.nextInt(4) + 1;
                    col = (ctx.getLevel().getColumns() - 1) - colOffset;
                }

                zombie.setX(col);
                zombie.setY(lane);

                ctx.addZombie(zombie);
                this.spawnedZombies.add(zombie);

                remainingBudget -= cost;

                ConsoleView.showMessage(String.format(
                        "Zombie %s spawned at wave %d in lane %d which costed %d.\n",
                        zombie.getName(), this.waveNumber, lane + 1, cost
                ));
            } else {
                break;
            }
        }
    }

    public boolean isThresholdReached() {
        if (!started || spawnedZombies.isEmpty()) return false;

        int currentTotalHp = spawnedZombies.stream()
                .filter(z -> !z.isDead())
                .mapToInt(Zombie::getHp)
                .sum();

        if (initialTotalHp == 0) return true;

        double remainingHpRatio = (double) currentTotalHp / initialTotalHp;
        return remainingHpRatio <= 0.25;
    }

    public int getWaveNumber() { return waveNumber; }
    public boolean isLastWave() { return isLastWave; }
    public int getWaveCost() { return waveCost; }
    public int getWaveDelay() { return waveDelay; }
    public boolean isStarted() { return started; }
    public List<Zombie> getSpawnedZombies() { return spawnedZombies; }
}