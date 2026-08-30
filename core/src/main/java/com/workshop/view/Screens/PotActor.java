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
        timeLabel.setPosition(0, 0);
        timeLabel.setWidth(90);
        timeLabel.setAlignment(Align.center);
        timeLabel.setFontScale(0.65f);

        actionButton = new TextButton("", skin, "green_small");
        actionButton.setSize(92, 26);
        actionButton.setPosition(-1f, -28);
        actionButton.getLabel().setFontScale(0.8f);

        actionButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleClick();
            }
        });

        addActor(actionButton);
        addActor(timeLabel);

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
            double remainingHours = pot.getRemainingPlantedTime() - (delta / 3600.0);
            if (remainingHours <= 0) {
                pot.setRemainingPlantedTime(0);
                pot.setPlantReady(true);
                refresh();
            } else {
                pot.setRemainingPlantedTime(remainingHours);
                timeLabel.setText(formatTime(remainingHours * 3600.0));
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
        if (textureBank != null) textureBank.update();

        float drawX = getX() + getWidth() / 2f;
        float drawY = getY() + 8f;

        String potClip = (!pot.isEmpty() && !pot.isPlantReady()) ? "boost" : "idle";
        renderPamScaled(batch, POT_PAM, potClip, animTime, drawX, drawY, 0.38f);

        if (pot.isLocked() || isUnlockingAnim) {
            String clip = isUnlockingAnim ? "open" : "idle";
            float time = isUnlockingAnim ? unlockAnimTime : animTime;
            renderPamScaled(batch, LOCK_PAM, clip, time, drawX, drawY + 15f, 0.35f);
        } else if (!pot.isEmpty()) {
            float plantX = drawX - 18f;

            if (!pot.isPlantReady()) {
                renderPamScaled(batch, SPROUT_PAM, "idle", animTime, plantX, drawY + 25f, 0.35f);
            } else {
                try {
                    renderPamScaled(batch, plantPamPath, "idle", animTime, plantX, drawY + 30f, 0.32f);
                } catch (Exception e) {
                    TextureRegion reg = Textures.regionOrNull(fallbackRegionName());
                    if (reg != null) {
                        batch.setColor(Color.WHITE);
                        batch.draw(reg, plantX - 25, drawY + 15, 50, 50);
                    }
                }
            }
        }

        super.draw(batch, parentAlpha);
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
        timeLabel.toFront();

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
            timeLabel.setText(formatTime(pot.getRemainingPlantedTime() * 3600.0));
            timeLabel.setColor(Color.WHITE);

            int gemsNeeded = (int) Math.ceil(pot.getRemainingPlantedTime());
            actionButton.setText("Speed " + gemsNeeded);
        }
    }

    private String fallbackRegionName() {
        String plantName = (pot.isMarigold() || pot.getPlantType() == null)
            ? "Marigold" : pot.getPlantType().getName();
        return "PLANT_" + plantName.toUpperCase().replace(" ", "_");
    }

    private String formatTime(double seconds) {
        int totalSecs = (int) Math.max(0, seconds);
        int hours = totalSecs / 3600;
        int mins = (totalSecs % 3600) / 60;
        int secs = totalSecs % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, mins, secs);
        }
        return String.format("%02d:%02d", mins, secs);
    }
}
