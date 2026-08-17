package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.zombie.Zombie;

import pvz.libpvz.pam.PamPlayer;

public final class ZombieActor extends Actor {

    private final Zombie zombie;
    private final ZombieAnimationSpec animationSpec;
    private final PamPlayer pamPlayer;

    private static final String INITIAL_FROZEN_BLOCK_PAM =
        "768/INITIAL/EFFECTS/ICEBLOOM_ICE_BLOCK_ZOMBIE/ICEBLOOM_ICE_BLOCK_ZOMBIE.PAM";


    private static final String SANDSTORM_TOP_PAM =
        "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";
    private static final String SANDSTORM_TOP_CLIP = "idle";
    private static final float SANDSTORM_EFFECT_DURATION = 1.2f;

    private float sandstormEffectTime;

    private ZombieAnimationState currentState =
        ZombieAnimationState.IDLE;

    private float stateTime;

    public ZombieActor(
        Zombie zombie,
        ZombieAnimationSpec animationSpec,
        PamPlayer pamPlayer
    ) {
        this.zombie = zombie;
        this.animationSpec = animationSpec;
        this.pamPlayer = pamPlayer;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (zombie.isEnteredViaSandstorm()) {
            sandstormEffectTime += delta;
            if (sandstormEffectTime >= SANDSTORM_EFFECT_DURATION) {
                zombie.setEnteredViaSandstorm(false);
            }
        }

        if (zombie.isInitialFrozenBlock()) {
            return;
        }

        updateAnimationState();
        stateTime += delta;
    }

    private void updateAnimationState() {
        ZombieAnimationState nextState;

        if (zombie.isEating()) {
            nextState = ZombieAnimationState.EAT;
        } else {
            nextState = ZombieAnimationState.WALK;
        }

        if (!animationSpec.hasClip(nextState)) {
            nextState = ZombieAnimationState.IDLE;
        }

        if (currentState != nextState) {
            currentState = nextState;
            stateTime = 0f;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (zombie.isDead()) {
            return;
        }

        if (zombie.isInitialFrozenBlock()) {
            pamPlayer.draw(
                batch,
                INITIAL_FROZEN_BLOCK_PAM,
                "idle",
                stateTime,
                getX() + 33f,
                getY(),
                true
            );

            return;
        }

        String clip =
            animationSpec.getClip(currentState);

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

        if (zombie.isEnteredViaSandstorm()) {
            pamPlayer.draw(
                batch,
                SANDSTORM_TOP_PAM,
                SANDSTORM_TOP_CLIP,
                sandstormEffectTime,
                getX(),
                getY(),
                false
            );
        }
    }

    public ZombieAnimationState getCurrentState() {
        return currentState;
    }

    public Zombie getZombie() {
        return zombie;
    }
}
