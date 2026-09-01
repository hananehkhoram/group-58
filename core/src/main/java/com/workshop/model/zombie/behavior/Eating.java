package com.workshop.model.zombie.behavior;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Effects;
import com.workshop.model.zombie.Zombie;

public class Eating implements Behaviors {

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (zombie.isBoss() || zombie.isStunned()) {
            zombie.setEating(false);
            return;
        }

        if (zombie.getEffect().contains(Effects.HYPNOTIZED)) {
            Zombie enemyZombie = findTargetZombie(zombie, ctx);
            if (enemyZombie != null) {
                boolean wasEating = zombie.isEating();
                zombie.setEating(true);
                if (!wasEating) {
                    zombie.resetEatClock(ctx);
                }
                int damage = zombie.consumeEatDamage(ctx);
                if (damage > 0) {
                    enemyZombie.takeDamage(damage);
                }
            } else {
                zombie.setEating(false);
            }
            return;
        }

        int row = zombie.getRow();
        int col = (int) zombie.getX();

        int totalRows = ctx.getPlantGrid().length;
        int totalCols = ctx.getPlantGrid()[0].length;
        if (row < 0 || row >= totalRows || col < 0 || col >= totalCols) {
            zombie.setEating(false);
            return;
        }

        Plant target = ctx.getPlantGrid()[row][col];
        if (target != null && target.getHp() > 0 && !target.isCatified()) {
            boolean wasEating = zombie.isEating();
            zombie.setEating(true);
            if (!wasEating) {
                zombie.resetEatClock(ctx);
            }
            int damage = zombie.consumeEatDamage(ctx);
            if (damage > 0) {
                target.takeDamage(damage);
            }
        } else {
            zombie.setEating(false);
        }
    }

    private Zombie findTargetZombie(Zombie hypnotizedZombie, GameContext ctx) {
        for (Zombie other : ctx.getAliveZombies()) {
            if (other == hypnotizedZombie || other.isDead() || other.getEffect().contains(Effects.HYPNOTIZED)) {
                continue;
            }
            if (other.occupiesRow(hypnotizedZombie.getRow())) {
                if (Math.abs(other.getX() - hypnotizedZombie.getX()) <= 0.4) {
                    return other;
                }
            }
        }
        return null;
    }
}
