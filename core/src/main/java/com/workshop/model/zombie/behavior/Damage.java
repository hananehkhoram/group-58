package com.workshop.model.zombie.behavior;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Effects;
import com.workshop.model.zombie.Zombie;

import java.util.List;

public class Damage implements Behaviors {

    private final List<TargetType> targetTypes;
    private final double afterKillSpeedMultiplier;
    private boolean hasKilledOnce = false;

    /** Gargantuar **/
    public Damage() {
        this(List.of(TargetType.PLANT, TargetType.HYPNOTIZED_ZOMBIE), 1.0);
    }

    /** All-Star */
    public Damage(List<TargetType> targetTypes, double afterKillSpeedMultiplier) {
        this.targetTypes = targetTypes;
        this.afterKillSpeedMultiplier = afterKillSpeedMultiplier;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int col = (int) zombie.getX();

        int totalRows = ctx.getPlantGrid().length;
        int totalCols = ctx.getPlantGrid()[0].length;
        if (row < 0 || row >= totalRows || col < 0 || col >= totalCols) return;

        if (targetTypes.contains(TargetType.PLANT)) {
            Plant target = ctx.getPlantGrid()[row][col];
            if (target != null && !target.isDead()) {
                target.takeDamage(Integer.MAX_VALUE);
                if ("Gargantuar".equalsIgnoreCase(zombie.getName())
                    || "ZombieGargantuar".equals(zombie.getId())) {
                    ctx.shakeScreen(18f, 0.45f);
                } else {
                    ctx.shakeScreen(8f, 0.22f);
                }
                onKill(zombie);
                return;
            }
        }

        if (targetTypes.contains(TargetType.HYPNOTIZED_ZOMBIE)) {
            for (Zombie otherZombie : ctx.getAliveZombies()) {
                if (otherZombie == zombie || otherZombie.getRow() != row) continue;

                if (otherZombie.searchEffect(Effects.HYPNOTIZED) && Math.abs(otherZombie.getX() -
                        zombie.getX()) < 0.8) {
                    otherZombie.takeDamage(Integer.MAX_VALUE);
                    onKill(zombie);
                    return;
                }
            }
        }
    }

    private void onKill(Zombie zombie) {
        if (!hasKilledOnce && afterKillSpeedMultiplier != 1.0) {
            zombie.setSpeed(zombie.getSpeed() * afterKillSpeedMultiplier);
            hasKilledOnce = true;
        }
    }

    public enum TargetType {
        PLANT, HYPNOTIZED_ZOMBIE, EXPLORER
    }
}
