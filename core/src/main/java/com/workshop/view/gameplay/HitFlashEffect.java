package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;

import java.util.function.IntSupplier;

public final class HitFlashEffect {

    private static final float FLASH_DURATION = 0.12f;
    private static final float DEFAULT_FLASH_REST = 0.35f;
    private static final float MAX_FLASH_INTENSITY = 0.75f;

    private final IntSupplier hpSupplier;
    private final float cycle;
    private int lastHp = Integer.MIN_VALUE;
    private float cycleTime;

    public HitFlashEffect(IntSupplier hpSupplier) {
        this(hpSupplier, DEFAULT_FLASH_REST);
    }

    public HitFlashEffect(IntSupplier hpSupplier, float flashRest) {
        this.hpSupplier = hpSupplier;
        this.cycle = FLASH_DURATION + Math.max(0.05f, flashRest);
        this.cycleTime = this.cycle;
    }

    public void update(float delta) {
        int currentHp = hpSupplier.getAsInt();

        if (lastHp == Integer.MIN_VALUE) {
            lastHp = currentHp;
        } else if (currentHp < lastHp && cycleTime >= cycle) {
            cycleTime = 0f;
        }

        lastHp = currentHp;

        if (cycleTime < cycle) {
            cycleTime += delta;
        }
    }

    public float getIntensity() {
        if (cycleTime >= FLASH_DURATION) {
            return 0f;
        }

        float progress = MathUtils.clamp(cycleTime / FLASH_DURATION, 0f, 1f);
        return (1f - progress) * MAX_FLASH_INTENSITY;
    }

    public void drawWithFlash(Batch batch, float parentAlpha, Runnable drawEntity) {
        batch.setColor(1f, 1f, 1f, parentAlpha);
        drawEntity.run();

        float flash = getIntensity();
        if (flash <= 0.01f) {
            return;
        }

        batch.flush();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.setColor(1f, 1f, 1f, flash * parentAlpha);
        drawEntity.run();
        batch.flush();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setColor(Color.WHITE);
    }
}
