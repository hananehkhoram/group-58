package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.controller.repository.Textures;
import com.workshop.model.projectile.BowlingWallnut;
import com.workshop.model.projectile.Projectile;

import pvz.libpvz.pam.PamPlayer;

public final class ProjectileActor extends Actor {

    private static final float TARGET_HEIGHT_TO_CELL_RATIO = 0.5f;
    private static final float SPRITE_HEIGHT_TO_CELL_RATIO = 0.32f;
    private static final float BOWLING_HEIGHT_TO_CELL_RATIO = 1.1f;
    private static final float FROZEN_PART_TIME = 0.22f;

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
        if (drawSprite(batch)) {
            return;
        }

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
            float time = spec.isFreezeFrame() ? FROZEN_PART_TIME : stateTime;
            if (spec.getPart() != null) {
                pamPlayer.drawPart(
                    batch,
                    spec.getPamPath(),
                    spec.getClip(),
                    time,
                    0f,
                    0f,
                    spec.getPart()
                );
            } else {
                pamPlayer.draw(
                    batch,
                    spec.getPamPath(),
                    spec.getClip(),
                    time,
                    0f,
                    0f,
                    true
                );
            }
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

    private boolean drawSprite(Batch batch) {
        String imageId = spec.getImageResourceId();
        if (imageId == null) {
            return false;
        }

        TextureRegion region = Textures.regionOrNull(imageId);
        if (region == null) {
            return false;
        }

        float size = cellHeight * spriteHeightRatio() * spec.getScale();
        float x = getX() - size / 2f;
        float y = getY() - size / 2f;
        batch.draw(region, x, y, size, size);
        return true;
    }

    private Rectangle getBounds() {
        if (resolvedBounds == null) {
            if (spec.getPamPath() == null) {
                return null;
            }
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

        float targetHeight = cellHeight * pamHeightRatio();
        resolvedScale = targetHeight / bounds.height;
        resolvedScale *= spec.getScale();
        return resolvedScale;
    }

    private float pamHeightRatio() {
        return isBowlingPlant()
            ? BOWLING_HEIGHT_TO_CELL_RATIO
            : TARGET_HEIGHT_TO_CELL_RATIO;
    }

    private float spriteHeightRatio() {
        return isBowlingPlant()
            ? BOWLING_HEIGHT_TO_CELL_RATIO
            : SPRITE_HEIGHT_TO_CELL_RATIO;
    }

    private boolean isBowlingPlant() {
        return projectile instanceof BowlingWallnut;
    }
}
