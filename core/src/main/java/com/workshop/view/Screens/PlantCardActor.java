package com.workshop.view.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.workshop.controller.repository.Textures;
import com.workshop.model.plants.Plant;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class PlantCardActor extends Table {

    public enum Mode { GRID, SLOT, CONVEYOR }

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

    private double cooldownRemainingSeconds = 0;
    private Label cooldownLabel;
    private Table cooldownOverlay;
    private static NinePatchDrawable cooldownBackground;

    private OnClick onClick;

    private static NinePatchDrawable cardBackground;
    private static NinePatchDrawable cardBackgroundFocused;
    private static NinePatchDrawable cardBackgroundBoosted;

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

    private static NinePatchDrawable buildRoundedBackground(Color fill, Color border) {
        int size = 32;
        int radius = 8;

        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        pixmap.setColor(border);
        fillRoundedRect(pixmap, 0, 0, size, size, radius);

        pixmap.setColor(fill);
        fillRoundedRect(pixmap, 2, 2, size - 4, size - 4, radius - 2);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        NinePatch patch = new NinePatch(texture, radius, radius, radius, radius);
        return new NinePatchDrawable(patch);
    }

    private static void fillRoundedRect(Pixmap pixmap, int x, int y, int w, int h, int r) {
        pixmap.fillRectangle(x + r, y, w - 2 * r, h);
        pixmap.fillRectangle(x, y + r, w, h - 2 * r);
        pixmap.fillCircle(x + r, y + r, r);
        pixmap.fillCircle(x + w - r - 1, y + r, r);
        pixmap.fillCircle(x + r, y + h - r - 1, r);
        pixmap.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    private static NinePatchDrawable getCardBackground() {
        if (cardBackground == null) {
            cardBackground = buildRoundedBackground(
                new Color(0.22f, 0.16f, 0.08f, 0.55f),
                new Color(0.85f, 0.72f, 0.4f, 0.85f)
            );
        }
        return cardBackground;
    }

    private static NinePatchDrawable getCardBackgroundFocused() {
        if (cardBackgroundFocused == null) {
            cardBackgroundFocused = buildRoundedBackground(
                new Color(0.3f, 0.45f, 0.14f, 0.75f),
                new Color(0.55f, 1f, 0.45f, 1f)
            );
        }
        return cardBackgroundFocused;
    }

    private static NinePatchDrawable getCardBackgroundBoosted() {
        if (cardBackgroundBoosted == null) {
            cardBackgroundBoosted = buildRoundedBackground(
                new Color(0.82f, 0.62f, 0.08f, 0.90f),
                new Color(1f, 0.92f, 0.4f, 1f)
            );
        }
        return cardBackgroundBoosted;
    }

    private static NinePatchDrawable getCooldownBackground() {
        if (cooldownBackground == null) {
            cooldownBackground = buildRoundedBackground(
                new Color(0f, 0f, 0f, 0.30f),
                new Color(0f, 0f, 0f, 0f)
            );
        }
        return cooldownBackground;
    }

    public void rebuildUI() {
        clearChildren();
        clearListeners();
        align(Align.center);

        Table pamContainer = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (textureBank != null) textureBank.update();

                float centerX = getX() + getWidth() / 2f;
                float centerY = getY() + getHeight() / 2f;

                // رندر انیمیشن پس‌زمینه بوست، با مقیاس مناسب برای فیت شدن در کادر کارت
                if (isBoosted && pamPlayer != null) {
                    Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
                    Matrix4 transform = new Matrix4(oldMatrix);
                    transform.translate(centerX, centerY, 0);

                    // اسکیل بهینه‌شده متناسب با ابعاد کادر
                    float boostScale = (mode == Mode.SLOT) ? 0.16f : 0.22f;
                    transform.scale(boostScale, boostScale, 1f);

                    transform.translate(-centerX, -centerY, 0);
                    batch.setTransformMatrix(transform);

                    try {
                        pamPlayer.draw(batch, "768/INITIAL/ZEN_GARDEN/BOOSTCARD_ANIM/BOOSTCARD_ANIM.PAM", "animation", animTime, centerX, centerY, true);
                    } catch (Exception ignored) {
                    }

                    batch.setTransformMatrix(oldMatrix);
                }

                batch.setColor(Color.WHITE);

                boolean drawn;
                if (mode == Mode.CONVEYOR) {
                    drawn = drawPlantPam(batch, centerX, centerY, 0.28f);
                } else {
                    float drawX = centerX + (mode == Mode.SLOT ? -110f : 15f) + 100;
                    float drawY = centerY + (mode == Mode.SLOT ? -5f : -20f);
                    float plantScale = mode == Mode.SLOT ? 0.45f : 1f;

                    drawn = drawPlantPam(batch, drawX, drawY, plantScale);
                }

                if (!drawn) {
                    try {
                        TextureRegion reg = Textures.regionOrNull("PLANT_" + plant.getName().toUpperCase().replace(" ", "_"));
                        if (reg != null) batch.draw(reg, getX() - 25, getY() - 25, 50, 50);
                    } catch (Exception ignored) {
                    }
                }

                batch.setColor(Color.WHITE);
            }
        };

        NinePatchDrawable background;
        if (isBoosted) {
            background = getCardBackgroundBoosted();
        } else if (isFocused) {
            background = getCardBackgroundFocused();
        } else {
            background = getCardBackground();
        }
        setBackground(background);

        cooldownLabel = createSafeLabel("", "big");
        cooldownLabel.setFontScale(0.45f);
        cooldownLabel.setAlignment(Align.center);
        cooldownLabel.setColor(Color.WHITE);

        if (mode == Mode.CONVEYOR) {
            add(pamContainer).grow();
        } else if (mode == Mode.GRID) {
            Table footerTable = new Table();
            footerTable.align(Align.center);

            Label lvlLbl = createSafeLabel("LVL " + plant.getLevel(), "big");
            lvlLbl.setFontScale(0.32f);
            if (isBoosted) {
                lvlLbl.setColor(new Color(0.25f, 0.15f, 0.0f, 1f));
            }

            footerTable.add(lvlLbl).left().expandX();
            footerTable.add(buildSunCostGroup()).right();

            add(pamContainer).size(90f, 65f).center().row();
            add(footerTable).fillX().padLeft(6f).padRight(6f).padBottom(4f).row();
        } else {
            Table slotRow = new Table();
            slotRow.align(Align.center);

            slotRow.add(pamContainer).size(62f, 58f).center();
            slotRow.add().expandX();
            slotRow.add(buildSunCostGroup()).padLeft(-10f).right();

            Stack cardStack = new Stack();
            cardStack.add(slotRow);

            cooldownOverlay = new Table();
            cooldownOverlay.setBackground(getCooldownBackground());
            cooldownOverlay.setTouchable(Touchable.disabled);

            cooldownLabel = createSafeLabel("", "big");
            cooldownLabel.setAlignment(Align.center);
            cooldownLabel.setFontScale(0.62f);
            cooldownLabel.setColor(Color.WHITE);

            cooldownOverlay.add(cooldownLabel).center();
            cooldownOverlay.setVisible(false);

            Container<Table> overlayContainer = new Container<>(cooldownOverlay);
            overlayContainer.pad(3f);
            overlayContainer.fill();

            cardStack.add(overlayContainer);
            add(cardStack).grow();
        }

        setColor(Color.WHITE);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) onClick.clicked(PlantCardActor.this);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1) {
                    setBackground(isBoosted ? getCardBackgroundBoosted() : getCardBackgroundFocused());
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                if (pointer == -1 && !isFocused) {
                    setBackground(isBoosted ? getCardBackgroundBoosted() : getCardBackground());
                }
            }
        });
    }

    private boolean drawPlantPam(Batch batch, float x, float y, float scale) {
        String rawName = plant.getName().toUpperCase().replace(" ", "").replace("-", "");
        if (rawName.equalsIgnoreCase("PRIMALPOTATOMINE")) {
            rawName = "PRIMAL_POTATOMINE";
        }

        String pamPath = "PLANT/" + rawName + "/" + rawName + ".PAM";
        String[] clips = {"idle","idle_stage1_", "idle_stage1", "loop", "animation", "anim", "attack1", "idle1_1", "stage1_spawn"};

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        if (scale != 1f) {
            Matrix4 transform = new Matrix4(oldTransform);
            transform.translate(x, y, 0);
            transform.scale(scale, scale, 1f);
            transform.translate(-x, -y, 0);
            batch.setTransformMatrix(transform);
        }

        boolean drawn = false;
        for (String clip : clips) {
            try {
                pamPlayer.draw(batch, pamPath, clip, animTime, x, y, true);
                drawn = true;
                break;
            } catch (Exception ignored) {
            }
        }

        batch.setTransformMatrix(oldTransform);
        return drawn;
    }

    private Table buildSunCostGroup() {
        Table group = new Table();

        if (skin.has("image_ui_hud_ingame_sun", Drawable.class)) {
            Image sunIcon = new Image(skin.getDrawable("image_ui_hud_ingame_sun"));
            group.add(sunIcon).size(mode == Mode.SLOT ? 22f : 18f).padRight(2f);
        }

        Label sunLbl = createSafeLabel(String.valueOf(plant.getSunCost()), "big");
        sunLbl.setFontScale(mode == Mode.SLOT ? 0.3f : 0.4f);

        if (isBoosted) {
            sunLbl.setColor(new Color(0.2f, 0.1f, 0.0f, 1f));
        } else {
            sunLbl.setColor(1f, 0.92f, 0.55f, 1f);
        }

        group.add(sunLbl);

        return group;
    }

    private void updateCooldownLabel() {
        if (cooldownLabel == null) {
            return;
        }

        if (cooldownRemainingSeconds <= 0) {
            cooldownLabel.setText("");
            return;
        }

        int seconds = (int) Math.ceil(cooldownRemainingSeconds);
        cooldownLabel.setText(seconds + "s");
    }

    public void setCooldownRemaining(double seconds) {
        cooldownRemainingSeconds = Math.max(0, seconds);

        if (cooldownOverlay == null || cooldownLabel == null) {
            return;
        }

        if (cooldownRemainingSeconds <= 0) {
            cooldownOverlay.setVisible(false);
            cooldownLabel.setText("");
            return;
        }

        cooldownOverlay.setVisible(true);
        int remaining = (int) Math.ceil(cooldownRemainingSeconds);
        cooldownLabel.setText(remaining + "s");
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
