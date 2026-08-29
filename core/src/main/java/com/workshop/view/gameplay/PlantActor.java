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
    private final PamPlayer pamPlayer;
    private final HitFlashEffect hitFlash;
    private final float cellHeight;
    private static final float FALLBACK_ATTACK_DURATION = 0.45f;

    // گیاه‌ها معمولاً تقریباً هم‌قدِ یه خونه‌ی گرید هستن؛ این ضریب
    // نسبتِ ارتفاعِ اسپرایتِ خامِ PAM به ارتفاعِ خونه‌ی گرید رو تعیین
    // می‌کنه. اگه هنوز بزرگ/کوچیک بود، همینو دستی تیون کن.
    private static final float TARGET_HEIGHT_TO_CELL_RATIO = 1.1f;

    private Float resolvedScale;

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

    private PlantAnimationState currentState =
        PlantAnimationState.IDLE;

    private float stateTime;
    private float frostStateTime;
    private final float attackDuration;

    public PlantActor(
        Plant plant,
        PlantAnimationSpec animationSpec,
        PamPlayer pamPlayer,
        float cellHeight
    ) {
        this.plant = plant;
        this.hitFlash = new HitFlashEffect(() ->
            plant.getHp()
                + (int) plant.getIceHp()
                + (int) plant.getOctopusHp()
        );
        this.animationSpec = animationSpec;
        this.pamPlayer = pamPlayer;
        this.cellHeight = cellHeight;

        String attackClip =
            animationSpec.getClip(PlantAnimationState.ATTACK);

        if (attackClip == null) {
            attackDuration = 0f;
        } else {
            attackDuration =
                pamPlayer.clipDurationSeconds(
                    animationSpec.getPamPath(),
                    attackClip
                );
        }
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

        boolean iced = plant.isIced();
        int freezeLevel = plant.getFreezeLevel();

        if (!iced) {
            stateTime += delta;

            if (currentState == PlantAnimationState.ATTACK) {
                float duration = attackDuration;

                if (duration <= 0f) {
                    duration = FALLBACK_ATTACK_DURATION;
                }

                if (stateTime >= duration) {
                    currentState = PlantAnimationState.IDLE;
                    stateTime = 0f;
                }
            }
        }

        wasIced = iced;
        lastFreezeLevel = freezeLevel;
        frostStateTime += delta;

        plant.tickPlantFoodGlow(delta);
        if (plant.isShowingPlantFoodGlow()) {
            plantFoodGlowTime += delta;
            if (animationSpec.hasClip(PlantAnimationState.PLANTFOOD)
                && currentState != PlantAnimationState.PLANTFOOD) {
                play(PlantAnimationState.PLANTFOOD);
            }
        } else {
            plantFoodGlowTime = 0f;
            if (currentState == PlantAnimationState.PLANTFOOD) {
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

        if (plant.isIced()) {
            drawIceBlock(batch, parentAlpha);
        } else if (plant.getFreezeLevel() > 0) {
            drawChillOverlay(batch, parentAlpha);
        }
    }

    private void drawPlantFoodGlow(Batch batch, float parentAlpha) {
        if (!plantFoodGlowClipResolved) {
            resolvedPlantFoodGlowClip = resolveClip(
                "PlantActor(plantfood-glow)",
                PLANTFOOD_GLOW_PAM,
                "animation"
            );
            if (resolvedPlantFoodGlowClip == null) {
                resolvedPlantFoodGlowClip = resolveClip(
                    "PlantActor(plantfood-glow)",
                    PLANTFOOD_GLOW_PAM,
                    "idle"
                );
            }
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

    public boolean play(PlantAnimationState state) {
        if (plant.isIced()) {
            return false;
        }

        if (!animationSpec.hasClip(state)) {
            return false;
        }

        // اگر همین animation در حال اجراست، دوباره از صفر شروعش نکن.
        if (currentState == state) {
            return true;
        }

        currentState = state;
        stateTime = 0f;

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
