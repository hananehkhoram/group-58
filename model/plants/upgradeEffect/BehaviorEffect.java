package model.plants.upgradeEffect;

/**
 * A non-numeric (behavioral) upgrade applied at a given plant level,
 * e.g. "Double Sun Chance" or "reset family cooldowns".
 * Parsed from CSV cells of the form BEHAVIOR:KEY.
 */
public class BehaviorEffect {
    private final BehaviorKey key;

    public BehaviorEffect(BehaviorKey key) {
        this.key = key;
    }

    public BehaviorKey getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
