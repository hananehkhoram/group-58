package com.workshop.model.MiniGame.Izambi.multiplayer;

/**
 * How a {@link NetworkIzambiMatch} talks to its peer. The real
 * implementation relays messages through the server via {@code GameClient};
 * tests or offline tools could swap in an in-memory implementation.
 */
public interface IzambiMatchTransport {
    void sendAction(String wire);

    void sendState(String wire);

    void sendEnd(String wire);
}
