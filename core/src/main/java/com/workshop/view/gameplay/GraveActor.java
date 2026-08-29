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
    private String lastClip;

    public GraveActor(Grave grave, PamPlayer pamPlayer, float cellHeight) {
        this.grave = grave;
        this.hitFlash = new HitFlashEffect(grave::getHp, 0.7f);
        this.pamPlayer = pamPlayer;
        this.cellHeight = cellHeight;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        hitFlash.update(delta);
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

        if (!clip.equals(lastClip)) {
            lastClip = clip;
            resolvedScale = null;
        }

        hitFlash.drawWithFlash(batch, parentAlpha, () -> drawGrave(batch, pamPath, clip));
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private void drawGrave(Batch batch, String pamPath, String clip) {
        float scale = getScale(pamPath, clip);

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4(oldTransform);
        transform.translate(getX(), getY(), 0);
        transform.scale(scale, scale, 1f);
        transform.translate(-getX(), -getY(), 0);
        batch.setTransformMatrix(transform);

        try {
            // مرحله‌های آسیب قبر ژست ثابت‌اند؛ لوپ باعث چشمک سریع می‌شود.
            pamPlayer.draw(
                batch,
                pamPath,
                clip,
                0f,
                getX(),
                getY(),
                false
            );
        } catch (Throwable ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    public Grave getGrave() {
        return grave;
    }
}
