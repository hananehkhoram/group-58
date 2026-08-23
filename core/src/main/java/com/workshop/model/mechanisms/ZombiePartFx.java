package com.workshop.model.mechanisms;

public final class ZombiePartFx {

    public enum Kind {
        ARMOR,
        ARM,
        HEAD
    }

    public final int row;
    public final double x;
    public final Kind kind;

    public ZombiePartFx(int row, double x, Kind kind) {
        this.row = row;
        this.x = x;
        this.kind = kind;
    }
}
