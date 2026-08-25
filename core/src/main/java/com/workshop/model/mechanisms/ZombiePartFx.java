package com.workshop.model.mechanisms;

import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.ArmorType;

public final class ZombiePartFx {

    public enum Kind {
        ARMOR,
        ARM,
        HEAD
    }

    public final int row;
    public final double x;
    public final Kind kind;
    public final ArmorType armorType;
    public final Zombie zombie;

    public ZombiePartFx(int row, double x, Kind kind) {
        this(row, x, kind, null, null);
    }

    public ZombiePartFx(int row, double x, Kind kind, ArmorType armorType) {
        this(row, x, kind, armorType, null);
    }

    public ZombiePartFx(int row, double x, Kind kind, ArmorType armorType, Zombie zombie) {
        this.row = row;
        this.x = x;
        this.kind = kind;
        this.armorType = armorType;
        this.zombie = zombie;
    }
}
