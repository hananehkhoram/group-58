package model.plants.plantAbilities;

/**
 * The defensive behavior variant of a Wall-nut family plant, derived from
 * the "Base Ability" descriptions in plants.csv.
 */
public enum WallNutType {
    /** Plain high-HP blocker (Wall-nut). */
    BLOCKER,
    /** Tall blocker that also prevents zombies from vaulting over it (Tall-nut). */
    TALL_BLOCKER,
    /** Blocker that reflects damage back at attackers (Endurian). */
    REFLECTIVE,
    /** On being eaten, forces the attacking zombie into an adjacent lane (Garlic). */
    LANE_REDIRECT,
    /** Pulls zombies from neighboring lanes into its own lane (Sweet Potato). */
    LANE_ATTRACT,
    /** Stackable cover planted on top of another plant to shield it (Pumpkin). */
    STACKABLE_COVER,
    /** Acts as a blocker and generates sun on every hit it takes (Sun Bean). */
    SUN_GENERATING
}
