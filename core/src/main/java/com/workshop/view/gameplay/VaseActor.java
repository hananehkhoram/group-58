package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.controller.repository.Textures;
import com.workshop.model.MiniGame.VaseGame.Vase;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

public final class VaseActor extends Actor {

    private final Vase vase;
    private final PamPlayer pamPlayer;

    private final String pamPath;
    private final String preferredClip;

    private String resolvedClip;
    private boolean clipResolved;

    private float stateTime;

    public VaseActor(
        Vase vase,
        PamPlayer pamPlayer,
        String pamPath,
        String preferredClip
    ) {
        this.vase = vase;
        this.pamPlayer = pamPlayer;
        this.pamPath = pamPath;
        this.preferredClip = preferredClip;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!vase.isBroken()) {
            stateTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        if (vase.isBroken()) {
            return;
        }

        if (!clipResolved) {
            resolveClip();
            clipResolved = true;
        }

        if (resolvedClip == null) {
            return;
        }

        Rectangle bounds =
            pamPlayer.bounds(
                pamPath,
                resolvedClip
            );

        if (bounds == null
            || bounds.width <= 0f
            || bounds.height <= 0f) {

            return;
        }

        float scaleX =
            getWidth() / bounds.width;

        float scaleY =
            getHeight() / bounds.height;

        float scale =
            Math.min(scaleX, scaleY);

        float centerX =
            getX() + getWidth() / 2f;

        float centerY =
            getY() + getHeight() / 2f;

        batch.flush();

        Matrix4 original =
            batch.getTransformMatrix().cpy();

        Matrix4 scaled =
            original.cpy()
                .translate(
                    centerX,
                    centerY,
                    0f
                )
                .scale(
                    scale,
                    scale,
                    1f
                )
                .translate(
                    -centerX,
                    -centerY,
                    0f
                );

        batch.setTransformMatrix(scaled);

        batch.setColor(
            1f,
            1f,
            1f,
            parentAlpha
        );

        pamPlayer.draw(
            batch,
            pamPath,
            resolvedClip,
            stateTime,
            centerX,
            centerY,
            true
        );

        batch.flush();

        batch.setTransformMatrix(original);

        batch.setColor(
            1f,
            1f,
            1f,
            parentAlpha
        );
    }

    private void resolveClip() {

        List<String> clips =
            pamPlayer.clips(pamPath);

        if (clips == null || clips.isEmpty()) {

            Gdx.app.error(
                "VaseActor",
                "No clips found: " + pamPath
            );

            return;
        }

        if (preferredClip != null
            && clips.contains(preferredClip)) {

            resolvedClip = preferredClip;
            return;
        }

        resolvedClip = clips.get(0);

        Gdx.app.log(
            "VaseActor",
            "Clip \""
                + preferredClip
                + "\" not found. Using \""
                + resolvedClip
                + "\". Available: "
                + clips
        );
    }

    public Vase getVase() {
        return vase;
    }
}
