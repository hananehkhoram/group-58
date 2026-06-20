package model.plants;

/**
 * Plant tags, matches the tokens found in the "tags" column of plants.csv
 * (semicolon-separated in the CSV, parsed into an EnumSet&lt;Tag&gt;).
 */
public enum Tag {
    DAY,
    NIGHT,
    SHROOM,
    RAMP_UP,
    PEA,
    ICE,
    CHARGE,
    MAGIC,
    FIRE,
    POISON,
    STACK,
    WATER,
    AOE,
    TRAP,
    MOVE_ZOMBIES,
    SUN,
    EXPLOSIVE_TAG
}
