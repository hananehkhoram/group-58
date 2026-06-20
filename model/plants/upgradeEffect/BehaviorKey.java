package model.plants.upgradeEffect;

/**
 * Identifies a non-numeric (behavioral) upgrade unlocked at a given level.
 * Generated from every distinct "BEHAVIOR:&lt;KEY&gt;" token in plants.csv.
 */
public enum BehaviorKey {
    AOE_ON_DEATH,
    CAN_CRUSH_2X,
    DOUBLE_SUN_CHANCE,
    EXPLODE_ON_FINISH,
    MELT_AREA_3X3,
    PLANT_FOOD_ON_ENTERANCE,
    RESET_FAMILY_COOLDOWNS,
    TARGET_PRIORITY_UP,
    ZOMBIE_DMG_BUFF,
    ZOMBIE_HP_BUFF
}
