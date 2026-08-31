package com.workshop.model.MiniGame.Izambi.multiplayer;

import com.workshop.net.GameClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sends {@link NetworkIzambiMatch} messages over the real server connection.
 * <p>
 * Sends must go out in the order they were issued — the host broadcasts a
 * STATE snapshot roughly every 0.2s, and if two of those ever reached the
 * guest out of order, the guest would briefly render a stale
 * position/timer, and then "jump" once a newer one finally lands. Since
 * {@code GameClient}'s send methods block until the server replies, a
 * single dedicated background thread drains an ordered queue instead of
 * spawning one thread per call (which gave no ordering guarantee at all —
 * that was the actual bug behind the choppy/backwards-looking timer and
 * unit positions on the guest's screen).
 */
public final class GameClientMatchTransport implements IzambiMatchTransport {
    private final GameClient client;
    private final String matchId;
    private final BlockingQueue<Runnable> outbox = new LinkedBlockingQueue<>();

    public GameClientMatchTransport(GameClient client, String matchId) {
        this.client = client;
        this.matchId = matchId;

        Thread sender = new Thread(this::drain, "izambi-match-send");
        sender.setDaemon(true);
        sender.start();
    }

    private void drain() {
        try {
            while (true) {
                outbox.take().run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void sendAction(String wire) {
        outbox.offer(() -> client.sendMatchAction(matchId, wire));
    }

    @Override
    public void sendState(String wire) {
        outbox.offer(() -> client.sendMatchState(matchId, wire));
    }

    @Override
    public void sendEnd(String wire) {
        outbox.offer(() -> client.sendMatchEnd(matchId, wire));
    }
}
