package com.workshop.view.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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

    private final int gx, gy;
    private final Pot pot;
    private final PamPlayer pamPlayer;
    private final TextureBank textureBank;
    private final Skin skin;
    private final Listener listener;

    private final Image potImage;
    private final Widget plantWidget;
    private final Label timeLabel;
    private final TextButton actionButton;

    private float animTime = 0f;
    private boolean hasPlantToDraw = false;
    private String pamPath = "";

    public PotActor(int gx, int gy, Pot pot, PamPlayer pamPlayer, TextureBank textureBank,
                    Skin skin, Listener listener) {
        this.gx = gx;
        this.gy = gy;
        this.pot = pot;
        this.pamPlayer = pamPlayer;
        this.textureBank = textureBank;
        this.skin = skin;
        this.listener = listener;

        setSize(120, 140);

        potImage = new Image();
        potImage.setSize(120, 60);
        addActor(potImage);

        plantWidget = new Widget() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (!hasPlantToDraw) return;
                if (textureBank != null) textureBank.update();

                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + 6f;

                batch.setColor(Color.WHITE);
                try {
                    pamPlayer.draw(batch, pamPath, "idle", animTime, drawX, drawY, true);
                } catch (Exception e) {
                    TextureRegion reg = Textures.regionOrNull(fallbackRegionName());
                    if (reg != null) batch.draw(reg, drawX - 25, drawY, 50, 50);
                }
            }
        };
        plantWidget.setSize(90, 90);
        plantWidget.setPosition(15, 40);
        addActor(plantWidget);

        timeLabel = new Label("", skin);
        timeLabel.setPosition(0, 110);
        timeLabel.setWidth(120);
        timeLabel.setAlignment(Align.center);
        addActor(timeLabel);

        actionButton = new TextButton("", skin);
        actionButton.setSize(90, 30);
        actionButton.setPosition(15, -5);
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
        if (pot.isLocked()) listener.onBuy(gx, gy);
        else if (pot.isEmpty()) listener.onPlant(gx, gy);
        else if (pot.isPlantReady()) listener.onCollect(gx, gy);
        else listener.onFasterGrow(gx, gy);
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

        if (hasPlantToDraw) {
            animTime += delta;
        }
    }

    /** بعد از plant/collect/buy از بیرون صدا زده می‌شود تا ظاهر گلدون به‌روز شود. */
    public void refresh() {
        if (pot.isLocked()) {
            setRegion(potImage, "IMAGE_UI_GREENHOUSE_POT_LOCKED");
            hasPlantToDraw = false;
            timeLabel.setText("");
            actionButton.setText("Buy");
            return;
        }
        setRegion(potImage, "IMAGE_UI_GREENHOUSE_POT_OPEN");

        if (pot.isEmpty()) {
            hasPlantToDraw = false;
            timeLabel.setText("");
            actionButton.setText("Plant");
            return;
        }

        String plantName = (pot.isMarigold() || pot.getPlantType() == null)
            ? "Marigold" : pot.getPlantType().getName();
        String rawName = plantName.toUpperCase().replace(" ", "").replace("-", "");
        if (rawName.equalsIgnoreCase("PRIMALPOTATOMINE")) rawName = "PRIMAL_POTATOMINE";
        pamPath = "PLANT/" + rawName + "/" + rawName + ".PAM";
        hasPlantToDraw = true;
        animTime = 0f;

        if (pot.isPlantReady()) {
            timeLabel.setText("Ready!");
            actionButton.setText("Collect");
        } else {
            timeLabel.setText(formatTime(pot.getRemainingPlantedTime()));
            actionButton.setText("Speed up (Gems)");
        }
    }

    private String fallbackRegionName() {
        String plantName = (pot.isMarigold() || pot.getPlantType() == null)
            ? "Marigold" : pot.getPlantType().getName();
        return "PLANT_" + plantName.toUpperCase().replace(" ", "_");
    }

    private void setRegion(Image image, String regionName) {
        TextureRegion r = Textures.regionOrNull(regionName);
        if (r != null) {
            image.setDrawable(new TextureRegionDrawable(r));
        }
    }

    private String formatTime(double seconds) {
        int s = (int) Math.max(0, seconds);
        return String.format("%02d:%02d", s / 60, s % 60);
    }
}
