package com.workshop.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.zombie.Zombie;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import pvz.libpvz.pam.PamPlayer;

public final class ZombieAnimationLayer extends Group {

    private final GameContext gameContext;
    private final ZombieAnimationResolver resolver;
    private final PamPlayer pamPlayer;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Map<Zombie, ZombieActor> zombieActors =
        new IdentityHashMap<>();

    public ZombieAnimationLayer(
        GameContext gameContext,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;

        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        resolver = new ZombieAnimationResolver();
        pamPlayer = Textures.getPamPlayer();
    }

    @Override
    public void act(float delta) {
        syncZombieActors();
        super.act(delta);
    }

    private void syncZombieActors() {
        Set<Zombie> activeZombies =
            Collections.newSetFromMap(
                new IdentityHashMap<>()
            );

        for (Zombie zombie : gameContext.getAliveZombies()) {

            if (zombie == null) {
                continue;
            }

            if (zombie.isDead()
                && !((zombie.isAshed() && !zombie.isAshFinished())
                || (!zombie.isAshed() && !zombie.isDeathAnimFinished()))) {
                continue;
            }

            activeZombies.add(zombie);

            ZombieActor actor =
                zombieActors.get(zombie);

            if (actor == null) {
                actor = createZombieActor(zombie);

                if (actor == null) {
                    continue;
                }

                zombieActors.put(zombie, actor);
                addActor(actor);
            }

            actor.setPosition(
                getZombieX(zombie),
                getRowCenterY(zombie.getRow()) + 10f
            );
            actor.setZIndex(zombie.getRow());
        }

        removeMissingZombies(activeZombies);
    }

    private ZombieActor createZombieActor(
        Zombie zombie
    ) {
        ZombieAnimationSpec spec =
            resolver.resolve(
                zombie,
                gameContext.getSeason().getName()
            );

        if (spec == null) {
            return null;
        }

        return new ZombieActor(
            zombie,
            spec,
            resolver,
            gameContext.getSeason().getName(),
            pamPlayer,
            getCellHeight()
        );
    }

    private void removeMissingZombies(
        Set<Zombie> activeZombies
    ) {
        Iterator<Map.Entry<Zombie, ZombieActor>>
            iterator =
            zombieActors.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Zombie, ZombieActor> entry =
                iterator.next();

            if (activeZombies.contains(entry.getKey())) {
                continue;
            }

            entry.getValue().remove();
            iterator.remove();

        }
    }

    private float getZombieX(Zombie zombie) {
        float cellWidth =
            gridWidth
                / gameContext.getLevel().getColumns();

        return gridX
            + (float) zombie.getX() * cellWidth;
    }

    private float getCellHeight() {
        return gridHeight / gameContext.getLevel().getRows();
    }

    private float getRowCenterY(int row) {
        float cellHeight =
            gridHeight
                / gameContext.getLevel().getRows();

        return gridY
            + gridHeight
            - row * cellHeight
            - cellHeight / 2f
            + cellHeight * 0.16f;
    }
}
