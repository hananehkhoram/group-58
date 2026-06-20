package model.plants.plantAbilities;

/**
 * The kind of payload a Lobber-family plant launches, derived from the
 * "Base Ability" descriptions in plants.csv.
 */
public enum LobType {
    /** Plain arcing projectile, single damage value (Cabbage-pult). */
    NORMAL,
    /** Two payload variants: a low-damage kernel or a stunning butter shot (Kernel-pult). */
    KERNEL_OR_BUTTER,
    /** Heavy payload with splash/area damage (Melon-pult). */
    AOE,
    /** Heavy splash payload that also slows zombies on impact (Winter Melon). */
    AOE_ICE,
    /** Splash payload that also raises ambient temperature (Pepper-pult). */
    AOE_FIRE
}
