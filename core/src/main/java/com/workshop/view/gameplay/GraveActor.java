package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.season.Grave;

import pvz.libpvz.pam.PamPlayer;

public final class GraveActor extends Actor {

    private final Grave grave;
    private final PamPlayer pamPlayer;
    private final HitFlashEffect hitFlash;
    private final float cellHeight;

    private static final float TARGET_HEIGHT_TO_CELL_RATIO = 0.95f;

    private Float resolvedScale;

    private float stateTime;

    public GraveActor(Grave grave, PamPlayer pamPlayer, float cellHeight) {
        this.grave = grave;
        this.hitFlash =
            new HitFlashEffect(grave::getHp);
        this.pamPlayer = pamPlayer;
        this.cellHeight = cellHeight;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        hitFlash.update(delta);
        stateTime += delta;
    }

    private float getScale(String pamPath, String clip) {
        if (resolvedScale != null) {
            return resolvedScale;
        }

        Rectangle bounds = pamPlayer.bounds(pamPath, clip);

        if (bounds == null || bounds.height <= 0f) {
            return 1f;
        }

        resolvedScale =
            (cellHeight * TARGET_HEIGHT_TO_CELL_RATIO) / bounds.height;

        return resolvedScale;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        String pamPath = GraveAnimationResolver.getPamPath(grave.getType());
        String clip = GraveAnimationResolver.getClip(grave.getHp(), grave.getMaxHp());

        if (pamPath == null || clip == null) {
            return;
        }
        float flash = hitFlash.getIntensity();
        batch.setColor(1f + flash, 1f + flash, 1f + flash, parentAlpha);

        float scale = getScale(pamPath, clip);

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4(oldTransform);
        transform.translate(getX(), getY(), 0);
        transform.scale(scale, scale, 1f);
        transform.translate(-getX(), -getY(), 0);
        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(
                batch,
                pamPath,
                clip,
                stateTime,
                getX(),
                getY(),
                true
            );
        } catch (Throwable ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    public Grave getGrave() {
        return grave;
    }
}
