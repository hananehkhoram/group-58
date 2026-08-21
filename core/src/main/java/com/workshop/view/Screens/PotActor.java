package com.workshop.view.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GreenHouseData.Pot;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class PotActor extends Group {

    public interface Listener {
        void onBuy(int x, int y);
        void onPlant(int x, int y);
        void onCollect(int x, int y);
        void onFasterGrow(int x, int y);
    }

    private static final String LOCK_PAM = "768/INITIAL/UI/CHOOSER/SLOT_LOCK_SMALL/SLOT_LOCK_SMALL.PAM";
    private static final String POT_PAM = "768/INITIAL/ZEN_GARDEN/GROWING_PLANT_SLOT/GROWING_PLANT_SLOT.PAM";
    private static final String SPROUT_PAM = "768/INITIAL/ZEN_GARDEN/PLANT_ANIMATIONS/SPROUT/SPROUT.PAM";

    private final int gx, gy;
    private final Pot pot;
    private final PamPlayer pamPlayer;
    private final TextureBank textureBank;
    private final Skin skin;
    private final Listener listener;

    private final Label timeLabel;
    private final TextButton actionButton;

    private float animTime = 0f;
    private String plantPamPath = "";

    private boolean isUnlockingAnim = false;
    private float unlockAnimTime = 0f;
    private static final float UNLOCK_ANIM_DURATION = 0.6f;

    public PotActor(int gx, int gy, Pot pot, PamPlayer pamPlayer, TextureBank textureBank,
                    Skin skin, Listener listener) {
        this.gx = gx;
        this.gy = gy;
        this.pot = pot;
        this.pamPlayer = pamPlayer;
        this.textureBank = textureBank;
        this.skin = skin;
        this.listener = listener;

        setSize(90, 100);

        timeLabel = new Label("", skin);
        timeLabel.setPosition(0, 28);
        timeLabel.setWidth(90);
        timeLabel.setAlignment(Align.center);
        timeLabel.setFontScale(0.75f);
        addActor(timeLabel);

        actionButton = new TextButton("", skin, "green_small");
        actionButton.setSize(75, 24);
        actionButton.setPosition(7.5f, 0);
        actionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleClick();
            }
        });
        addActor(actionButton);

        refresh();
    }

    private void handleClick() {
        if (pot.isLocked()) {
            isUnlockingAnim = true;
            unlockAnimTime = 0f;
            listener.onBuy(gx, gy);
        } else if (pot.isEmpty()) {
            listener.onPlant(gx, gy);
        } else if (pot.isPlantReady()) {
            listener.onCollect(gx, gy);
        } else {
            listener.onFasterGrow(gx, gy);
        }
    }

    public void update(float delta) {
        act(delta);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!pot.isLocked() && !pot.isEmpty() && !pot.isPlantReady()) {
            double remaining = pot.getRemainingPlantedTime() - delta;
            if (remaining <= 0) {
                pot.setRemainingPlantedTime(0);
                pot.setPlantReady(true);
                refresh();
            } else {
                pot.setRemainingPlantedTime(remaining);
                timeLabel.setText(formatTime(remaining));
            }
        }

        animTime += delta;

        if (isUnlockingAnim) {
            unlockAnimTime += delta;
            if (unlockAnimTime >= UNLOCK_ANIM_DURATION) {
                isUnlockingAnim = false;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (textureBank != null) textureBank.update();

        float drawX = getX() + getWidth() / 2f;
        float drawY = getY() + 50f;

        // ۱. حالت قفل یا در حال باز شدن قفل
        if (pot.isLocked() || isUnlockingAnim) {
            String clip = isUnlockingAnim ? "open" : "idle";
            float time = isUnlockingAnim ? unlockAnimTime : animTime;
            renderPamScaled(batch, LOCK_PAM, clip, time, drawX, drawY, 0.25f);
            return;
        }

        // ۲. رندر گلدان (در صورت آنلاک بودن)
        String potClip = (!pot.isEmpty() && !pot.isPlantReady()) ? "boost" : "idle";
        renderPamScaled(batch, POT_PAM, potClip, animTime, drawX, drawY, 0.22f);

        // ۳. رندر جوانه یا گیاه بالغ
        if (!pot.isEmpty()) {
            if (!pot.isPlantReady()) {
                // رندر جوانه SPROUT
                renderPamScaled(batch, SPROUT_PAM, "idle", animTime, drawX, drawY + 8f, 0.20f);
            } else {
                // رندر گیاه اصلی
                try {
                    renderPamScaled(batch, plantPamPath, "idle", animTime, drawX, drawY + 10f, 0.18f);
                } catch (Exception e) {
                    TextureRegion reg = Textures.regionOrNull(fallbackRegionName());
                    if (reg != null) {
                        batch.setColor(Color.WHITE);
                        batch.draw(reg, drawX - 20, drawY, 40, 40);
                    }
                }
            }
        }
    }

    private void renderPamScaled(Batch batch, String path, String clip, float time, float x, float y, float scale) {
        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4(oldTransform);

        transform.translate(x, y, 0);
        transform.scale(scale, scale, 1f);
        transform.translate(-x, -y, 0);
        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(batch, path, clip, time, x, y, true);
        } catch (Exception ignored) {}

        batch.setTransformMatrix(oldTransform);
    }

    public void refresh() {
        if (pot.isLocked()) {
            timeLabel.setText("");
            actionButton.setText("Buy");
            return;
        }

        if (pot.isEmpty()) {
            timeLabel.setText("");
            actionButton.setText("Plant");
            return;
        }

        String plantName = (pot.isMarigold() || pot.getPlantType() == null)
            ? "Marigold" : pot.getPlantType().getName();
        String rawName = plantName.toUpperCase().replace(" ", "").replace("-", "");
        if (rawName.equalsIgnoreCase("PRIMALPOTATOMINE")) rawName = "PRIMAL_POTATOMINE";
        plantPamPath = "PLANT/" + rawName + "/" + rawName + ".PAM";

        if (pot.isPlantReady()) {
            timeLabel.setText("Ready!");
            timeLabel.setColor(Color.GREEN);
            actionButton.setText("Collect");
        } else {
            timeLabel.setText(formatTime(pot.getRemainingPlantedTime()));
            timeLabel.setColor(Color.WHITE);
            actionButton.setText("Speed up");
        }
    }

    private String fallbackRegionName() {
        String plantName = (pot.isMarigold() || pot.getPlantType() == null)
            ? "Marigold" : pot.getPlantType().getName();
        return "PLANT_" + plantName.toUpperCase().replace(" ", "_");
    }

    private String formatTime(double seconds) {
        int s = (int) Math.max(0, seconds);
        return String.format("%02d:%02d", s / 60, s % 60);
    }
}
