package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.projectile.Projectile;

import pvz.libpvz.pam.PamPlayer;

public final class ProjectileActor extends Actor {

    private static final float TARGET_HEIGHT_TO_CELL_RATIO = 0.5f;

    private final Projectile projectile;
    private final ProjectileAnimationSpec spec;
    private final PamPlayer pamPlayer;
    private final float cellHeight;

    private Rectangle resolvedBounds;
    private Float resolvedScale;
    private float stateTime;

    public ProjectileActor(
        Projectile projectile,
        ProjectileAnimationSpec spec,
        PamPlayer pamPlayer,
        float cellHeight
    ) {
        this.projectile = projectile;
        this.spec = spec;
        this.pamPlayer = pamPlayer;
        this.cellHeight = cellHeight;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Rectangle bounds = getBounds();
        if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            return;
        }

        float scale = getScale(bounds);
        float localCenterX = bounds.x + bounds.width / 2f;
        float localCenterY = -(bounds.y + bounds.height / 2f);

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4(oldTransform);

        transform.translate(getX(), getY(), 0f);
        transform.scale(scale, scale, 1f);
        transform.translate(-localCenterX, -localCenterY, 0f);
        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(
                batch,
                spec.getPamPath(),
                spec.getClip(),
                stateTime,
                0f,
                0f,
                true
            );
        } finally {
            batch.setTransformMatrix(oldTransform);
        }
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public ProjectileAnimationSpec getSpec() {
        return spec;
    }

    private Rectangle getBounds() {
        if (resolvedBounds == null) {
            resolvedBounds = pamPlayer.bounds(
                spec.getPamPath(),
                spec.getClip()
            );
        }
        return resolvedBounds;
    }

    private float getScale(Rectangle bounds) {
        if (resolvedScale != null) {
            return resolvedScale;
        }

        float targetHeight = cellHeight * TARGET_HEIGHT_TO_CELL_RATIO;
        resolvedScale = targetHeight / bounds.height;
        resolvedScale *= spec.getScale();
        return resolvedScale;
    }
}
