package model.plants.upgradeEffect;

/**
 * A numeric upgrade applied at a given plant level, e.g. "HP +150" or "Cooldown -5s".
 * Parsed from CSV cells of the form STAT:KEY:[+-]NUMBER[UNIT].
 */
public class StatEffect {
    private final StatKey key;
    private final double amount;
    private final String unit; // "", "s", "%", "x", "Tile" — may be empty

    public StatEffect(StatKey key, double amount, String unit) {
        this.key = key;
        this.amount = amount;
        this.unit = unit == null ? "" : unit;
    }

    public StatKey getKey() {
        return key;
    }

    public double getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        String sign = amount >= 0 ? "+" : "";
        return key + " " + sign + amount + unit;
    }
}
