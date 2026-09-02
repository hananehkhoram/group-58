package com.workshop.model.mechanisms;

import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.level.LevelType;

public final class WaveSystem {

    private final GameContext ctx;

    public WaveSystem(GameContext ctx) {
        this.ctx = ctx;
    }

    public void update(double deltaTime) {
        if (getIZombieManager() != null) {
            ctx.setWaveSpawningFinished(true);
            return;
        }

        if (ctx.getLevel().getLevelType() == LevelType.PLANT_WHAT_YOU_GET) {
            if (!ctx.isManualStartCommandReceived()) {
                return;
            }
        }

        Wave[] waves = ctx.getLevel().getWaves();

        if (waves == null || waves.length == 0) {
            ctx.setWaveSpawningFinished(true);
            return;
        }

        if (ctx.getCurrentWaveIndex() == 0) {
            boolean startImmediately =
                ctx.getLevel().getLevelType() == LevelType.PLANT_WHAT_YOU_GET;
            long delayStart = ctx.getManualWaveStartTick();
            int delay = startImmediately ? 0 : waves[0].getWaveDelay();
            if (ctx.getTimeManager().getTotalTicks() < delayStart + delay) {
                return;
            }
            ctx.recordFirstWaveStart();
            spawnWave(waves[0]);
            return;
        }

        if (ctx.getCurrentWaveIndex() >= waves.length) {
            if (ctx.getLevel().getLevelType() == LevelType.Beghouled_MG) {

                int previousIndex =
                    (ctx.getCurrentWaveIndex() - 1) % waves.length;

                Wave previousWave = waves[previousIndex];

                if (previousWave.isThresholdReached()) {
                    int nextIndex =
                        ctx.getCurrentWaveIndex() % waves.length;

                    Wave nextWave = waves[nextIndex];
                    nextWave.reset();
                    spawnWave(nextWave);
                }

                return;
            }

            ctx.setWaveSpawningFinished(true);
            return;
        }

        Wave previousWave = waves[ctx.getCurrentWaveIndex() - 1];
        if (previousWave.isThresholdReached()) {
            Wave nextWave = waves[ctx.getCurrentWaveIndex()];
            spawnWave(nextWave);
        }
    }

    private void spawnWave(Wave wave) {
        wave.start(ctx);
        ctx.incrementWaveIndex();
        ctx.setActiveWaveInProgress(true);
    }

    private IZombieManager getIZombieManager() {
        if (ctx.getLevelManager() instanceof IZombieManager manager) {
            return manager;
        }
        return null;
    }
}
