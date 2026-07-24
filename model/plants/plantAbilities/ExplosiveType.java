package model.plants.plantAbilities;

public enum ExplosiveType {
    /** Arms after a delay, explodes on zombie contact (Potato Mine). */
    TIMED_MINE,
    /** Faster arm time, explodes in a 3x3 area on contact (Primal Potato Mine). */
    TIMED_MINE_AOE,
    /** Explodes immediately in a 3x3 area when planted/triggered (Cherry Bomb). */
    INSTANT_AOE,
    /** Crushes the first adjacent zombie, no explosion (Squash). */
    CRUSH,
    /** Explodes in a 3x3 area and launches bouncing projectiles (Grapeshot). */
    INSTANT_AOE_SHRAPNEL,
    /** Instantly sets an entire lane on fire, melts ice (Jalapeno). */
    LANE_FIRE,
    /** Massive explosion across the whole board, leaves a crater (Doom-shroom). */
    BOARD_WIDE,
    /** Pulls the first aquatic zombie underwater, instakill (Tangle Kelp). */
    WATER_TRAP,
    /** Freezes the first zombie that steps on it (Iceberg Lettuce). */
    FREEZE_TRAP,
    /** Freezes and stuns every zombie on the board (Ice-shroom). */
    BOARD_WIDE_FREEZE,
    /** Melts a 3x3 ice area instantly (Hot Potato). */
    MELT_AREA,
    /** Destroys graves (Grave Buster). */
    GRAVE_DESTROY
}
