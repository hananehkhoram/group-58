package com.workshop.model.MiniGame.Izambi.multiplayer;

import com.workshop.net.UserSnapshot;

/**
 * A single placement request sent from whoever's client did NOT do the
 * placement locally (the "guest" side of a match) to the host, which owns
 * the real simulation. Encoded as a plain wire string so it can travel
 * through the existing MATCH_ACTION relay message unchanged.
 */
public final class IzambiAction {
    public final MatchRole role;
    public final String unitName;
    public final int row;
    public final int column;

    public IzambiAction(MatchRole role, String unitName, int row, int column) {
        this.role = role;
        this.unitName = unitName;
        this.row = row;
        this.column = column;
    }

    public String toWire() {
        return UserSnapshot.join(role.name(), unitName, String.valueOf(row), String.valueOf(column));
    }

    public static IzambiAction fromWire(String wire) {
        String[] p = UserSnapshot.split(wire);
        return new IzambiAction(MatchRole.fromWire(p[0]), p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]));
    }
}
