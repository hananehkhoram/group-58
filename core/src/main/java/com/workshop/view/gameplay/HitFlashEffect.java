package com.workshop.view.gameplay;

import com.badlogic.gdx.math.MathUtils;

import java.util.function.IntSupplier;


public final class HitFlashEffect {

    private static final float FLASH_DURATION = 0.15f;
    private static final float MAX_FLASH_INTENSITY = 0.9f;

    private final IntSupplier hpSupplier;
    private int lastHp = Integer.MIN_VALUE;
    private float flashTime = FLASH_DURATION;

    public HitFlashEffect(IntSupplier hpSupplier) {
        this.hpSupplier = hpSupplier;
    }

    public void update(float delta) {
        int currentHp = hpSupplier.getAsInt();

        if (lastHp == Integer.MIN_VALUE) {
            lastHp = currentHp;
        } else if (currentHp < lastHp) {
            flashTime = 0f;
        }

        lastHp = currentHp;

        if (flashTime < FLASH_DURATION) {
            flashTime += delta;
        }
    }


    public float getIntensity() {
        float progress = MathUtils.clamp(flashTime / FLASH_DURATION, 0f, 1f);
        return (1f - progress) * MAX_FLASH_INTENSITY;
    }
}
