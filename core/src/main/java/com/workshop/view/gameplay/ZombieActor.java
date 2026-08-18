package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.zombie.Zombie;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

public final class ZombieActor extends Actor {

    private final Zombie zombie;
    private final ZombieAnimationSpec animationSpec;
    private final PamPlayer pamPlayer;
    private static final double DANGER_ZONE_X = 1.5;
    private static final float MAX_DANGER_TINT = 0.65f;

    private final HitFlashEffect hitFlash;

    private static final String ICE_BLOCK_PAM =
        "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM";
    private static final String ICE_BLOCK_PREFERRED_CLIP = "idle";

    // iceHp شروعش برای بلوک یخِ ابتدایی، ۶۰۰ است (رجوع کنید به
    // Zombie.setAsInitialFrozenBlock()).
    private static final double INITIAL_ICE_HP = 600.0;

    private String resolvedSandstormClip;
    private boolean sandstormClipResolved;
    private static final String SANDSTORM_TOP_PAM =
        "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";
    private static final float SANDSTORM_EFFECT_DURATION = 1.2f;

    private float sandstormEffectTime;
    private static final float MIN_ICE_ALPHA = 0.25f;

    private String resolvedIceBlockClip;
    private boolean iceBlockClipResolved;

    private ZombieAnimationState currentState =
        ZombieAnimationState.IDLE;

    private float stateTime;

    public ZombieActor(
        Zombie zombie,
        ZombieAnimationSpec animationSpec,
        PamPlayer pamPlayer
    ) {
        this.zombie = zombie;
        this.hitFlash =
            new HitFlashEffect(zombie::getHp);
        this.animationSpec = animationSpec;
        this.pamPlayer = pamPlayer;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        hitFlash.update(delta);

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

    private void resolveSandstormClip() {
        List<String> clips = pamPlayer.clips(SANDSTORM_TOP_PAM);

        if (clips == null || clips.isEmpty()) {
            Gdx.app.error(
                "ZombieActor",
                "No clips found for PAM: " + SANDSTORM_TOP_PAM
            );
            return;
        }

        if (clips.contains("idle")) {
            resolvedSandstormClip = "idle";
        } else if (clips.contains("animation")) {
            resolvedSandstormClip = "animation";
        } else {
            resolvedSandstormClip = clips.get(0);
        }

        Gdx.app.log(
            "ZombieActor",
            "Sandstorm clips: " + clips
                + " | selected: " + resolvedSandstormClip
        );
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (zombie.isDead()) {
            return;
        }

        if (zombie.isInitialFrozenBlock()) {
            drawIceBlock(batch, parentAlpha);
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


        float dangerTint = resolveDangerTint(zombie.getX());
        float flash = hitFlash.getIntensity();

        batch.setColor(
            1f + flash,
            1f - dangerTint + flash,
            1f - dangerTint + flash,
            parentAlpha
        );

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
            if (!sandstormClipResolved) {
                resolveSandstormClip();
                sandstormClipResolved = true;
            }

            if (resolvedSandstormClip != null) {
                pamPlayer.draw(
                    batch,
                    SANDSTORM_TOP_PAM,
                    resolvedSandstormClip,
                    sandstormEffectTime,
                    getX(),
                    getY(),
                    false
                );
            }
        }
    }
    private float resolveDangerTint(double zombieX) {
        if (zombieX >= DANGER_ZONE_X) {
            return 0f;
        }

        float proximity = MathUtils.clamp(
            (float) (1.0 - zombieX / DANGER_ZONE_X),
            0f,
            1f
        );

        return proximity * MAX_DANGER_TINT;
    }

    private void drawIceBlock(Batch batch, float parentAlpha) {
        if (!iceBlockClipResolved) {
            resolveIceBlockClip();
            iceBlockClipResolved = true;
        }

        if (resolvedIceBlockClip == null) {
            return;
        }

        float alpha = resolveIceBlockAlpha(zombie.getIceHp()) * parentAlpha;

        batch.setColor(1f, 1f, 1f, alpha);

        pamPlayer.draw(
            batch,
            ICE_BLOCK_PAM,
            resolvedIceBlockClip,
            stateTime,
            getX() + 33f,
            getY(),
            true
        );

        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private void resolveIceBlockClip() {
        List<String> clips = pamPlayer.clips(ICE_BLOCK_PAM);

        if (clips == null || clips.isEmpty()) {
            Gdx.app.error(
                "ZombieActor",
                "No clips found for PAM: " + ICE_BLOCK_PAM
            );
            return;
        }

        if (clips.contains(ICE_BLOCK_PREFERRED_CLIP)) {
            resolvedIceBlockClip = ICE_BLOCK_PREFERRED_CLIP;
        } else {
            Gdx.app.log(
                "ZombieActor",
                "Clip \"" + ICE_BLOCK_PREFERRED_CLIP + "\" not found in "
                    + ICE_BLOCK_PAM + ", falling back to \"" + clips.get(0)
                    + "\". Available: " + clips
            );
            resolvedIceBlockClip = clips.get(0);
        }
    }

    /**
     * شفافیتِ بلوک رو متناسب با آسیبِ باقی‌مونده حساب می‌کنه: سالم
     * (iceHp کامل) کاملاً توپر، نزدیک شکستن (iceHp نزدیک صفر) خیلی
     * کم‌رنگ (ولی نه کاملاً محو، تا معلوم باشه هنوز از بین نرفته).
     */
    private float resolveIceBlockAlpha(double iceHp) {
        float fraction = MathUtils.clamp(
            (float) (iceHp / INITIAL_ICE_HP),
            0f,
            1f
        );

        return MIN_ICE_ALPHA + fraction * (1f - MIN_ICE_ALPHA);
    }

    public ZombieAnimationState getCurrentState() {
        return currentState;
    }

    public Zombie getZombie() {
        return zombie;
    }
}
