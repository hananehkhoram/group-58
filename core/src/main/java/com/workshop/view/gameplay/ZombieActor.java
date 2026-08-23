package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.zombie.Zombie;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

public final class ZombieActor extends Actor {

    private final Zombie zombie;
    private ZombieAnimationSpec animationSpec;
    private final ZombieAnimationResolver resolver;
    private final String seasonName;
    private final PamPlayer pamPlayer;
    private final float cellHeight;
    private static final double DANGER_ZONE_X = 1.5;
    private static final float MAX_DANGER_TINT = 0.65f;


    private static final float TARGET_HEIGHT_TO_CELL_RATIO = 1.6f;

    private Float resolvedScale;

    private final HitFlashEffect hitFlash;

    private static final String ICE_BLOCK_PAM =
        "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM";
    private static final String ICE_BLOCK_PREFERRED_CLIP = "idle";

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
    private String resolvedAshClip;
    private boolean ashClipResolved;
    private static final float ASH_DURATION = 1.25f;
    private static final float DIE_DURATION = 1.45f;
    private boolean wearingArmor;

    public ZombieActor(
        Zombie zombie,
        ZombieAnimationSpec animationSpec,
        ZombieAnimationResolver resolver,
        String seasonName,
        PamPlayer pamPlayer,
        float cellHeight
    ) {
        this.zombie = zombie;
        this.hitFlash = new HitFlashEffect(() ->
            zombie.getHp() + (int) zombie.getIceHp()
        );
        this.animationSpec = animationSpec;
        this.resolver = resolver;
        this.seasonName = seasonName;
        this.pamPlayer = pamPlayer;
        this.cellHeight = cellHeight;
        this.wearingArmor = hasLiveArmor();
    }

    private float getScale() {
        if (resolvedScale != null) {
            return resolvedScale;
        }

        String idleClip = animationSpec.getIdleClip();

        if (idleClip == null) {
            return 1f;
        }

        Rectangle bounds = pamPlayer.bounds(
            animationSpec.getPamPath(),
            idleClip
        );

        if (bounds == null || bounds.height <= 0f) {
            return 1f;
        }

        resolvedScale =
            (cellHeight * TARGET_HEIGHT_TO_CELL_RATIO) / bounds.height;

        return resolvedScale;
    }

    private void drawScaled(
        Batch batch,
        String pamPath,
        String clip,
        float time,
        float x,
        float y,
        boolean loop
    ) {
        float scale = getScale();

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4(oldTransform);
        transform.translate(x, y, 0);
        transform.scale(scale, scale, 1f);
        transform.translate(-x, -y, 0);
        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(batch, pamPath, clip, time, x, y, loop);
        } catch (Throwable ignored) {
        }

        batch.setTransformMatrix(oldTransform);
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

        refreshArmorSpec();

        if (zombie.isDead() && !zombie.isAshed()) {
            if (currentState != ZombieAnimationState.DIE) {
                currentState = ZombieAnimationState.DIE;
                stateTime = 0f;
            }
            stateTime += delta;
            if (!zombie.isDeathAnimFinished() && stateTime >= DIE_DURATION) {
                zombie.markDeathAnimFinished();
            }
            return;
        }

        if (zombie.isAshed()) {
            if (currentState != ZombieAnimationState.ASH) {
                currentState = ZombieAnimationState.ASH;
                stateTime = 0f;
            }
            stateTime += delta;
            if (!zombie.isAshFinished() && stateTime >= ASH_DURATION) {
                zombie.markAshFinished();
            }
            return;
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
            if (zombie.isAshed() && !zombie.isAshFinished()) {
                drawAsh(batch, parentAlpha);
            } else if (!zombie.isAshed() && !zombie.isDeathAnimFinished()) {
                drawDie(batch, parentAlpha);
            }
            return;
        }

        if (zombie.isInitialFrozenBlock() || zombie.isIced()) {
            drawIceBlock(batch, parentAlpha);
            return;
        }

        float dangerTint = resolveDangerTint(zombie.getX());
        float flash = hitFlash.getIntensity();

        float[] armorTint = resolveArmorTint();
        batch.setColor(
            armorTint[0] + flash,
            armorTint[1] - dangerTint + flash,
            armorTint[2] - dangerTint + flash,
            parentAlpha
        );

        String clip = animationSpec.getClip(currentState);

        if (clip == null) {
            clip = animationSpec.getIdleClip();
        }

        drawScaled(
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
                drawScaled(
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

    private void refreshArmorSpec() {
        boolean armored = hasLiveArmor();
        if (armored == wearingArmor) {
            return;
        }
        wearingArmor = armored;
        ZombieAnimationSpec next = resolver.resolve(zombie, seasonName);
        if (next != null) {
            animationSpec = next;
            resolvedScale = null;
        }
    }

    private boolean hasLiveArmor() {
        return zombie.getArmor() != null && !zombie.getArmor().isDestroyed();
    }

    private float[] resolveArmorTint() {
        int stage = zombie.getArmorDamageStage();
        if (stage <= 0) {
            return new float[] {1f, 1f, 1f};
        }
        if (stage == 1) {
            return new float[] {1f, 0.82f, 0.62f};
        }
        return new float[] {0.72f, 0.68f, 0.64f};
    }

    private void drawDie(Batch batch, float parentAlpha) {
        batch.setColor(1f, 1f, 1f, parentAlpha);
        String clip = animationSpec.getClip(ZombieAnimationState.DIE);
        if (clip == null) {
            clip = animationSpec.getIdleClip();
        }
        if (clip == null) {
            zombie.markDeathAnimFinished();
            return;
        }
        drawScaled(
            batch,
            animationSpec.getPamPath(),
            clip,
            stateTime,
            getX(),
            getY(),
            false
        );
    }

    private void drawAsh(Batch batch, float parentAlpha) {
        if (!ashClipResolved) {
            resolveAshClip();
            ashClipResolved = true;
        }

        if (resolvedAshClip == null) {
            zombie.markAshFinished();
            return;
        }

        batch.setColor(1f, 1f, 1f, parentAlpha);
        drawScaled(
            batch,
            resolveAshPamPath(),
            resolvedAshClip,
            stateTime,
            getX(),
            getY(),
            false
        );
    }

    private String resolveAshPamPath() {
        if (animationSpec.hasClip(ZombieAnimationState.ASH)) {
            return animationSpec.getPamPath();
        }
        String ashPam = animationSpec.getAshPamPath();
        return ashPam != null ? ashPam : animationSpec.getPamPath();
    }

    private void resolveAshClip() {
        if (animationSpec.hasClip(ZombieAnimationState.ASH)) {
            resolvedAshClip = animationSpec.getClip(ZombieAnimationState.ASH);
            return;
        }

        String ashPam = resolveAshPamPath();
        List<String> clips = pamPlayer.clips(ashPam);
        if (clips == null || clips.isEmpty()) {
            Gdx.app.error("ZombieActor", "No ash clips found for: " + ashPam);
            return;
        }

        for (String clip : clips) {
            String normalized = clip.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            if (normalized.endsWith("ASH")) {
                resolvedAshClip = clip;
                return;
            }
        }

        if (clips.contains("animation")) {
            resolvedAshClip = "animation";
        } else if (clips.contains("idle")) {
            resolvedAshClip = "idle";
        } else {
            resolvedAshClip = clips.get(0);
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

        float flash = hitFlash.getIntensity();
        batch.setColor(1f + flash, 1f + flash, 1f + flash, alpha);

        drawScaled(
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
