package com.workshop.view.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.gameplay.ZombieAnimationResolver;
import com.workshop.view.gameplay.ZombieAnimationSpec;
import pvz.libpvz.pam.PamPlayer;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;

public final class ZombieCardActor extends Table {

    public interface OnClick {
        void clicked(ZombieCardActor card);
    }

    private static NinePatchDrawable cardBackground;
    private static NinePatchDrawable cardBackgroundFocused;
    private static NinePatchDrawable cooldownBackground;

    private final Zombie zombie;
    private final String zombieType;
    private final int cost;
    private final PamPlayer pamPlayer;
    private final Skin skin;
    private final ZombieAnimationSpec animationSpec;
    private Table cooldownOverlay;
    private Label cooldownLabel;


    private boolean focused;
    private float animationTime;
    private double cooldownRemainingSeconds;
    private OnClick onClick;

    public ZombieCardActor(
        Zombie zombie,
        String zombieType,
        int cost,
        String seasonName,
        PamPlayer pamPlayer,
        Skin skin
    ) {
        this.zombie = zombie;
        this.zombieType = zombieType;
        this.cost = cost;
        this.pamPlayer = pamPlayer;
        this.skin = skin;
        this.animationSpec = ZombieAnimationResolver.shared().resolve(
            zombie,
            seasonName
        );

        setTouchable(Touchable.enabled);
        rebuildUi();
    }

    public void updateAnimation(float delta) {
        animationTime += delta;
    }

    public String getZombieType() {
        return zombieType;
    }

    public Zombie getZombie() {
        return zombie;
    }

    public int getCost() {
        return cost;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        rebuildUi();
    }

    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    private void rebuildUi() {
        clearChildren();
        clearListeners();
        align(Align.center);
        setBackground(
            focused
                ? getCardBackgroundFocused()
                : getCardBackground()
        );

        Actor preview = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                drawZombie(
                    batch,
                    getX() + getWidth() / 2f,
                    getY() + getHeight() / 2f - 4f
                );
            }
        };

        Table row = new Table();
        row.setFillParent(false);

        row.add(preview)
            .size(58f, 58f)
            .left();

        row.add()
            .expandX();

        row.add(buildCostGroup())
            .right()
            .padRight(4f);

        Stack cardStack = new Stack();

        cardStack.add(row);

        cooldownOverlay = new Table();

        cooldownOverlay.setBackground(
            getCooldownBackground()
        );

        cooldownOverlay.setTouchable(
            Touchable.disabled
        );

        cooldownLabel = new Label("", skin);

        cooldownLabel.setAlignment(
            Align.center
        );

        cooldownLabel.setFontScale(0.62f);

        cooldownLabel.setColor(
            Color.WHITE
        );

        cooldownOverlay.add(
            cooldownLabel
        ).center();

        Container<Table> overlayContainer =
            new Container<>(cooldownOverlay);

        overlayContainer.pad(3f);
        overlayContainer.fill();
        overlayContainer.setTouchable(
            Touchable.disabled
        );

        cardStack.add(overlayContainer);

        add(cardStack).grow();

        updateCooldownVisual();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (onClick != null) {
                    onClick.clicked(ZombieCardActor.this);
                }
            }

            @Override
            public void enter(
                InputEvent event,
                float x,
                float y,
                int pointer,
                com.badlogic.gdx.scenes.scene2d.Actor fromActor
            ) {
                if (pointer == -1) {
                    setBackground(getCardBackgroundFocused());
                }
            }

            @Override
            public void exit(
                InputEvent event,
                float x,
                float y,
                int pointer,
                com.badlogic.gdx.scenes.scene2d.Actor toActor
            ) {
                if (pointer == -1 && !focused) {
                    setBackground(getCardBackground());
                }
            }
        });
    }

    private void drawZombie(
        Batch batch,
        float drawX,
        float drawY
    ) {
        if (animationSpec == null
            || animationSpec.getIdleClip() == null) {
            return;
        }

        String pamPath = animationSpec.getPamPath();
        String idleClip = animationSpec.getIdleClip();

        Rectangle bounds;
        try {
            bounds = pamPlayer.bounds(pamPath, idleClip);
        } catch (Throwable ignored) {
            return;
        }

        float scale = 0.25f;
        if (bounds != null
            && bounds.width > 0f
            && bounds.height > 0f) {
            scale = Math.min(
                52f / bounds.width,
                52f / bounds.height
            );
        }

        Matrix4 oldTransform = batch.getTransformMatrix().cpy();
        Matrix4 transform = new Matrix4(oldTransform);
        transform.translate(drawX, drawY, 0f);
        transform.scale(scale, scale, 1f);
        transform.translate(-drawX, -drawY, 0f);
        batch.setTransformMatrix(transform);

        try {
            pamPlayer.draw(
                batch,
                pamPath,
                idleClip,
                animationTime,
                drawX,
                drawY,
                true
            );
        } catch (Throwable ignored) {
        } finally {
            batch.setTransformMatrix(oldTransform);
        }
    }

    private Table buildCostGroup() {
        Table group = new Table();

        if (skin.has("image_ui_hud_ingame_sun", Drawable.class)) {
            Image sunIcon = new Image(
                skin.getDrawable("image_ui_hud_ingame_sun")
            );
            group.add(sunIcon).size(22f).padRight(2f);
        }

        Label costLabel = new Label(String.valueOf(cost), skin);
        costLabel.setFontScale(0.5f);
        costLabel.setColor(1f, 0.92f, 0.55f, 1f);
        group.add(costLabel);

        return group;
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
                new Color(0.36f, 0.20f, 0.16f, 0.82f),
                new Color(1f, 0.55f, 0.30f, 1f)
            );
        }
        return cardBackgroundFocused;
    }

    private static NinePatchDrawable buildRoundedBackground(
        Color fill,
        Color border
    ) {
        int size = 32;
        int radius = 8;

        Pixmap pixmap = new Pixmap(
            size,
            size,
            Pixmap.Format.RGBA8888
        );
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        pixmap.setColor(border);
        fillRoundedRect(pixmap, 0, 0, size, size, radius);

        pixmap.setColor(fill);
        fillRoundedRect(
            pixmap,
            2,
            2,
            size - 4,
            size - 4,
            radius - 2
        );

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        NinePatch patch = new NinePatch(
            texture,
            radius,
            radius,
            radius,
            radius
        );
        return new NinePatchDrawable(patch);
    }

    private static NinePatchDrawable getCooldownBackground() {
        if (cooldownBackground == null) {
            cooldownBackground =
                buildRoundedBackground(
                    new Color(0f, 0f, 0f, 0.62f),
                    new Color(0f, 0f, 0f, 0f)
                );
        }

        return cooldownBackground;
    }

    private static void fillRoundedRect(
        Pixmap pixmap,
        int x,
        int y,
        int width,
        int height,
        int radius
    ) {
        pixmap.fillRectangle(
            x + radius,
            y,
            width - 2 * radius,
            height
        );
        pixmap.fillRectangle(
            x,
            y + radius,
            width,
            height - 2 * radius
        );
        pixmap.fillCircle(x + radius, y + radius, radius);
        pixmap.fillCircle(
            x + width - radius - 1,
            y + radius,
            radius
        );
        pixmap.fillCircle(
            x + radius,
            y + height - radius - 1,
            radius
        );
        pixmap.fillCircle(
            x + width - radius - 1,
            y + height - radius - 1,
            radius
        );
    }

    public void setCooldownRemaining(
        double seconds
    ) {
        cooldownRemainingSeconds =
            Math.max(0, seconds);

        updateCooldownVisual();
    }

    private void updateCooldownVisual() {
        if (cooldownOverlay == null
            || cooldownLabel == null) {
            return;
        }

        if (cooldownRemainingSeconds <= 0) {
            cooldownOverlay.setVisible(false);
            cooldownLabel.setText("");
            return;
        }

        cooldownOverlay.setVisible(true);

        int remaining =
            (int) Math.ceil(
                cooldownRemainingSeconds
            );

        cooldownLabel.setText(
            remaining + "s"
        );
    }
}
