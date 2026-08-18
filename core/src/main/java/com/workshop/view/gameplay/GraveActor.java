package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.season.Grave;

import pvz.libpvz.pam.PamPlayer;

public final class GraveActor extends Actor {

    private final Grave grave;
    private final PamPlayer pamPlayer;

    private float stateTime;

    public GraveActor(Grave grave, PamPlayer pamPlayer) {
        this.grave = grave;
        this.pamPlayer = pamPlayer;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        String pamPath = GraveAnimationResolver.getPamPath(grave.getType());
        String clip = GraveAnimationResolver.getClip(grave.getType());

        if (pamPath == null || clip == null) {
            return;
        }

        pamPlayer.draw(
            batch,
            pamPath,
            clip,
            stateTime,
            getX(),
            getY(),
            true
        );
    }

    public Grave getGrave() {
        return grave;
    }
}
