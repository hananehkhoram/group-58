package com.workshop.model.mechanisms;

import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.zombie.Effects;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.Behaviors;

import java.util.ArrayList;
import java.util.List;

public final class ZombieSystem {

    private static final double LOSS_X = 0.0;

    private final GameContext ctx;
    private final LawnMower[] lawnMowers;

    public ZombieSystem(GameContext ctx, LawnMower[] lawnMowers) {
        this.ctx = ctx;
        this.lawnMowers = lawnMowers;
    }

    public void update(double deltaTime) {
        List<Zombie> deathsThisTick =
            new ArrayList<>();

        List<Zombie> zombiesSnapshot =
            new ArrayList<>(
                ctx.getAliveZombies()
            );

        IZombieManager iZombieManager =
            getIZombieManager();

        for (Zombie zombie : zombiesSnapshot) {

            if (!ctx.getAliveZombies().contains(zombie)) {
                continue;
            }

            zombie.update(
                ctx,
                deltaTime
            );

            despawnIfWalkedOffLawn(zombie);
            if (!ctx.getAliveZombies().contains(zombie)) {
                continue;
            }

            if (iZombieManager != null
                && !zombie.isDead()
                && !zombie.isMovingBackward()
                && !zombie.getEffect().contains(Effects.HYPNOTIZED)
                && zombie.getX() <= LOSS_X) {

                boolean brainWasEaten =
                    iZombieManager.attackBrain(
                        zombie,
                        ctx
                    );

                if (brainWasEaten) {
                    killAllZombiesInRow(
                        zombie.getRow()
                    );
                }

                continue;
            }

            if (iZombieManager == null
                && !zombie.isBoss()
                && !zombie.isMovingBackward()
                && zombie.getX() <= LOSS_X) {

                LawnMower mower =
                    lawnMowers[
                        (int) zombie.getY()
                        ];

                if (!mower.isAvailable()) {
                    ctx.triggerPlayerLoss();
                    return;
                }

                if (!mower.isActivated()) {
                    mower.activate();

                    ctx.playSound(
                        "sfx:music/lawnmower"
                    );
                }
            }

            if (zombie.isDead()) {
                if (zombie.isAshed() && !zombie.isAshFinished()) {
                    continue;
                }
                if (!zombie.isAshed() && !zombie.isDeathAnimFinished()) {
                    continue;
                }

                for (
                    Behaviors behavior
                    : zombie.getBehaviors().values()
                ) {
                    behavior.onDeath(
                        zombie,
                        ctx
                    );
                }

                LootItem.tryDropLoot(
                    ctx,
                    (int) Math.floor(
                        zombie.getX()
                    ),
                    zombie.getRow()
                );

                ctx.getAliveZombies()
                    .remove(zombie);

                ctx.incrementZombieKills();

                deathsThisTick.add(
                    zombie
                );

                ctx.recordZombieKillTick();

                if (iZombieManager == null) {

                    boolean noMowerLeftInRow =
                        !lawnMowers[
                            (int) zombie.getY()
                            ].isAvailable();

                    if (noMowerLeftInRow
                        && Math.floor(
                        zombie.getX()
                    ) == 0) {

                        ctx.recordAlmostLostKill();
                    }
                }
            }
        }

        com.workshop.controller.ScoringManager
            .onZombiesDied(
                ctx,
                deathsThisTick
            );
    }

    public void killAllZombiesInRow(int row) {
        List<Zombie> zombies =
            new ArrayList<>(
                ctx.getAliveZombies()
            );

        for (Zombie zombie : zombies) {

            if (!zombie.occupiesRow(row)) {
                continue;
            }

            if (zombie.isDead()) {
                continue;
            }

            zombie.setEating(false);

            zombie.takeArmorPiercingDamage(
                zombie.getHp() + 1
            );
        }
    }

    private void despawnIfWalkedOffLawn(Zombie zombie) {
        if (zombie == null || zombie.isDead()) {
            return;
        }
        double rightEdge = ctx.getLevel().getColumns() + 0.35;
        if (zombie.isMovingBackward() && zombie.getX() >= rightEdge) {
            zombie.takeArmorPiercingDamage(Math.max(1, zombie.getHp()));
        }
    }

    private IZombieManager getIZombieManager() {
        if (ctx.getLevelManager() instanceof IZombieManager manager) {
            return manager;
        }
        return null;
    }
}
