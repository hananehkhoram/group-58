package model.projectile;

/**
 * The on-hit effect of a projectile, independent of its trajectory/shape.
 * Trajectory (straight, lobbed, homing, bowling, piercing) lives separately
 * in TrajectoryType — do not mix the two here.
 */
public enum BulletType {
    /** Plain damage, no special on-hit effect. */
    NORMAL,
    /** Slows target's movement/attack speed on hit (Snow Pea). */
    ICE,
    /** Double damage; melts ice on the target if it's frozen (Fire Peashooter). */
    FIRE,
    /** Ignores armor, applies damage-over-time (Goo Peashooter, Hunter Zombie's shard vs plants). */
    POISON,
    /** Passes through zombies without stopping (Fume-shroom); pair with TrajectoryType.PIERCING. */
    SMOKE,
    /** Ignores obstacles; can hypnotize on hit (Caulipower). */
    MAGIC,
    /** Instantly destroys the target (Electric Blueberry). */
    ELECTRIC,
    /** Disables the target and blocks other projectiles from passing (Octopus zombie's throw). */
    IMMOBILIZE
}