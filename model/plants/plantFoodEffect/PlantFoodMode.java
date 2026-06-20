package model.plants.plantFoodEffect;

/**
 * How Plant Food amplifies a plant's base ability, derived from the
 * "Plant Food Effect" descriptions in plants.csv. Each BaseAbility
 * implementation interprets this according to its own kind (e.g. Shooters
 * uses BARRAGE to fire a burst of extra-strong shots; WallNut uses
 * GRANT_ARMOR to add bonus HP).
 */
public enum PlantFoodMode {
    /** Rapid burst of extra/empowered shots for a few seconds (most Shooters). */
    BARRAGE,
    /** One-time instant payload, plant is consumed (one-shot Sun Producers/Explosives). */
    INSTANT_CONSUME,
    /** Throws extra/empowered lobbed projectiles at random targets (Lobbers). */
    BARRAGE_LOB,
    /** Destroys/disables multiple random targets at once (Caulipower, Electric Blueberry, Tangle Kelp). */
    MULTI_TARGET_BURST,
    /** Grants temporary or permanent bonus HP / armor (Wall-nut family). */
    GRANT_ARMOR,
    /** Powerful single melee/area strike (Melee family). */
    HEAVY_STRIKE,
    /** Resets or refreshes the plant's own lifespan/cooldown across all instances of itself (Sea-shroom, Puff-shroom). */
    SELF_RESET,
    /** No effect: the plant has no Plant Food interaction (mints, Imitater). */
    NONE
}
