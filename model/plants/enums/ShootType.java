package model.plants.enums;

public enum ShootType {
    /** Single straight shot down the plant's own lane (Peashooter). */
    STRAIGHT,
    /** Multiple straight shots fired in sequence down the same lane (Repeater, Mega Gatling Pea). */
    STRAIGHT_SEQUENTIAL,
    /** Straight shot fired simultaneously across 3 parallel lanes (Threepeater). */
    TRI_LANE,
    /** Shots fired in 4 diagonal directions (Rotobaga). */
    QUAD_DIAGONAL,
    /** One shot forward, two shots backward at once (Split Pea). */
    FRONT_AND_BACK,
    /** Shots fired in 5 directions including backward (Starfruit). */
    STAR_BURST,
    /** Requires a charge-up delay before releasing a single heavy shot (Citron). */
    CHARGED,
    /** Random direction, passes through obstacles, homes toward a zombie (Caulipower). */
    RANDOM_HOMING,
    /** Random direction bolt that instantly destroys its target (Electric Blueberry). */
    RANDOM_INSTANT,
    /** Always targets the nearest zombie (Cat-tail). */
    NEAREST_TARGET,
    /** Short range, limited lifespan shot (Sea-shroom, Puff-shroom). */
    SHORT_RANGE,
    /** Passes through multiple zombies in its lane (Cactus, Fume-shroom). */
    PIERCING,
    /** Bounces / rolls between lanes rather than flying straight (Bowling Bulb). */
    BOWLING
}
