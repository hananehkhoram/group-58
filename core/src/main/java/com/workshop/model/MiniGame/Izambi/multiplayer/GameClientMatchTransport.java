package com.workshop.model.MiniGame.Izambi.multiplayer;

import com.workshop.net.GameClient;

/** Sends {@link NetworkIzambiMatch} messages over the real server connection. */
public final class GameClientMatchTransport implements IzambiMatchTransport {
    private final GameClient client;
    private final String matchId;

    public GameClientMatchTransport(GameClient client, String matchId) {
        this.client = client;
        this.matchId = matchId;
    }

    @Override
    public void sendAction(String wire) {
        runAsync(() -> client.sendMatchAction(matchId, wire));
    }

    @Override
    public void sendState(String wire) {
        runAsync(() -> client.sendMatchState(matchId, wire));
    }

    @Override
    public void sendEnd(String wire) {
        runAsync(() -> client.sendMatchEnd(matchId, wire));
    }

    // These sends are fire-and-forget from the caller's point of view (the
    // model layer never waits on their result), but GameClient's request()
    // blocks the calling thread until the server replies. Running each send
    // on its own short-lived thread keeps the render thread from stalling.
    private static void runAsync(Runnable task) {
        Thread thread = new Thread(task, "izambi-match-send");
        thread.setDaemon(true);
        thread.start();
    }
}
