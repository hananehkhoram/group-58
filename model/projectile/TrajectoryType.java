package model.projectile;

/**
 * The geometric/movement pattern of a projectile, independent of its on-hit
 * effect (BulletType). A single BulletType can pair with any trajectory
 * (e.g. FIRE + STRAIGHT, FIRE + PIERCING via Torchwood + Threepeater).
 */
public enum TrajectoryType {
    /** Single straight line down the lane (Peashooter). */
    STRAIGHT,
    /** Passes through multiple targets in the same lane, hitting each once (Cactus, strike-through). */
    PIERCING,
    /** Locks onto and tracks a target anywhere on the board (Cat-tail). */
    HOMING,
    /** Ignores obstacles/gravestones, lands directly on the target (Lobber family; only type that hits divers underwater). */
    LOBBED,
    /** Rolls forward, changes lane by 45/90 degrees on impact (Bowling minigame). */
    BOWLING
}