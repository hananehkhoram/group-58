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
    private float timeAccumulator;

    private boolean ended;
    private MatchRole winner;

    private static final int PLANT_STARTING_SUN = 300;
    private static final float TICK_DURATION = 0.1f;

    public void start(MenuManager menuManager, int levelNumber) {
        izambi = new Izambi();
        izambi.startMultiplayerMatch(menuManager, levelNumber);
        plantSun = PLANT_STARTING_SUN;
        elapsedSeconds = 0f;
        timeAccumulator = 0f;
        ended = false;
        winner = null;
    }

    public void update(float delta) {
        if (ended) {
            return;
        }

        int sunBeforeTick = izambi.getCtx().getSunAmount();

        timeAccumulator += delta;
        while (timeAccumulator >= TICK_DURATION && !ended) {
            izambi.getCtx().getTimeManager().advanceTime(1);
            izambi.getGameEngine().update(TICK_DURATION);
            timeAccumulator -= TICK_DURATION;
        }

        // Same "mirror whatever the producers made" trick as the networked
        // match: keeps both players' sun fair without hand-tuned constants.
        int sunGained = izambi.getCtx().getSunAmount() - sunBeforeTick;
        if (sunGained > 0) {
            plantSun += sunGained;
        }

        elapsedSeconds += delta;

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
