package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import pvz.libpvz.pam.PamPlayer;

public final class ZombiePlacementPreviewActor extends Actor {

    private static final float TARGET_HEIGHT_TO_CELL_RATIO = 1.6f;

    private final ZombieAnimationSpec animationSpec;
    private final PamPlayer pamPlayer;
    private final float cellHeight;

    private float stateTime;
    private Float resolvedScale;

    public ZombiePlacementPreviewActor(
        ZombieAnimationSpec animationSpec,
        PamPlayer pamPlayer,
        float cellHeight
    ) {
        this.animationSpec = animationSpec;
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
        String clip = animationSpec.getIdleClip();
        if (clip == null) {
            return;
        }

        float x = getX();
        float y = getY();
        float scale = getScale();

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4(oldTransform);
        transform.translate(x, y, 0f);
        transform.scale(scale, scale, 1f);
        transform.translate(-x, -y, 0f);
        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(
                batch,
                animationSpec.getPamPath(),
                clip,
                stateTime,
                x,
                y,
                true
            );
        } catch (Throwable ignored) {
        } finally {
            batch.setTransformMatrix(oldTransform);
        }
    }

    private float getScale() {
        if (resolvedScale != null) {
            return resolvedScale;
        }

        String clip = animationSpec.getIdleClip();
        Rectangle bounds;

        try {
            bounds = pamPlayer.bounds(
                animationSpec.getPamPath(),
                clip
            );
        } catch (Throwable ignored) {
            resolvedScale = 1f;
            return resolvedScale;
        }

        if (bounds == null || bounds.height <= 0f) {
            resolvedScale = 1f;
            return resolvedScale;
        }

        resolvedScale =
            (cellHeight * TARGET_HEIGHT_TO_CELL_RATIO)
                / bounds.height;

        return resolvedScale;
    }
}
