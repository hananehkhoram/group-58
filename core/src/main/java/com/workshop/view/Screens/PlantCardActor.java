package com.workshop.view.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.workshop.controller.repository.Textures;
import com.workshop.model.plants.Plant;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class PlantCardActor extends Table {

    public enum Mode { GRID, SLOT }

    public interface OnClick {
        void clicked(PlantCardActor card);
    }

    private final Plant plant;
    private final PamPlayer pamPlayer;
    private final TextureBank textureBank;
    private final Skin skin;
    private final Mode mode;

    private boolean isSelected = false;
    private boolean isFocused = false;
    private boolean isBoosted = false;
    private float animTime = 0f;

    private OnClick onClick;

    public PlantCardActor(Plant plant, PamPlayer pamPlayer, TextureBank textureBank, Skin skin, Mode mode) {
        this.plant = plant;
        this.pamPlayer = pamPlayer;
        this.textureBank = textureBank;
        this.skin = skin;
        this.mode = mode;

        setTouchable(Touchable.enabled);
        rebuildUI();
    }

    public void updateAnimation(float delta) {
        this.animTime += delta;
    }

    public void rebuildUI() {
        clearChildren();
        top();

        if (skin.has("SeedPacketBorder", Drawable.class)) {
            setBackground(skin.getDrawable("SeedPacketBorder"));
        } else if (skin.has("PlantAlmanacBorder", Drawable.class)) {
            setBackground(skin.getDrawable("PlantAlmanacBorder"));
        }

        Table pamContainer = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (textureBank != null) textureBank.update();

                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + (mode == Mode.SLOT ? 2f : 6f);

                if (isSelected) {
                    batch.setColor(0.3f, 0.3f, 0.3f, 0.6f);
                } else {
                    batch.setColor(Color.WHITE);
                }

                String rawName = plant.getName().toUpperCase().replace(" ", "").replace("-", "");
                if (rawName.equalsIgnoreCase("PRIMALPOTATOMINE")) rawName = "PRIMAL_POTATOMINE";

                try {
                    pamPlayer.draw(batch, "PLANT/" + rawName + "/" + rawName + ".PAM", "idle", animTime, drawX, drawY, true);
                } catch (Exception e) {
                    TextureRegion reg = Textures.regionOrNull("PLANT_" + plant.getName().toUpperCase().replace(" ", "_"));
                    if (reg != null) batch.draw(reg, drawX - 25, drawY, 50, 50);
                }
                batch.setColor(Color.WHITE);
            }
        };

        float pamHeight = (mode == Mode.SLOT) ? 50f : 65f;
        add(pamContainer).size(90, pamHeight).padTop(4).row();

        if (mode == Mode.GRID) {
            Table footerTable = new Table();

            Label lvlLbl = createSafeLabel("LVL " + plant.getLevel(), "big");
            lvlLbl.setFontScale(0.32f);
            footerTable.add(lvlLbl).left().expandX();

            Label sunLbl = createSafeLabel(String.valueOf(plant.getSunCost()), "big");
            sunLbl.setFontScale(0.45f);
            sunLbl.setColor(Color.YELLOW);
            footerTable.add(sunLbl).right();

            add(footerTable).fillX().padLeft(6).padRight(6).padBottom(4).row();
        } else {
            Label sunLbl = createSafeLabel(String.valueOf(plant.getSunCost()), "big");
            sunLbl.setFontScale(0.42f);
            sunLbl.setColor(Color.YELLOW);
            add(sunLbl).padBottom(2).row();
        }

        if (isBoosted) {
            setColor(1f, 0.9f, 0.4f, 1f);
        } else if (isFocused) {
            setColor(0.5f, 1f, 0.5f, 1f);
        } else {
            setColor(Color.WHITE);
        }

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) onClick.clicked(PlantCardActor.this);
            }
        });
    }

    public Plant getPlant() { return plant; }
    public void setSelected(boolean selected) { this.isSelected = selected; rebuildUI(); }
    public void setFocused(boolean focused) { this.isFocused = focused; rebuildUI(); }
    public void setBoosted(boolean boosted) { this.isBoosted = boosted; rebuildUI(); }
    public void setOnClick(OnClick onClick) { this.onClick = onClick; }

    private Label createSafeLabel(String text, String styleName) {
        return skin.has(styleName, Label.LabelStyle.class) ? new Label(text, skin, styleName) : new Label(text, skin);
    }
}
