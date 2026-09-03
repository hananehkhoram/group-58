package com.workshop.model.zombie;

import com.workshop.view.Console;

import java.util.List;

/**
 * Ice / butter / stun status for a zombie.
 */
final class ZombieStatusEffects {

    private static final double BUTTER_STUN_SECONDS = 4.0;

    private boolean isIced;
    private double iceHp;
    private double butterRemaining;
    private double stunRemaining;
    private boolean initialFrozenBlock;

    void setAsInitialFrozenBlock(List<Effects> effects) {
        initialFrozenBlock = true;
        isIced = true;
        iceHp = 600;
        if (!effects.contains(Effects.FROZEN)) {
            effects.add(Effects.FROZEN);
        }
    }

    boolean isIced() {
        return isIced;
    }

    boolean isInitialFrozenBlock() {
        return initialFrozenBlock;
    }

    double getIceHp() {
        return iceHp;
    }

    void applyButter(List<Effects> effects, boolean isBoss) {
        if (isBoss) {
            return;
        }
        butterRemaining = BUTTER_STUN_SECONDS;
        if (effects != null && !effects.contains(Effects.BUTTERED)) {
            effects.add(Effects.BUTTERED);
        }
    }

    boolean isButtered() {
        return butterRemaining > 0;
    }

    void applyStun(double seconds, Zombie zombie) {
        stunRemaining = Math.max(stunRemaining, seconds);
        zombie.setEating(false);
    }

    boolean isStunned() {
        return stunRemaining > 0;
    }

    boolean tick(Zombie zombie, List<Effects> effects, boolean isBoss, double deltaTime) {
        if (isBoss && isIced) {
            isIced = false;
            iceHp = 0;
            initialFrozenBlock = false;
            if (effects != null) {
                effects.remove(Effects.FROZEN);
            }
        }

        if (butterRemaining > 0) {
            butterRemaining -= deltaTime;
            if (butterRemaining <= 0) {
                butterRemaining = 0;
                if (effects != null) {
                    effects.remove(Effects.BUTTERED);
                }
            } else {
                zombie.setEating(false);
                return true;
            }
        }

        if (stunRemaining > 0) {
            stunRemaining -= deltaTime;
            if (stunRemaining < 0) {
                stunRemaining = 0;
            }
            zombie.setEating(false);
        }

        return initialFrozenBlock;
    }

    boolean absorbFrozenBlockDamage(double damage, List<Effects> effects) {
        if (!initialFrozenBlock) {
            return false;
        }
        iceHp -= damage;
        if (iceHp <= 0) {
            initialFrozenBlock = false;
            isIced = false;
            if (effects != null) {
                effects.remove(Effects.FROZEN);
            }
            Console.showMessage("Zombie broke free from ice!");
        }
        return true;
    }

    void absorbIceShellDamage(double damage, List<Effects> effects) {
        if (!isIced) {
            return;
        }
        iceHp -= damage;
        if (iceHp <= 0) {
            isIced = false;
            if (effects != null) {
                effects.remove(Effects.FROZEN);
            }
            Console.showMessage("Zombie broke free from ice!");
        }
    }

    void meltIce(List<Effects> effects) {
        if (isIced) {
            iceHp = 0;
            isIced = false;
            initialFrozenBlock = false;
            if (effects != null) {
                effects.remove(Effects.FROZEN);
            }
        }
    }

    void meltIce(double amount, List<Effects> effects) {
        if (!isIced) {
            return;
        }
        iceHp -= amount;
        if (iceHp <= 0) {
            iceHp = 0;
            isIced = false;
            initialFrozenBlock = false;
            if (effects != null) {
                effects.remove(Effects.FROZEN);
            }
        }
    }

    void applySlowOrFreeze(List<Effects> effects, boolean isBoss) {
        if (isBoss) {
            return;
        }
        if (!isIced) {
            isIced = true;
            effects.add(Effects.FROZEN);
            iceHp = 300;
        }
    }

    void setMirroredIceState(boolean iced, boolean initialFrozenBlock, double iceHp, List<Effects> effects) {
        this.isIced = iced;
        this.initialFrozenBlock = initialFrozenBlock;
        this.iceHp = iceHp;
        if (iced) {
            if (!effects.contains(Effects.FROZEN)) {
                effects.add(Effects.FROZEN);
            }
        } else {
            effects.remove(Effects.FROZEN);
        }
    }
}
