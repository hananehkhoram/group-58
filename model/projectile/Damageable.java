package model.projectile;

public interface Damageable {
    double getX();
    int getRow();
    boolean isDead();

    void takeDamage(int amount);

    /** Poison and similar effects bypass armor; override in Zombie to skip the armor check. */
    default void takeArmorPiercingDamage(int amount) {
        takeDamage(amount);
    }

    /** Fire melts ice; override where an iced state exists (Zombie, Plant). No-op by default. */
    default void meltIce() {}

    /** Ice/Hunter-zombie-shard slow or freeze; override where relevant. No-op by default. */
    default void applySlowOrFreeze() {}
}