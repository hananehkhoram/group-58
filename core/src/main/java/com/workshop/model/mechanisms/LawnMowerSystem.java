package com.workshop.model.mechanisms;

import com.workshop.model.GameContext;
import com.workshop.model.zombie.Zombie;

public final class LawnMowerSystem {

    private final GameContext ctx;
    private final LawnMower[] lawnMowers;

    public LawnMowerSystem(GameContext ctx, LawnMower[] lawnMowers) {
        this.ctx = ctx;
        this.lawnMowers = lawnMowers;
    }

    public void update(double deltaTime) {
        for (LawnMower l : lawnMowers) {
            if (!l.isActivated() || !l.isAvailable()) continue;

            for (Zombie z : getRowZombies(l.getRow())) {
                boolean aliveBefore = !z.isDead();
                l.trigger(z);
                if (aliveBefore && z.isDead()) {
                    ctx.getAliveZombies().remove(z);
                    ctx.incrementZombieKills();
                    ctx.recordLawnMowerKill();
                }
            }
            l.advance(deltaTime);

        }
    }

    public Zombie[] getRowZombies(int row) {
        return ctx.getAliveZombies().stream()
            .filter(z -> z.occupiesRow(row))
            .toArray(Zombie[]::new);
    }
}
