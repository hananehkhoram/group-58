package com.workshop.model.MiniGame.Izambi.multiplayer;

public enum MatchRole {
    PLANT,
    ZOMBIE;

    public MatchRole opposite() {
        return this == PLANT ? ZOMBIE : PLANT;
    }

    public static MatchRole fromWire(String s) {
        return "ZOMBIE".equalsIgnoreCase(s) ? ZOMBIE : PLANT;
    }
}
