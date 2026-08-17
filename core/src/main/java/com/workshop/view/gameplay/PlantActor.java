package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.plants.Plant;

import pvz.libpvz.pam.PamPlayer;

public final class PlantActor extends Actor {

    private final Plant plant;
    private final PlantAnimationSpec animationSpec;
    private final PamPlayer pamPlayer;

    private static final String ICE_BLOCK_PAM =
        "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";

    private static final String CHILL_OVERLAY_PAM =
        "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM";

    // iceHp شروعش برای یخ‌زدگیِ کامل، ۶۰۰ است (رجوع کنید به
    // Plant.increaseFreezeLevel()).
    private static final double INITIAL_ICE_HP = 600.0;

    private PlantAnimationState currentState =
        PlantAnimationState.IDLE;

    private float stateTime;
    private float frostStateTime;

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

        if (!plant.isIced()) {
            stateTime += delta;
        }

        frostStateTime += delta;
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

        if (clip != null) {
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

        if (plant.isIced()) {

            pamPlayer.draw(
                batch,
                ICE_BLOCK_PAM,
                resolveIceBlockClip(plant.getIceHp()),
                frostStateTime,
                getX(),
                getY(),
                true
            );
        } else if (plant.getFreezeLevel() > 0) {
            // سرمازدگیِ جزئی (هنوز کامل یخ نزده): یه لایه‌ی یخِ سبک روش.
            String frostClip = plant.getFreezeLevel() >= 2
                ? "chill_stage2"
                : "chill_stage1";

            pamPlayer.draw(
                batch,
                CHILL_OVERLAY_PAM,
                frostClip,
                frostStateTime,
                getX(),
                getY(),
                true
            );
        }
    }


    private String resolveIceBlockClip(double iceHp) {
        float fraction = MathUtils.clamp(
            (float) (iceHp / INITIAL_ICE_HP),
            0f,
            1f
        );

        int bin = MathUtils.clamp((int) (fraction * 7f), 0, 6);

        return bin == 6 ? "freeze_idle" : "ice_block_damage" + bin;
    }

    public boolean play(PlantAnimationState state) {
        if (plant.isIced()) {
            return false;
        }

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
