package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.plants.Plant;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

public final class PlantActor extends Actor {

    private final Plant plant;
    private final PlantAnimationSpec animationSpec;
    private final PlantAnimationSpec lilyPadSpec;
    private final PamPlayer pamPlayer;
    private final HitFlashEffect hitFlash;
    private final float cellHeight;
    private static final float FALLBACK_ATTACK_DURATION = 0.45f;
    private static final float SEQUENTIAL_SPIT_GAP = 0.14f;

    // گیاه‌ها معمولاً تقریباً هم‌قدِ یه خونه‌ی گرید هستن؛ این ضریب
    // نسبتِ ارتفاعِ اسپرایتِ خامِ PAM به ارتفاعِ خونه‌ی گرید رو تعیین
    // می‌کنه. اگه هنوز بزرگ/کوچیک بود، همینو دستی تیون کن.
    private static final float TARGET_HEIGHT_TO_CELL_RATIO = 1.1f;
    private static final float GRAVE_BUSTER_HEIGHT_TO_CELL_RATIO = 1.55f;
    private static final float MINT_HEIGHT_TO_CELL_RATIO = 2.0f;

    private Float resolvedScale;
    private Float lilyPadScale;

    private static final String ICE_BLOCK_PAM =
        "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    private static final String ICE_BLOCK_PREFERRED_CLIP = "idle";

    private boolean wasIced;
    private int lastFreezeLevel;

    private static final String CHILL_OVERLAY_PAM =
        "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM";
    private static final String CHILL_OVERLAY_PREFERRED_CLIP = "idle";

    private static final String PLANTFOOD_GLOW_PAM =
        "768/INITIAL/EFFECTS/PLANTFOOD_FX/PLANTFOOD_FX.PAM";
    private static final float PLANTFOOD_GLOW_HEIGHT_TO_CELL_RATIO = 1.7f;
    private static final String PULLED_PAM =
        "768/FULL/EFFECTS/ZOMBOSS_PLANT_PULLED/ZOMBOSS_PLANT_PULLED.PAM";

    // iceHp شروعش برای یخ‌زدگیِ کامل، ۶۰۰ است (رجوع کنید به
    // Plant.increaseFreezeLevel()).
    private static final double INITIAL_ICE_HP = 600.0;

    // این کتابخونه فقط یه کلیپِ واقعاً قابل‌پخش (معمولاً idle) داره؛
    // کلیپ‌های مرحله‌ی آسیب که داخل خودِ فایل PAM هستن، از این API
    // قابل‌پخش نیستن. برای همین مرحله‌ی آسیب رو با شفافیت شبیه‌سازی می‌کنیم.
    private static final float MIN_ICE_ALPHA = 0.25f;

    private String resolvedIceBlockClip;
    private boolean iceBlockClipResolved;

    private String resolvedChillClip;
    private boolean chillClipResolved;

    private String resolvedPlantFoodGlowClip;
    private boolean plantFoodGlowClipResolved;
    private Float plantFoodGlowScale;
    private float plantFoodGlowTime;
    private String resolvedPulledClip;
    private boolean pulledClipResolved;
    private Float pulledScale;

    private PlantAnimationState currentState =
        PlantAnimationState.IDLE;

    private float stateTime;
    private float frostStateTime;
    private Float resolvedAttackDuration;
    private Float graveBusterAttackDuration;
    private int spitReleaseIndex;

    public PlantActor(
        Plant plant,
        PlantAnimationSpec animationSpec,
        PamPlayer pamPlayer,
        float cellHeight
    ) {
        this(plant, animationSpec, pamPlayer, cellHeight, null);
    }

    public PlantActor(
        Plant plant,
        PlantAnimationSpec animationSpec,
        PamPlayer pamPlayer,
        float cellHeight,
        PlantAnimationSpec lilyPadSpec
    ) {
        this.plant = plant;
        this.hitFlash = new HitFlashEffect(() ->
            plant.getHp()
                + (int) plant.getIceHp()
                + (int) plant.getOctopusHp()
        );
        this.animationSpec = animationSpec;
        this.lilyPadSpec = lilyPadSpec;
        this.pamPlayer = pamPlayer;
        this.cellHeight = cellHeight;

        if (plant.isGraveDestroyer()) {
            currentState = PlantAnimationState.ATTACK;
        }
    }

    private float getScale() {
        if (resolvedScale != null) {
            return resolvedScale;
        }

        animationSpec.ensureClipsBound(pamPlayer);
        String clip = scaleClip();
        if (clip == null) {
            return 1f;
        }

        Rectangle bounds;
        try {
            bounds = pamPlayer.bounds(
                animationSpec.getPamPath(),
                clip
            );
        } catch (RuntimeException exception) {
            return 1f;
        }
        if (bounds == null || bounds.height <= 0f) {
            return 1f;
        }

        if (plant.isGraveDestroyer()) {
            float target = cellHeight * GRAVE_BUSTER_HEIGHT_TO_CELL_RATIO;
            float scaleY = target / bounds.height;
            float scaleX = bounds.width > 0f ? target / bounds.width : scaleY;
            resolvedScale = Math.max(scaleX, scaleY);
            return resolvedScale;
        }

        float ratio = isEmpowerMint()
            ? MINT_HEIGHT_TO_CELL_RATIO
            : TARGET_HEIGHT_TO_CELL_RATIO;
        resolvedScale = (cellHeight * ratio) / bounds.height;
        return resolvedScale;
    }

    private String scaleClip() {
        if (plant.isGraveDestroyer()) {
            String attack = animationSpec.getClip(PlantAnimationState.ATTACK);
            if (attack != null) {
                return attack;
            }
        }
        return animationSpec.getIdleClip();
    }

    private boolean isEmpowerMint() {
        String name = plant.getName();
        if (name == null) {
            return false;
        }
        String compact = name.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return compact.endsWith("mint");
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
        if (plant.isBeingPulled()) {
            transform.rotate(0f, 0f, 1f, stateTime * 220f);
        }
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

        boolean iced = plant.isIced();
        int freezeLevel = plant.getFreezeLevel();

        if (!iced) {
            stateTime += delta;

            if (plant.isGraveDestroyer()) {
                animationSpec.ensureClipsBound(pamPlayer);
                play(PlantAnimationState.ATTACK);
                if (stateTime >= graveBusterAttackDuration()) {
                    plant.markActionComplete();
                }
            } else if (currentState == PlantAnimationState.ATTACK) {
                if (stateTime >= attackClipDuration()) {
                    if (hasPlantFoodBodyClip() && plant.isShowingPlantFoodGlow()) {
                        play(PlantAnimationState.PLANTFOOD);
                    } else if (plant.isPlantFoodActive()) {
                        stateTime = 0f;
                        spitReleaseIndex = 0;
                    } else {
                        currentState = PlantAnimationState.IDLE;
                        stateTime = 0f;
                        spitReleaseIndex = 0;
                    }
                }
            }
        }

        wasIced = iced;
        lastFreezeLevel = freezeLevel;
        frostStateTime += delta;

        plant.tickPlantFoodGlow(delta);
        if (plant.isShowingPlantFoodGlow()) {
            plantFoodGlowTime += delta;
            if (hasPlantFoodBodyClip()
                && currentState != PlantAnimationState.PLANTFOOD) {
                play(PlantAnimationState.PLANTFOOD);
            }
        } else {
            plantFoodGlowTime = 0f;
            if (currentState == PlantAnimationState.PLANTFOOD
                && !plant.isGraveDestroyer()) {
                playIdle();
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (plant.isDead()) {
            return;
        }

        if (plant.isShowingPlantFoodGlow()) {
            drawPlantFoodGlow(batch, parentAlpha);
        }

        drawLilyPadUnderneath(batch, parentAlpha);

        animationSpec.ensureClipsBound(pamPlayer);
        String clip = animationSpec.getClip(currentState);
        if (clip == null) {
            clip = animationSpec.getIdleClip();
        }
        final String drawClip = clip;

        if (drawClip != null) {
            hitFlash.drawWithFlash(batch, parentAlpha, () -> {
                boolean loop =
                    currentState != PlantAnimationState.ATTACK;

                drawScaled(
                    batch,
                    animationSpec.getPamPath(),
                    drawClip,
                    stateTime,
                    getX(),
                    getY(),
                    loop
                );
            });
        }

        if (plant.isBeingPulled()) {
            drawPulledOverlay(batch, parentAlpha);
        }

        if (plant.isIced()) {
            drawIceBlock(batch, parentAlpha);
        } else if (plant.getFreezeLevel() > 0) {
            drawChillOverlay(batch, parentAlpha);
        }
    }

    private void drawLilyPadUnderneath(Batch batch, float parentAlpha) {
        if (lilyPadSpec == null || !plant.isHasLilyPadUnderneath()) {
            return;
        }

        lilyPadSpec.ensureClipsBound(pamPlayer);
        String clip = lilyPadSpec.getIdleClip();
        if (clip == null) {
            return;
        }

        batch.setColor(1f, 1f, 1f, parentAlpha);
        drawScaled(
            batch,
            lilyPadSpec.getPamPath(),
            clip,
            stateTime,
            getX(),
            getY() - cellHeight * 0.08f,
            true,
            getLilyPadScale()
        );
    }

    private float getLilyPadScale() {
        if (lilyPadScale != null) {
            return lilyPadScale;
        }
        if (lilyPadSpec == null) {
            return 1f;
        }
        String clip = lilyPadSpec.getIdleClip();
        if (clip == null) {
            return 1f;
        }
        Rectangle bounds;
        try {
            bounds = pamPlayer.bounds(lilyPadSpec.getPamPath(), clip);
        } catch (RuntimeException exception) {
            lilyPadScale = 1f;
            return lilyPadScale;
        }
        if (bounds == null || bounds.height <= 0f) {
            lilyPadScale = 1f;
            return lilyPadScale;
        }
        lilyPadScale = (cellHeight * 0.95f) / bounds.height;
        return lilyPadScale;
    }

    private void drawPlantFoodGlow(Batch batch, float parentAlpha) {
        if (!plantFoodGlowClipResolved) {
            resolvedPlantFoodGlowClip = resolveClip(
                "PlantActor(plantfood-glow)",
                PLANTFOOD_GLOW_PAM,
                "plantfood_on"
            );
            plantFoodGlowClipResolved = true;
        }

        if (resolvedPlantFoodGlowClip == null) {
            return;
        }

        batch.setColor(1f, 1f, 1f, parentAlpha);
        drawScaled(
            batch,
            PLANTFOOD_GLOW_PAM,
            resolvedPlantFoodGlowClip,
            plantFoodGlowTime,
            getX(),
            getY(),
            true,
            getPlantFoodGlowScale()
        );
    }

    private float getPlantFoodGlowScale() {
        if (plantFoodGlowScale != null) {
            return plantFoodGlowScale;
        }

        if (resolvedPlantFoodGlowClip == null) {
            return 1f;
        }

        Rectangle bounds = pamPlayer.bounds(
            PLANTFOOD_GLOW_PAM,
            resolvedPlantFoodGlowClip
        );

        if (bounds == null || bounds.height <= 0f) {
            plantFoodGlowScale = 1f;
            return plantFoodGlowScale;
        }

        plantFoodGlowScale =
            (cellHeight * PLANTFOOD_GLOW_HEIGHT_TO_CELL_RATIO) / bounds.height;
        return plantFoodGlowScale;
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

    private void drawPulledOverlay(Batch batch, float parentAlpha) {
        if (!pulledClipResolved) {
            resolvedPulledClip = resolveClip(
                "PlantActor(pulled)",
                PULLED_PAM,
                "animation"
            );
            pulledClipResolved = true;
        }
        if (resolvedPulledClip == null) {
            return;
        }
        if (pulledScale == null) {
            Rectangle bounds = pamPlayer.bounds(PULLED_PAM, resolvedPulledClip);
            if (bounds != null && bounds.height > 0f) {
                pulledScale = (cellHeight * 1.15f) / bounds.height;
            } else {
                pulledScale = 1f;
            }
        }
        batch.setColor(1f, 1f, 1f, parentAlpha);
        drawScaled(
            batch,
            PULLED_PAM,
            resolvedPulledClip,
            stateTime,
            getX(),
            getY(),
            true,
            pulledScale
        );
    }

    private void drawIceBlock(Batch batch, float parentAlpha) {
        if (!iceBlockClipResolved) {
            resolvedIceBlockClip = resolveClip(
                "PlantActor(ice-block)",
                ICE_BLOCK_PAM,
                ICE_BLOCK_PREFERRED_CLIP
            );
            iceBlockClipResolved = true;
        }

        if (resolvedIceBlockClip == null) {
            return;
        }

        float alpha = resolveIceBlockAlpha(plant.getIceHp()) * parentAlpha;
        drawWithAlpha(batch, ICE_BLOCK_PAM, resolvedIceBlockClip, alpha, parentAlpha,false);
    }

    private void drawChillOverlay(Batch batch, float parentAlpha) {
        if (!chillClipResolved) {
            resolvedChillClip = resolveClip(
                "PlantActor(chill-overlay)",
                CHILL_OVERLAY_PAM,
                CHILL_OVERLAY_PREFERRED_CLIP
            );
            chillClipResolved = true;
        }

        if (resolvedChillClip == null) {
            return;
        }

        // freezeLevel ۱ یا ۲: هرچی نزدیک‌تر به یخ‌زدگیِ کامل (۳)، توپرتر.
        float alpha = MathUtils.clamp(plant.getFreezeLevel() / 3f, 0.3f, 0.8f) * parentAlpha;
        drawWithAlpha(batch, CHILL_OVERLAY_PAM, resolvedChillClip, alpha, parentAlpha,false);
    }

    private void drawWithAlpha(
        Batch batch,
        String pamPath,
        String clip,
        float alpha,
        float restoreAlpha,
        boolean loop
    ) {
        hitFlash.drawWithFlash(batch, alpha, () -> {
            drawScaled(
                batch,
                pamPath,
                clip,
                frostStateTime,
                getX(),
                getY(),
                loop
            );
        });
        batch.setColor(1f, 1f, 1f, restoreAlpha);
    }

    private String resolveClip(String tag, String pamPath, String preferredClip) {
        List<String> clips = pamPlayer.clips(pamPath);

        if (clips == null || clips.isEmpty()) {
            Gdx.app.error(tag, "No clips found for PAM: " + pamPath);
            return null;
        }

        if (clips.contains(preferredClip)) {
            return preferredClip;
        }

        Gdx.app.log(
            tag,
            "Clip \"" + preferredClip + "\" not found in " + pamPath
                + ", falling back to \"" + clips.get(0) + "\". Available: " + clips
        );

        return clips.get(0);
    }

    /**
     * شفافیتِ بلوک رو متناسب با آسیبِ باقی‌مونده حساب می‌کنه: سالم
     * (iceHp کامل) کاملاً توپر، نزدیک شکستن (iceHp نزدیک صفر) خیلی
     * کم‌رنگ (ولی نه کاملاً محو).
     */
    private float resolveIceBlockAlpha(double iceHp) {
        float fraction = MathUtils.clamp(
            (float) (iceHp / INITIAL_ICE_HP),
            0f,
            1f
        );

        return MIN_ICE_ALPHA + fraction * (1f - MIN_ICE_ALPHA);
    }

    private float graveBusterAttackDuration() {
        if (graveBusterAttackDuration != null) {
            return graveBusterAttackDuration;
        }

        animationSpec.ensureClipsBound(pamPlayer);
        String clip = animationSpec.getClip(PlantAnimationState.ATTACK);
        if (clip == null) {
            clip = animationSpec.getIdleClip();
        }

        try {
            if (clip != null) {
                float duration = pamPlayer.clipDurationSeconds(
                    animationSpec.getPamPath(),
                    clip
                );
                if (duration > 0.05f) {
                    graveBusterAttackDuration = duration;
                    return duration;
                }
            }
        } catch (RuntimeException ignored) {
        }

        graveBusterAttackDuration = 2f;
        return graveBusterAttackDuration;
    }

    public boolean play(PlantAnimationState state) {
        if (plant.isIced()) {
            return false;
        }

        animationSpec.ensureClipsBound(pamPlayer);
        if (!animationSpec.hasClip(state)) {
            return false;
        }

        if (state == PlantAnimationState.ATTACK
            && hasPlantFoodBodyClip()
            && plant.isShowingPlantFoodGlow()) {
            if (currentState != PlantAnimationState.PLANTFOOD) {
                currentState = PlantAnimationState.PLANTFOOD;
                stateTime = 0f;
            }
            return true;
        }

        // اگر همین animation در حال اجراست، دوباره از صفر شروعش نکن.
        if (currentState == state) {
            return true;
        }

        currentState = state;
        stateTime = 0f;
        if (state == PlantAnimationState.ATTACK) {
            spitReleaseIndex = 0;
        }

        return true;
    }

    private boolean hasPlantFoodBodyClip() {
        String clip = animationSpec.getClip(PlantAnimationState.PLANTFOOD);
        if (clip == null) {
            return false;
        }
        String compact = clip.toLowerCase();
        return compact.contains("plantfood") || compact.contains("plant_food");
    }

    int dueShotReleases() {
        if (currentState != PlantAnimationState.ATTACK) {
            return 0;
        }
        float spitAt = attackClipDuration() * plant.attackReleaseRatio();
        if (stateTime < spitAt) {
            return 0;
        }
        if (!plant.releasesShotsSequentially()) {
            return Math.max(0, plant.pendingShotCount());
        }
        int due = 0;
        while (spitReleaseIndex + due < 8
            && stateTime >= spitAt + (spitReleaseIndex + due) * SEQUENTIAL_SPIT_GAP) {
            due++;
        }
        return due;
    }

    void markShotReleased() {
        spitReleaseIndex++;
    }

    private float attackClipDuration() {
        if (resolvedAttackDuration != null) {
            return resolvedAttackDuration;
        }
        animationSpec.ensureClipsBound(pamPlayer);
        String clip = animationSpec.getClip(PlantAnimationState.ATTACK);
        if (clip == null) {
            resolvedAttackDuration = FALLBACK_ATTACK_DURATION;
            return resolvedAttackDuration;
        }
        try {
            float duration = pamPlayer.clipDurationSeconds(
                animationSpec.getPamPath(),
                clip
            );
            if (duration > 0.08f) {
                resolvedAttackDuration = duration;
                return resolvedAttackDuration;
            }
        } catch (RuntimeException ignored) {
        }
        resolvedAttackDuration = FALLBACK_ATTACK_DURATION;
        return resolvedAttackDuration;
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
