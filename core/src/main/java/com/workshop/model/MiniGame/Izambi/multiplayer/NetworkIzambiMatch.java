package com.workshop.model.MiniGame.Izambi.multiplayer;

import com.workshop.controller.MenuManager;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.MiniGame.Izambi.Izambi;

/**
 * Drives one side of a networked "I, Zombie" match.
 * <p>
 * Only the HOST runs the real simulation (a normal {@link Izambi} +
 * GameEngine): it applies both its own local placements and the placements
 * relayed from the guest, ticks the engine every frame, decides win/lose,
 * and periodically broadcasts an {@link IzambiSnapshot}. The GUEST never
 * simulates anything locally — it renders the latest snapshot it received
 * and sends its own placements to the host as {@link IzambiAction}s.
 * <p>
 * This split avoids any risk of the two sides' simulations drifting apart,
 * at the cost of the guest seeing the game a couple hundred milliseconds
 * behind the host.
 */
public final class NetworkIzambiMatch {

    public static final int MATCH_DURATION_SECONDS = 120;
    private static final int PLANT_STARTING_SUN = 150;
    private static final int PLANT_PASSIVE_INCOME = 25;
    private static final float PLANT_INCOME_INTERVAL_SECONDS = 12f;
    private static final float STATE_BROADCAST_INTERVAL = 0.2f;

    private final boolean isHost;
    private final MatchRole localRole;
    private final IzambiMatchTransport transport;

    private Izambi izambi;
    private int plantSun;
    private float elapsedSeconds;
    private float incomeAccumulator;
    private float broadcastAccumulator;

    private boolean ended;
    private MatchRole winner;

    private IzambiSnapshot latestSnapshot;

    public NetworkIzambiMatch(boolean isHost, MatchRole localRole, IzambiMatchTransport transport) {
        this.isHost = isHost;
        this.localRole = localRole;
        this.transport = transport;
    }

    /** Only meaningful on the host: starts the real simulation. */
    public void startHost(MenuManager menuManager, int levelNumber) {
        if (!isHost) {
            return;
        }
        izambi = new Izambi();
        izambi.startMultiplayerMatch(menuManager, levelNumber);
        plantSun = PLANT_STARTING_SUN;
        elapsedSeconds = 0f;
        incomeAccumulator = 0f;
        broadcastAccumulator = 0f;
        ended = false;
        winner = null;
    }

    public void update(float delta) {
        if (ended) {
            return;
        }
        if (isHost) {
            updateHost(delta);
        }
        // Guest has nothing to simulate; it just waits for state pushes.
    }

    private void updateHost(float delta) {
        izambi.getGameEngine().update(delta);

        elapsedSeconds += delta;
        incomeAccumulator += delta;
        while (incomeAccumulator >= PLANT_INCOME_INTERVAL_SECONDS) {
            incomeAccumulator -= PLANT_INCOME_INTERVAL_SECONDS;
            plantSun += PLANT_PASSIVE_INCOME;
        }

        IZombieManager manager = izambi.getIZombieManager();
        int remaining = remainingSeconds();

        if (manager.areAllBrainsEaten()) {
            finish(MatchRole.ZOMBIE);
        } else if (remaining <= 0) {
            finish(MatchRole.PLANT);
        } else if (manager.shouldPlayerLose(izambi.getCtx())) {
            finish(MatchRole.PLANT);
        }

        broadcastAccumulator += delta;
        if (broadcastAccumulator >= STATE_BROADCAST_INTERVAL || ended) {
            broadcastAccumulator = 0f;
            IzambiSnapshot snap = captureSnapshot();
            latestSnapshot = snap;
            transport.sendState(snap.toWire());
            if (ended) {
                transport.sendEnd(winner.name());
            }
        }
    }

    private void finish(MatchRole winnerRole) {
        ended = true;
        winner = winnerRole;
        izambi.getCtx().setBattleStarted(false);
    }

    private IzambiSnapshot captureSnapshot() {
        return IzambiSnapshot.capture(
            izambi.getCtx(),
            izambi.getIZombieManager(),
            plantSun,
            remainingSeconds(),
            ended,
            winner
        );
    }

    public int remainingSeconds() {
        if (!isHost) {
            return latestSnapshot == null ? MATCH_DURATION_SECONDS : latestSnapshot.remainingSeconds;
        }
        return Math.max(0, MATCH_DURATION_SECONDS - (int) elapsedSeconds);
    }

    /**
     * Attempts to place the local player's unit (a plant if localRole is
     * PLANT, a zombie if ZOMBIE). On the host this applies immediately; on
     * the guest this just asks the host to do it and returns true if the
     * request was sent (not whether it was accepted).
     */
    public boolean placeLocal(String unitName, int row, int column) {
        if (ended) {
            return false;
        }
        if (isHost) {
            return applyPlacement(localRole, unitName, row, column);
        }
        transport.sendAction(new IzambiAction(localRole, unitName, row, column).toWire());
        return true;
    }

    /** Host-only: applies a placement relayed from the guest. */
    public void onRemoteAction(String wire) {
        if (!isHost || ended) {
            return;
        }
        IzambiAction action = IzambiAction.fromWire(wire);
        applyPlacement(action.role, action.unitName, action.row, action.column);
    }

    private boolean applyPlacement(MatchRole role, String unitName, int row, int column) {
        if (role == MatchRole.PLANT) {
            int cost = izambi.getPlantCost(unitName);
            if (cost < 0 || plantSun < cost) {
                return false;
            }
            if (!izambi.placePlant(unitName, row, column)) {
                return false;
            }
            plantSun -= cost;
            return true;
        }
        return izambi.placeZombie(unitName, row, column);
    }

    /** Guest-only: applies a state snapshot pushed by the host. */
    public void onRemoteState(String wire) {
        if (isHost) {
            return;
        }
        latestSnapshot = IzambiSnapshot.fromWire(wire);
        if (latestSnapshot.ended) {
            ended = true;
            winner = latestSnapshot.winner;
        }
    }

    public void onRemoteEnd(String wire) {
        ended = true;
        winner = MatchRole.fromWire(wire);
    }

    public void onOpponentLeft() {
        if (!ended) {
            ended = true;
            winner = localRole;
        }
    }

    public IzambiSnapshot snapshot() {
        if (isHost) {
            return latestSnapshot == null ? captureSnapshot() : latestSnapshot;
        }
        return latestSnapshot;
    }

    public boolean isHost() {
        return isHost;
    }

    /** Non-null only on the host: the real running simulation. */
    public Izambi getHostIzambi() {
        return izambi;
    }

    public MatchRole getLocalRole() {
        return localRole;
    }

    public boolean isEnded() {
        return ended;
    }

    public MatchRole getWinner() {
        return winner;
    }

    public boolean localWon() {
        return ended && winner == localRole;
    }
}
