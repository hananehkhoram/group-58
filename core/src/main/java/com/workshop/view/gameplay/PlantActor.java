package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.workshop.model.plants.Plant;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

public final class PlantActor extends Actor {

    private final Plant plant;
    private final PlantAnimationSpec animationSpec;
    private final PamPlayer pamPlayer;

    private static final String ICE_BLOCK_PAM =
        "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM";
    private static final String ICE_BLOCK_PREFERRED_CLIP = "idle";

    private static final String CHILL_OVERLAY_PAM =
        "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM";
    private static final String CHILL_OVERLAY_PREFERRED_CLIP = "idle";

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

        // گیاهِ کاملاً یخ‌زده دیگه انیمیشن/عمل نداره؛ فقط بلوک یخ روش می‌مونه.
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
            // اگه اکتورِ قبلی توی همین فریم رنگِ batch رو نیمه‌شفاف گذاشته
            // باشه (مثلاً پوششِ ساحل پست)، اینجا صریحاً برمی‌گردونیمش به
            // حالت عادی تا روی این گیاه اثر نذاره.
            batch.setColor(1f, 1f, 1f, parentAlpha);

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
            // یخ‌زدگیِ کامل: گیاه درون بلوک یخ نشون داده می‌شه (بلوک روی
            // خودِ گیاه رسم می‌شه، نه به‌جاش). مرحله‌ی آسیب با شفافیت
            // شبیه‌سازی می‌شه، نه تعویض کلیپ.
            drawIceBlock(batch, parentAlpha);
        } else if (plant.getFreezeLevel() > 0) {
            // سرمازدگیِ جزئی (هنوز کامل یخ نزده): یه لایه‌ی یخِ سبک روش،
            // با شفافیتِ متناسب با شدتِ سرما.
            drawChillOverlay(batch, parentAlpha);
        }
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
        drawWithAlpha(batch, ICE_BLOCK_PAM, resolvedIceBlockClip, alpha, parentAlpha);
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
        drawWithAlpha(batch, CHILL_OVERLAY_PAM, resolvedChillClip, alpha, parentAlpha);
    }

    private void drawWithAlpha(
        Batch batch,
        String pamPath,
        String clip,
        float alpha,
        float restoreAlpha
    ) {
        batch.setColor(1f, 1f, 1f, alpha);

        pamPlayer.draw(
            batch,
            pamPath,
            clip,
            frostStateTime,
            getX(),
            getY(),
            true
        );

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
