package model.plants;

/**
 * How a plant selects its target. No column in plants.csv currently encodes
 * this, so it is left as a small placeholder enum; PlantLoader leaves it
 * null for now. Extend and wire up via PlantLoader once source data exists.
 */
public enum TargetingMode {
    FIRST_IN_LANE,
    ALL_IN_ROW,
    NEAREST,
    RANDOM,
    NONE
}
