package com.workshop.view.gameplay;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.zombie.Zombie;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import pvz.libpvz.pam.PamPlayer;

public final class ProjectileAnimationLayer extends Group {

    private static final float SHOOTER_MUZZLE_X_RATIO = 0.20f;
    private static final float SHOOTER_MUZZLE_Y_RATIO = 0.18f;
    private static final float LOB_LAUNCH_X_RATIO = 0.50f;
    private static final float LOB_LAUNCH_Y_RATIO = 0.62f;
    private static final float LOB_APEX_Y_RATIO = 1.25f;
    private static final float LOB_LAND_Y_RATIO = 0.10f;

    private final GameContext gameContext;
    private final ProjectileAnimationResolver resolver;
    private final PamPlayer pamPlayer;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Map<Projectile, ProjectileActor> actors =
        new IdentityHashMap<>();

    public ProjectileAnimationLayer(
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
        this.resolver = new ProjectileAnimationResolver();
        this.pamPlayer = Textures.getPamPlayer();
    }

    @Override
    public void act(float delta) {
        syncProjectiles();
        super.act(delta);
    }

    private void syncProjectiles() {
        Set<Projectile> active = Collections.newSetFromMap(
            new IdentityHashMap<>()
        );

        for (Projectile projectile : gameContext.getProjectiles()) {
            if (projectile == null || !projectile.isActive()) {
                continue;
            }

            active.add(projectile);
            ProjectileActor actor = getOrCreateActor(projectile);
            if (actor == null) {
                continue;
            }

            updateActorPosition(actor, projectile);
            actor.setZIndex(Math.max(0, projectile.getRow()));
        }

        removeMissing(active);
    }

    private ProjectileActor getOrCreateActor(Projectile projectile) {
        ProjectileActor existing = actors.get(projectile);
        if (existing != null) {
            return existing;
        }

        ProjectileAnimationSpec spec = resolver.resolve(projectile);
        if (spec == null) {
            return null;
        }

        ProjectileActor actor = new ProjectileActor(
            projectile,
            spec,
            pamPlayer,
            getCellHeight()
        );

        actors.put(projectile, actor);
        addActor(actor);
        return actor;
    }

    private void updateActorPosition(
        ProjectileActor actor,
        Projectile projectile
    ) {
        ProjectileAnimationSpec spec = actor.getSpec();
        float drawX = getProjectileX(projectile);
        float drawY = getProjectileY(projectile);

        if (projectile.getTrajectory() == TrajectoryType.LOBBED) {
            drawX += lobOffsetX(projectile);
            drawY += lobOffsetY(projectile);
        } else if (projectile.getOwnerPlant() != null
            && projectile.getTrajectory() == TrajectoryType.STRAIGHT
            && !projectile.getOwnerPlant().isPeaFamily()) {
            drawX += getCellWidth() * SHOOTER_MUZZLE_X_RATIO;
            drawY += getCellHeight() * SHOOTER_MUZZLE_Y_RATIO;
        }

        drawX += spec.getOffsetX();
        drawY += spec.getOffsetY();
        actor.setPosition(drawX, drawY);
    }

    private float lobFlightT(Projectile projectile) {
        Plant owner = projectile.getOwnerPlant();
        float launchX = owner != null ? (float) owner.getX() : (float) projectile.getX();
        float traveled = (float) projectile.getX() - launchX;
        return MathUtils.clamp(traveled / estimateLobSpan(projectile, launchX), 0f, 1f);
    }

    private float lobOffsetX(Projectile projectile) {
        float t = lobFlightT(projectile);
        return getCellWidth() * LOB_LAUNCH_X_RATIO * (1f - t);
    }

    private float lobOffsetY(Projectile projectile) {
        float t = lobFlightT(projectile);
        float heightRatio = LOB_LAUNCH_Y_RATIO * (1f - t) * (1f - t)
            + LOB_APEX_Y_RATIO * 2f * t * (1f - t)
            + LOB_LAND_Y_RATIO * t * t;
        return getCellHeight() * heightRatio;
    }

    private float estimateLobSpan(Projectile projectile, float launchX) {
        float landingX = launchX + 4f;
        float nearest = Float.MAX_VALUE;

        for (Zombie zombie : gameContext.getAliveZombies()) {
            if (zombie == null || zombie.isDead() || zombie.getRow() != projectile.getRow()) {
                continue;
            }
            if (zombie.getX() < launchX) {
                continue;
            }

            float distance = (float) zombie.getX() - launchX;
            if (distance < nearest) {
                nearest = distance;
                landingX = (float) zombie.getX();
            }
        }

        return Math.max(1.2f, landingX - launchX);
    }

    private void removeMissing(Set<Projectile> active) {
        Iterator<Map.Entry<Projectile, ProjectileActor>> iterator =
            actors.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Projectile, ProjectileActor> entry = iterator.next();
            if (active.contains(entry.getKey())) {
                continue;
            }

            entry.getValue().remove();
            iterator.remove();
        }
    }

    private float getProjectileX(Projectile projectile) {
        return gridX + (float) projectile.getX() * getCellWidth();
    }

    private float getProjectileY(Projectile projectile) {
        return gridY
            + gridHeight
            - (float) projectile.getY() * getCellHeight()
            - getCellHeight() / 2f;
    }

    private float getCellWidth() {
        return gridWidth / gameContext.getLevel().getColumns();
    }

    private float getCellHeight() {
        return gridHeight / gameContext.getLevel().getRows();
    }
}
