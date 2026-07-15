package model.plants.plantAbilities;

/**
 * The distinct effect of a Modifier-family plant — these don't fit
 * Shooters/Lobber/Explosive/Melee/WallNut/SunProducers, each does something
 * structurally different, derived from plants.csv "Base Ability" text.
 */
public enum ModifierType {
    /** Converts passing bullets into fire bullets (Torchwood). */
    BULLET_BUFF,
    /** On being eaten, the attacking zombie is hypnotized and fights for the player (Hypno-shroom). */
    HYPNOTIZE,
    /** Copies another plant, allowing the same card to be used twice (Imitater). */
    COPY_PLANT,
    /** Provides a plantable surface on water tiles (Lily Pad). */
    WATER_PLATFORM,
    FAMILY_BUFF
}
