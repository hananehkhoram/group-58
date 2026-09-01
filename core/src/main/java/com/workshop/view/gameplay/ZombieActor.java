package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.ZombossSummon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final float BOSS_HEIGHT_TO_CELL_RATIO = 3.6f;

    private Float resolvedScale;

    private final HitFlashEffect hitFlash;

    private String resolvedSandstormIntroClip;
    private String resolvedSandstormLoopClip;
    private String resolvedSandstormOutroClip;
    private boolean sandstormClipResolved;
    private static final String SANDSTORM_TOP_PAM =
        "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";
    private static final String SANDSTORM_REAR_PAM =
        "768/INITIAL/EFFECTS/SANDSTORM_REAR/SANDSTORM_REAR.PAM";
    private static final float SANDSTORM_HEIGHT_TO_CELL_RATIO = 2.15f;

    private float sandstormEffectTime;
    private Float sandstormScale;

    private static final float ICE_HEIGHT_TO_CELL_RATIO =
        TARGET_HEIGHT_TO_CELL_RATIO * 1.05f;

    private static final float ICE_X_OFFSET = 0f;
    private static final float ICE_Y_OFFSET = 0f;

    private boolean wasIceVisible;
    private float iceBreakTimer;

    private ZombieAnimationState currentState =
        ZombieAnimationState.IDLE;

    private boolean isZombotanyPeashooter() {
        return "ZombieZombotanyPeashooter".equals(zombie.getId());
    }

    private boolean isZombotanySquash() {
        return "ZombieZombotanySquash".equals(zombie.getId());
    }

    private boolean isZombotanyWallnut() {
        return "ZombieZombotanyWallnut".equals(zombie.getId());
    }

    private boolean isZombotanyJalapeno() {
        return "ZombieZombotanyJalapeno".equals(zombie.getId());
    }

    private float stateTime;
    private String resolvedAshClip;
    private boolean ashClipResolved;
    private static final float ASH_DURATION = 1.25f;
    private static final float DIE_DURATION = 1.45f;
    private boolean wearingArmor;
    private boolean wasArmless;

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

        this.wasIceVisible = isIceVisible();
        printZombieParts();
        printSquashParts();
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

        float heightRatio = zombie.isBoss()
            ? BOSS_HEIGHT_TO_CELL_RATIO
            : TARGET_HEIGHT_TO_CELL_RATIO;
        resolvedScale =
            (cellHeight * heightRatio) / bounds.height;

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
        drawScaled(batch, pamPath, clip, time, x, y, loop, getScale(), currentPartsVisibility());
    }

    private void drawScaled(
        Batch batch,
        String pamPath,
        String clip,
        float time,
        float x,
        float y,
        boolean loop,
        float scale
    ) {
        drawScaled(batch, pamPath, clip, time, x, y, loop, scale, null);
    }

    private void drawOverlayScaled(
        Batch batch,
        String pamPath,
        String clip,
        float time,
        float x,
        float y,
        boolean loop
    ) {

        Rectangle bounds = pamPlayer.bounds(
            pamPath,
            clip
        );

        float scale = 1f;

        if (bounds != null && bounds.height > 0f) {
            scale =
                (cellHeight * TARGET_HEIGHT_TO_CELL_RATIO)
                    / bounds.height;
        }

        Matrix4 oldTransform =
            batch.getTransformMatrix().cpy();

        Matrix4 transform =
            new Matrix4(oldTransform);

        transform.translate(x, y, 0);
        transform.scale(scale, scale, 1f);
        transform.translate(-x, -y, 0);

        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(
                batch,
                pamPath,
                clip,
                time,
                x,
                y,
                loop,
                null
            );
        } catch(Throwable ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    private void drawScaled(
        Batch batch,
        String pamPath,
        String clip,
        float time,
        float x,
        float y,
        boolean loop,
        float scale,
        Map<String, Boolean> partsVisibility
    ) {
        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4(oldTransform);

        float scaleX = (zombie.getSpeed() < 0) ? -scale : scale;

        transform.translate(x, y, 0);
        transform.scale(scaleX, scale, 1f);
        transform.translate(-x, -y, 0);
        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(batch, pamPath, clip, time, x, y, loop, partsVisibility);
            if (zombie.hasLostArm()) {
                for (String stump : ZombieArmVisibility.stumpParts(pamPlayer, pamPath)) {
                    pamPlayer.drawPart(batch, pamPath, clip, time, x, y, stump);
                }
            }
        } catch (Throwable ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    private Map<String, Boolean> currentPartsVisibility() {
        Map<String, Boolean> props = ZombiePropVisibility.visibility(
            pamPlayer,
            animationSpec.getPamPath(),
            zombie
        );

        Map<String, Boolean> arm = null;
        if (zombie.hasLostArm()
            && !animationSpec.hasArmlessClip(currentState)) {
            arm = ZombieArmVisibility.hideOuterArm(
                pamPlayer,
                animationSpec.getPamPath()
            );
        }

        Map<String, Boolean> zombotany = zombotanyHiddenParts();

        Map<String, Boolean> merged = new HashMap<>();

        if (props != null) {
            merged.putAll(props);
        }
        if (arm != null) {
            merged.putAll(arm);
        }
        if (zombotany != null) {
            merged.putAll(zombotany);
        }

        return merged.isEmpty() ? null : merged;
    }

    private float getSandstormScale() {
        if (sandstormScale != null) {
            return sandstormScale;
        }

        String clip = resolvedSandstormLoopClip != null
            ? resolvedSandstormLoopClip
            : resolvedSandstormIntroClip;
        if (clip == null) {
            return 1f;
        }

        Rectangle bounds = pamPlayer.bounds(SANDSTORM_TOP_PAM, clip);

        if (bounds == null || bounds.height <= 0f) {
            sandstormScale = 1f;
            return sandstormScale;
        }

        sandstormScale =
            (cellHeight * SANDSTORM_HEIGHT_TO_CELL_RATIO) / bounds.height;
        return sandstormScale;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        hitFlash.update(delta);
        updateIceBreakState(delta);

        if (zombie.isEnteredViaSandstorm()) {
            sandstormEffectTime += delta;
        }

        refreshArmorSpec();

        boolean armless = zombie.hasLostArm();
        if (armless != wasArmless) {
            wasArmless = armless;
            stateTime = 0f;
        }

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
        stateTime += zombie.isStunned() ? delta * 0.2f : delta;
    }

    private void updateAnimationState() {
        ZombieAnimationState nextState;

        if (zombie.isEating()) {
            nextState = ZombieAnimationState.EAT;
        } else if (zombie.isBoss()) {
            nextState = bossAnimationState(zombie.getZomboss());
        } else {
            nextState = ZombieAnimationState.WALK;
        }

        if (!animationSpec.hasClip(nextState, zombie.hasLostArm())) {
            nextState = ZombieAnimationState.IDLE;
        }

        if (currentState != nextState) {
            currentState = nextState;
            stateTime = 0f;
        }
    }

    private ZombieAnimationState bossAnimationState(ZombossSummon zomboss) {
        if (zomboss == null) {
            return ZombieAnimationState.IDLE;
        }
        if (zomboss.isPhysicallyMoving()) {
            return ZombieAnimationState.WALK;
        }
        return switch (zomboss.getCurrentState()) {
            case INTRO -> ZombieAnimationState.INTRO;
            case STUNNED -> ZombieAnimationState.STUN;
            case WALKING, DASHING -> ZombieAnimationState.WALK;
            case IDLE, USING_VORTEX, LAUNCHING_SHARKS -> ZombieAnimationState.IDLE;
            default -> ZombieAnimationState.ATTACK;
        };
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

        resolvedSandstormIntroClip = pickClip(clips, "intro", "animation", "idle");
        resolvedSandstormLoopClip = pickClip(clips, "loop", "animation", "idle");
        resolvedSandstormOutroClip = pickClip(clips, "outro", "intro", "animation");
        if (resolvedSandstormLoopClip == null) {
            resolvedSandstormLoopClip = clips.get(0);
        }
        if (resolvedSandstormIntroClip == null) {
            resolvedSandstormIntroClip = resolvedSandstormLoopClip;
        }
        if (resolvedSandstormOutroClip == null) {
            resolvedSandstormOutroClip = resolvedSandstormLoopClip;
        }
    }

    private static String pickClip(List<String> clips, String... names) {
        for (String name : names) {
            if (clips.contains(name)) {
                return name;
            }
        }
        return null;
    }

    private String currentSandstormClip() {
        if (zombie.isSandstormLanded()) {
            return resolvedSandstormOutroClip;
        }
        if (sandstormEffectTime < 0.4f) {
            return resolvedSandstormIntroClip;
        }
        return resolvedSandstormLoopClip;
    }

    private float currentSandstormTime() {
        if (zombie.isSandstormLanded()) {
            return zombie.getSandstormLandTime();
        }
        if (sandstormEffectTime < 0.4f) {
            return sandstormEffectTime;
        }
        return sandstormEffectTime - 0.4f;
    }

    private boolean currentSandstormLoops() {
        return !zombie.isSandstormLanded() && sandstormEffectTime >= 0.4f;
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

        boolean inStorm = zombie.isEnteredViaSandstorm();
        if (inStorm) {
            if (!sandstormClipResolved) {
                resolveSandstormClip();
                sandstormClipResolved = true;
            }
            drawSandstormLayer(batch, parentAlpha, SANDSTORM_REAR_PAM);
        }

        float bodyAlpha = inStorm && !zombie.isSandstormLanded()
            ? parentAlpha * 0.55f
            : parentAlpha;

        if (zombie.isStunned()) {
            batch.setColor(
                1f,
                0.85f + flash * 0.15f,
                0.2f + flash,
                bodyAlpha
            );
        } else {
            batch.setColor(
                1f + flash,
                1f - dangerTint + flash,
                1f - dangerTint + flash,
                bodyAlpha
            );
        }

        String clip = animationSpec.getClip(currentState, zombie.hasLostArm());

        if (clip == null) {
            clip = animationSpec.getClip(
                ZombieAnimationState.IDLE,
                zombie.hasLostArm()
            );
        }
        if (clip == null) {
            clip = animationSpec.getIdleClip();
        }

        boolean loop = currentState != ZombieAnimationState.ATTACK
            && currentState != ZombieAnimationState.INTRO
            && currentState != ZombieAnimationState.DIE;
        if (currentState == ZombieAnimationState.ATTACK && animationSpec.attackLoops()) {
            loop = true;
        }
        float clipTime = stateTime;
        if (currentState == ZombieAnimationState.ATTACK) {
            clipTime += animationSpec.getAttackTimeOffset();
        }

        drawScaled(
            batch,
            animationSpec.getPamPath(),
            clip,
            clipTime,
            getX(),
            getY(),
            loop
        );

        if (isZombotanyPeashooter()) {
            drawPeashooterHead(batch);
        }

        if (isZombotanySquash()) {
            drawSquashHead(batch);
        }

        if (isZombotanyWallnut()) {
            drawWallnutHead(batch);
        }

        if (isZombotanyJalapeno()) {
            drawJalapenoHead(batch);
        }

        ZombieArmorLooks.draw(
            batch,
            zombie,
            getX(),
            getY(),
            cellHeight,
            bodyAlpha
        );

        if (inStorm) {
            drawSandstormLayer(batch, parentAlpha, SANDSTORM_TOP_PAM);
        }

        if (iceBreakTimer > 0f) {
            IceDamageSprite.drawBreak(
                batch,
                getX(),
                getY(),
                cellHeight,
                ICE_HEIGHT_TO_CELL_RATIO,
                ICE_X_OFFSET,
                ICE_Y_OFFSET,
                parentAlpha
            );
        }
    }

    private void drawSandstormLayer(Batch batch, float parentAlpha, String pamPath) {
        String stormClip = currentSandstormClip();
        if (stormClip == null) {
            return;
        }
        batch.setColor(1f, 1f, 1f, parentAlpha);
        drawScaled(
            batch,
            pamPath,
            stormClip,
            currentSandstormTime(),
            getX(),
            getY(),
            currentSandstormLoops(),
            getSandstormScale()
        );
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

    private void drawDie(Batch batch, float parentAlpha) {
        batch.setColor(1f, 1f, 1f, parentAlpha);
        String clip = animationSpec.getClip(
            ZombieAnimationState.DIE,
            zombie.hasLostArm()
        );
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

    private void drawIceBlock(
        Batch batch,
        float parentAlpha
    ) {
        IceDamageSprite.drawDamage(
            batch,
            zombie.getIceHp(),
            getX(),
            getY(),
            cellHeight,
            ICE_HEIGHT_TO_CELL_RATIO,
            ICE_X_OFFSET,
            ICE_Y_OFFSET,
            parentAlpha,
            hitFlash.getIntensity()
        );
    }

    public ZombieAnimationState getCurrentState() {
        return currentState;
    }

    public Zombie getZombie() {
        return zombie;
    }

    private boolean isIceVisible() {
        return zombie.isInitialFrozenBlock()
            || zombie.isIced();
    }

    private void updateIceBreakState(float delta) {
        boolean iceVisible = isIceVisible();

        if (wasIceVisible && !iceVisible) {
            iceBreakTimer =
                IceDamageSprite.BREAK_DURATION;
        }

        if (iceBreakTimer > 0f) {
            iceBreakTimer -= delta;

            if (iceBreakTimer < 0f) {
                iceBreakTimer = 0f;
            }
        }

        wasIceVisible = iceVisible;
    }

    private void drawPeashooterHead(Batch batch) {
        String pamPath =
            "768/INITIAL/PLANT/PEASHOOTER/PEASHOOTER.PAM";

        String clip = zombie.isEating() ? "attack" : "idle";

        Rectangle bounds = pamPlayer.bounds(pamPath, clip);

        float scale = 1f;
        if (bounds != null && bounds.height > 0f) {
            scale =
                (cellHeight * TARGET_HEIGHT_TO_CELL_RATIO)
                    / bounds.height;
        }

        scale *= 0.72f;

        float x = getX() + cellHeight * 0.02f;
        float y = getY() + cellHeight * 0.25f;

        Matrix4 oldTransform =
            batch.getTransformMatrix().cpy();

        Matrix4 transform =
            new Matrix4(oldTransform);

        float scaleX = (zombie.getSpeed() < 0) ? -scale : scale;

        transform.translate(x, y, 0);
        transform.scale(-scale, scale, 1f);
        transform.translate(-x, -y, 0);

        batch.setTransformMatrix(transform);

        try {
            pamPlayer.drawPart(
                batch, pamPath, clip, stateTime, x, y,
                "peashooter_head_base"
            );

            pamPlayer.drawPart(
                batch, pamPath, clip, stateTime, x, y,
                "peashooter_eye"
            );

            pamPlayer.drawPart(
                batch, pamPath, clip, stateTime, x, y,
                "peashooter_mouth"
            );
        } catch (Throwable ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    private void drawJalapenoHead(Batch batch) {

        String pamPath =
            "768/INITIAL/PLANT/JALAPENO/JALAPENO.PAM";

        String clip = "idle";

        Rectangle bounds = pamPlayer.bounds(
            pamPath,
            clip
        );

        float scale = 1f;

        if (bounds != null && bounds.height > 0f) {
            scale =
                (cellHeight * TARGET_HEIGHT_TO_CELL_RATIO)
                    / bounds.height;
        }

        scale *= 0.42f;

        float x = getX() + cellHeight * 0.03f;
        float y = getY() + cellHeight * 0.18f + 30;

        Matrix4 oldTransform =
            batch.getTransformMatrix().cpy();

        Matrix4 transform =
            new Matrix4(oldTransform);

        transform.translate(x, y, 0);
        transform.scale(-scale, scale, 1f);
        transform.translate(-x, -y, 0);

        batch.setTransformMatrix(transform);

        try {
            pamPlayer.drawPart(
                batch,
                pamPath,
                clip,
                stateTime,
                x,
                y,
                "root"
            );
        } catch (Throwable ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    private void drawSquashHead(Batch batch) {
        System.out.println("[SQUASH DRAW TEST]");

        String pamPath =
            "768/INITIAL/PLANT/SQUASH/SQUASH.PAM";

        String clip = "idle";

        Rectangle bounds = pamPlayer.bounds(
            pamPath,
            clip
        );

        float scale = 1f;

        if (bounds != null && bounds.height > 0f) {
            scale =
                (cellHeight * TARGET_HEIGHT_TO_CELL_RATIO)
                    / bounds.height;
        }


        scale *= 0.45f;


        float x = getX() + cellHeight * 0.05f;
        float y = getY() + cellHeight * 0.55f;


        Matrix4 oldTransform =
            batch.getTransformMatrix().cpy();


        Matrix4 transform =
            new Matrix4(oldTransform);


        float scaleX = (zombie.getSpeed() < 0)
            ? -scale
            : scale;


        transform.translate(x, y, 0);
        transform.scale(-scaleX, scale, 1f);
        transform.translate(-x, -y, 0);


        batch.setTransformMatrix(transform);


        try {

            pamPlayer.drawPart(
                batch,
                pamPath,
                clip,
                stateTime,
                x,
                y,
                "root"
            );

        } catch(Throwable ignored) {

        }

        batch.setTransformMatrix(oldTransform);
    }

    private void drawWallnutHead(Batch batch) {

        String pamPath =
            "PLANT/WALLNUT/WALLNUT.PAM";

        String clip = "idle";

        Rectangle bounds = pamPlayer.bounds(
            pamPath,
            clip
        );

        float scale = 1f;

        if (bounds != null && bounds.height > 0f) {
            scale =
                (cellHeight * TARGET_HEIGHT_TO_CELL_RATIO)
                    / bounds.height;
        }

        scale *= 0.35f;

        float x = getX() + cellHeight * 0.02f;
        float y = getY() + cellHeight * 0.02f + 40;

        Matrix4 oldTransform =
            batch.getTransformMatrix().cpy();

        Matrix4 transform =
            new Matrix4(oldTransform);

        float scaleX = -scale;

        transform.translate(x, y, 0);
        transform.scale(scaleX, scale, 1f);
        transform.translate(-x, -y, 0);

        batch.setTransformMatrix(transform);

        try {
            pamPlayer.drawPart(
                batch,
                pamPath,
                clip,
                stateTime,
                x,
                y,
                "root"
            );
        } catch (Throwable ignored) {
        }

        batch.setTransformMatrix(oldTransform);
    }

    private Map<String, Boolean> zombotanyHiddenParts() {

        if (!isZombotanyPeashooter()
            && !isZombotanySquash()
            && !"ZombieZombotanyWallnut".equals(zombie.getId())
            && !"ZombieZombotanyJalapeno".equals(zombie.getId())) {
            return null;
        }

        Map<String, Boolean> hidden = new HashMap<>();

        hidden.put("zombie_skull", false);
        hidden.put("zombie_jaw", false);
        hidden.put("zombie_pupil", false);

        return hidden;
    }

    private void printZombieParts() {

        PamPlayer.AnimationPart root =
            pamPlayer.getParts(
                "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM"
            );

        printPart(root, "");
    }

    private void printSquashParts() {

        PamPlayer.AnimationPart root =
            pamPlayer.getParts(
                "768/INITIAL/PLANT/SQUASH/SQUASH.PAM"
            );

        printPart(root, "");
    }


    private void printPart(
        PamPlayer.AnimationPart part,
        String space
    ) {
        if (part == null) {
            return;
        }

        System.out.println(
            "[ZOMBIE_PART] "
                + space
                + part.name
        );

        if (part.children != null) {
            for (Object child : part.children) {
                printPart(
                    (PamPlayer.AnimationPart) child,
                    space + "  "
                );
            }
        }
    }
}
