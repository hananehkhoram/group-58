package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

import pvz.libpvz.pam.PamPlayer;

public final class ZombiePreviewActor extends Actor {

    private final ZombieAnimationSpec animationSpec;
    private final PamPlayer pamPlayer;

    private float stateTime;

    public ZombiePreviewActor(
        ZombieAnimationSpec animationSpec,
        PamPlayer pamPlayer
    ) {
        this.animationSpec = animationSpec;
        this.pamPlayer = pamPlayer;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        String idleClip = animationSpec.getIdleClip();

        if (idleClip == null) {
            return;
        }

        pamPlayer.draw(
            batch,
            animationSpec.getPamPath(),
            idleClip,
            stateTime,
            getX(),
            getY(),
            true
        );
    }

}
