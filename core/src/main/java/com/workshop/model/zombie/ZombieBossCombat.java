package com.workshop.model.zombie;

import com.workshop.view.Console;

/**
 * Boss multi-phase HP / stun damage rules.
 */
final class ZombieBossCombat {

    private static final double BOSS_STUN_SECONDS = 8.0;

    private ZombieBossCombat() {}

    static void applyBodyDamage(Zombie zombie, int remaining, ZombieStatusEffects status) {
        if (remaining <= 0) {
            return;
        }
        int cap = zombie.getMaxHp();
        int phase = zombie.healthPhase();
        if (phase <= 0) {
            zombie.setHp(zombie.getHp() - remaining);
            return;
        }

        if (status.isStunned()) {
            zombie.setHp(Math.max(phaseFloorHp(zombie, phase), zombie.getHp() - remaining));
            return;
        }

        int newHp = zombie.getHp() - remaining;
        int newPhase = phaseForHp(newHp, cap);
        if (newPhase < phase) {
            int droppedTo = phase - 1;
            if (droppedTo <= 0) {
                zombie.setHp(newHp);
                return;
            }
            zombie.setHp(phaseCapHp(zombie, droppedTo));
            status.applyStun(BOSS_STUN_SECONDS, zombie);
            Console.showMessage(zombie.getName() + " is stunned!");
            return;
        }
        zombie.setHp(newHp);
    }

    static int phaseForHp(int current, int cap) {
        if (current <= 0 || cap <= 0) {
            return 0;
        }
        return (int) Math.ceil(current * 3.0 / cap);
    }

    static int phaseCapHp(Zombie zombie, int phase) {
        return Math.max(1, (int) Math.floor(zombie.getMaxHp() * phase / 3.0));
    }

    static int phaseFloorHp(Zombie zombie, int phase) {
        if (phase <= 1) {
            return 1;
        }
        return phaseCapHp(zombie, phase - 1) + 1;
    }
}
