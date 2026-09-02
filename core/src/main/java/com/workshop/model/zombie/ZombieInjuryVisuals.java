package com.workshop.model.zombie;

import com.workshop.model.zombie.behavior.ArmorType;

import java.util.ArrayDeque;

/**
 * Arm / head / ash / armor-pop visuals for death and injury.
 */
final class ZombieInjuryVisuals {

    private boolean ashed;
    private boolean ashFinished;
    private boolean lostArm;
    private boolean lostHead;
    private boolean deathAnimFinished;
    private boolean pendingArmDrop;
    private boolean pendingHeadDrop;
    private final ArrayDeque<ArmorType> pendingArmorPops = new ArrayDeque<>();

    void noteBodyInjury(int hp, int maxHp) {
        int effectiveMax = maxHp;
        if (effectiveMax <= 0) {
            effectiveMax = Math.max(hp, 1);
        }
        if (!lostArm && hp > 0 && hp <= effectiveMax / 2) {
            lostArm = true;
            pendingArmDrop = true;
        }
        if (!lostHead && hp <= 0 && !ashed) {
            lostHead = true;
            pendingHeadDrop = true;
        }
    }

    void onExplosionKill() {
        ashed = true;
        ashFinished = false;
        pendingHeadDrop = false;
        pendingArmDrop = false;
    }

    void queueArmorPop(ArmorType type) {
        pendingArmorPops.addLast(type);
    }

    ArmorType pollArmorPop() {
        return pendingArmorPops.pollFirst();
    }

    boolean consumeArmDrop() {
        if (!pendingArmDrop) {
            return false;
        }
        pendingArmDrop = false;
        return true;
    }

    boolean consumeHeadDrop() {
        if (!pendingHeadDrop) {
            return false;
        }
        pendingHeadDrop = false;
        return true;
    }

    boolean hasLostArm() {
        return lostArm;
    }

    boolean isAshed() {
        return ashed;
    }

    boolean isAshFinished() {
        return ashFinished;
    }

    void markAshFinished() {
        ashFinished = true;
    }

    boolean isDeathAnimFinished() {
        return deathAnimFinished;
    }

    void markDeathAnimFinished() {
        deathAnimFinished = true;
    }

    void setMirroredDeathState(boolean ashed, boolean ashFinished, boolean deathAnimFinished) {
        this.ashed = ashed;
        this.ashFinished = ashFinished;
        this.deathAnimFinished = deathAnimFinished;
    }
}
