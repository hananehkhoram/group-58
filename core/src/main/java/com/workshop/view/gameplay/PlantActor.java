package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.plants.Plant;

import pvz.libpvz.pam.PamPlayer;

public final class PlantActor extends Actor {

    private final Plant plant;
    private final PlantAnimationSpec animationSpec;
    private final PamPlayer pamPlayer;

    private PlantAnimationState currentState =
        PlantAnimationState.IDLE;

    private float stateTime;

    public PlantActor(
        Plant plant,
        PlantAnimationSpec animationSpec,
        PamPlayer pamPlayer
    ) {
        this.plant = plant;
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
        if (plant.isDead()) {
            return;
        }

        String clip = animationSpec.getClip(currentState);

        if (clip == null) {
            clip = animationSpec.getIdleClip();
        }

        if (clip == null) {
            return;
        }

        pamPlayer.draw(
            batch,
            animationSpec.getPamPath(),
            clip,
            stateTime,
            getX(),
            getY(),
            true
        );
    }

    public boolean play(PlantAnimationState state) {
        if (!animationSpec.hasClip(state)) {
            return false;
        }

        if (currentState != state) {
            currentState = state;
            stateTime = 0f;
        }

        return true;
    }

    public void playIdle() {
        currentState = PlantAnimationState.IDLE;
        stateTime = 0f;
    }

    public PlantAnimationState getCurrentState() {
        return currentState;
    }

    public Plant getPlant() {
        return plant;
    }
}
