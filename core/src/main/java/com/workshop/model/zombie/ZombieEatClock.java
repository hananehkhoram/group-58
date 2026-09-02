package com.workshop.model.zombie;

import com.workshop.model.GameContext;

/**
 * Fractional eat-DPS accumulator previously inlined in {@link Zombie}.
 */
final class ZombieEatClock {

    private static final int TICKS_PER_SECOND = 10;

    private double eatDamageAccumulator;
    private long lastEatTick = -1;

    void reset(GameContext ctx) {
        lastEatTick = ctx.getTimeManager().getTotalTicks();
        eatDamageAccumulator = 0;
    }

    int consume(GameContext ctx, double eatDps) {
        long now = ctx.getTimeManager().getTotalTicks();
        if (lastEatTick < 0) {
            lastEatTick = now;
        }
        long elapsedTicks = now - lastEatTick;
        lastEatTick = now;

        eatDamageAccumulator += eatDps * (elapsedTicks / (double) TICKS_PER_SECOND);
        int wholeDamage = (int) eatDamageAccumulator;
        eatDamageAccumulator -= wholeDamage;
        return wholeDamage;
    }
}
