package model.plants.enums;

/**
 * The on-hit effect/flavor of a shooter's projectile, derived from the
 * "Base Ability" descriptions in plants.csv (slowing, fire damage, armor
 * piercing + DoT, magic, electric, etc).
 */
public enum BulletType {
    /** Plain pea, no special on-hit effect. */
    NORMAL,
    /** Slows the target on hit (Snow Pea). */
    ICE,
    /** Deals bonus damage and melts ice (Fire Peashooter). */
    FIRE,
    /** Ignores armor and applies damage-over-time poison (Goo Peashooter). */
    POISON,
    /** Smoke projectile, passes through zombies (Fume-shroom). */
    SMOKE,
    /** Magic bolt: random direction, ignores obstacles, can hypnotize (Caulipower). */
    MAGIC,
    /** Instantly destroys whatever it hits (Electric Blueberry). */
    ELECTRIC,
    /** Homing bolt that always finds the nearest zombie (Cat-tail). */
    HOMING,
    /** Rolling projectile that bounces between lanes (Bowling Bulb). */
    BOWLING_NUT
}
