package com.workshop.model.MiniGame.Izambi.multiplayer;

import com.workshop.controller.MenuManager;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.MiniGame.Izambi.Izambi;

/**
 * Bonus "Couch Play" mode: both players share one device and one running
 * simulation, so there is no host/guest split and no network at all. The
 * plant player places with the mouse, the zombie player places with the
 * keyboard; both inputs land on this same {@link Izambi} instance.
 */
public final class CouchIzambiMatch {

    private Izambi izambi;
    private int plantSun;
    private float elapsedSeconds;
    private float incomeAccumulator;

    private boolean ended;
    private MatchRole winner;

    private static final int PLANT_STARTING_SUN = 300;
    private static final int PLANT_PASSIVE_INCOME = 25;
    private static final float PLANT_INCOME_INTERVAL_SECONDS = 12f;

    public void start(MenuManager menuManager, int levelNumber) {
        izambi = new Izambi();
        izambi.startMultiplayerMatch(menuManager, levelNumber);
        plantSun = PLANT_STARTING_SUN;
        elapsedSeconds = 0f;
        incomeAccumulator = 0f;
        ended = false;
        winner = null;
    }

    public void update(float delta) {
        if (ended) {
            return;
        }
        izambi.getGameEngine().update(delta);

        elapsedSeconds += delta;
        incomeAccumulator += delta;
        while (incomeAccumulator >= PLANT_INCOME_INTERVAL_SECONDS) {
            incomeAccumulator -= PLANT_INCOME_INTERVAL_SECONDS;
            plantSun += PLANT_PASSIVE_INCOME;
        }

        IZombieManager manager = izambi.getIZombieManager();
        if (manager.areAllBrainsEaten()) {
            finish(MatchRole.ZOMBIE);
        } else if (remainingSeconds() <= 0) {
            finish(MatchRole.PLANT);
        } else if (manager.shouldPlayerLose(izambi.getCtx())) {
            finish(MatchRole.PLANT);
        }
    }

    private void finish(MatchRole winnerRole) {
        ended = true;
        winner = winnerRole;
        izambi.getCtx().setBattleStarted(false);
        izambi.getCtx().setGameEnded(true);
    }

    public boolean placePlant(String plantName, int row, int column) {
        if (ended) {
            return false;
        }
        int cost = izambi.getPlantCost(plantName);
        if (cost < 0 || plantSun < cost) {
            return false;
        }
        if (!izambi.placePlant(plantName, row, column)) {
            return false;
        }
        plantSun -= cost;
        return true;
    }

    public boolean placeZombie(String zombieName, int row, int column) {
        if (ended) {
            return false;
        }
        int rebalancedCost = Izambi.getMultiplayerZombieCost(zombieName);
        Integer costOverride = rebalancedCost >= 0 ? rebalancedCost : null;
        return izambi.placeZombie(zombieName, row, column, costOverride);
    }

    public int remainingSeconds() {
        return Math.max(0, NetworkIzambiMatch.MATCH_DURATION_SECONDS - (int) elapsedSeconds);
    }

    public int getPlantSun() {
        return plantSun;
    }

    public Izambi getIzambi() {
        return izambi;
    }

    public boolean isEnded() {
        return ended;
    }

    public MatchRole getWinner() {
        return winner;
    }
}
