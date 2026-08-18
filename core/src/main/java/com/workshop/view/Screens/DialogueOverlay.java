package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.workshop.controller.repository.Audio;
import com.workshop.controller.repository.Textures;
import com.workshop.model.level.DialogueLine;

import java.util.ArrayList;
import java.util.List;

/**
 * جعبه‌ی دیالوگِ بین شخصیت‌ها (مثلاً Crazy Dave و Penny) در ابتدا یا انتهای
 * یک مرحله.
 * <p>
 * با هر تاچ/کلیک روی صفحه یک خط جلو می‌رود. بعد از آخرین خط، جعبه با یک
 * fade-out «صحنه را ترک می‌کند» و {@code onComplete} صدا زده می‌شود.
 */
public class DialogueOverlay {

    private static final float PORTRAIT_SIZE = 150f;
    private static final float BOX_WIDTH = 860f;
    private static final float TEXT_WIDTH = 620f;

    private final Stage stage;
    private final List<DialogueLine> lines;
    private final Runnable onComplete;

    private final Table overlay;
    private final Table portraitSlot;
    private final Label nameLabel;
    private final Label textLabel;

    private final List<Texture> loadedPortraits = new ArrayList<>();

    private int index = 0;

    public DialogueOverlay(
        Stage stage,
        Skin skin,
        List<DialogueLine> lines,
        Runnable onComplete
    ) {
        this.stage = stage;
        this.lines = lines;
        this.onComplete = onComplete;

        overlay = new Table();
        overlay.setFillParent(true);
        overlay.setTouchable(Touchable.enabled);
        overlay.bottom();
        overlay.pad(40);

        Table box = new Table();
        box.pad(22, 26, 22, 26);
        box.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        portraitSlot = new Table();

        nameLabel = new Label("", skin, "big");
        nameLabel.setColor(Color.valueOf("5B3A29"));
        nameLabel.setFontScale(1.1f);

        textLabel = new Label("", skin, "big");
        textLabel.setColor(Color.valueOf("3B2A1A"));
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.topLeft);

        Label continueHint = new Label("[click to continue]", skin, "big");
        continueHint.setColor(Color.valueOf("8A7050"));
        continueHint.setFontScale(0.65f);

        Table textColumn = new Table();
        textColumn.add(nameLabel).left().padBottom(8).row();
        textColumn.add(textLabel).width(TEXT_WIDTH).left().row();
        textColumn.add(continueHint).right().padTop(12);

        box.add(portraitSlot).size(PORTRAIT_SIZE, PORTRAIT_SIZE + 20).padRight(24);
        box.add(textColumn);

        overlay.add(box).width(BOX_WIDTH);

        overlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advance();
            }
        });

        overlay.getColor().a = 0f;
    }

    public void show() {
        if (lines == null || lines.isEmpty()) {
            finish();
            return;
        }

        stage.addActor(overlay);
        overlay.addAction(Actions.fadeIn(0.25f));
        renderLine(0);
    }

    private void advance() {
        index++;

        if (index >= lines.size()) {
            leave();
        } else {
            renderLine(index);
        }
    }

    private void renderLine(int i) {
        DialogueLine line = lines.get(i);

        nameLabel.setText(line.getSpeakerName());
        textLabel.setText(line.getText());

        // "موقع حرف زدن دیو" — only while Dave is the one talking.
        boolean isDave = line.getSpeakerName() != null
            && line.getSpeakerName().toLowerCase().contains("dave");

        if (isDave) {
            Audio.playMusic("music/crazydaveextralong1", true);
        } else {
            Audio.stopMusic();
        }

        portraitSlot.clearChildren();

        String pamPath = line.getPortraitPamPath();
        String pamClip = line.getPortraitPamClip();
        String resourceId = line.getPortraitResourceId();
        String rawPath = line.getPortraitPath();

        if (pamPath != null && pamClip != null) {
            portraitSlot.add(
                new PamPortraitWidget(pamPath, pamClip, PORTRAIT_SIZE, PORTRAIT_SIZE)
            ).size(PORTRAIT_SIZE, PORTRAIT_SIZE);

        } else if (resourceId != null) {
            TextureRegion region = Textures.regionOrNull(resourceId);

            if (region != null) {
                Image image = new Image(region);
                image.setScaling(Scaling.fit);
                portraitSlot.add(image).size(PORTRAIT_SIZE, PORTRAIT_SIZE);
            }

        } else if (rawPath != null && Gdx.files.internal(rawPath).exists()) {
            Texture texture = new Texture(Gdx.files.internal(rawPath));
            loadedPortraits.add(texture);

            Image image = new Image(texture);
            image.setScaling(Scaling.fit);
            portraitSlot.add(image).size(PORTRAIT_SIZE, PORTRAIT_SIZE);
        }
    }

    private void leave() {
        overlay.addAction(
            Actions.sequence(
                Actions.fadeOut(0.35f),
                Actions.run(this::finish)
            )
        );
    }

    private void finish() {
        overlay.remove();

        Audio.stopMusic();

        for (Texture texture : loadedPortraits) {
            texture.dispose();
        }

        loadedPortraits.clear();

        if (onComplete != null) {
            onComplete.run();
        }
    }

    /** یک پرتره‌ی انیمیشنی (PAM) که داخل جعبه‌ی دیالوگ به‌اندازه‌ی مناسب جا می‌شود. */
    private static class PamPortraitWidget extends Actor {

        private final String pamPath;
        private final String requestedClip;
        private final float targetWidth;
        private final float targetHeight;

        private String resolvedClip;
        private boolean resolved;
        private float scale = 1f;
        private float stateTime;

        PamPortraitWidget(
            String pamPath,
            String requestedClip,
            float targetWidth,
            float targetHeight
        ) {
            this.pamPath = pamPath;
            this.requestedClip = requestedClip;
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;

            setSize(targetWidth, targetHeight);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!resolved) {
                resolveClip();
                resolved = true;
            }

            if (resolvedClip == null) {
                return;
            }

            float centerX = getX() + targetWidth / 2f;
            float centerY = getY() + targetHeight / 2f;

            // v0.1.4 of libPVZ's PamPlayer.draw() has no scale parameter، پس
            // اسکیل رو با دستکاری موقتِ ماتریس تبدیل خودِ batch انجام می‌دیم:
            // دور مرکز پرتره بزرگ/کوچیک می‌کنیم، رسم می‌کنیم، بعد برمی‌گردونیم.
            batch.flush();

            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(centerX, centerY, 0f)
                .scale(scale, scale, 1f)
                .translate(-centerX, -centerY, 0f);

            batch.setTransformMatrix(scaled);

            batch.setColor(1f, 1f, 1f, parentAlpha);

            Textures.getPamPlayer().draw(
                batch,
                pamPath,
                resolvedClip,
                stateTime,
                centerX,
                centerY,
                true
            );

            batch.flush();
            batch.setTransformMatrix(original);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void resolveClip() {
            List<String> clips = Textures.getPamPlayer().clips(pamPath);

            if (clips != null && clips.contains(requestedClip)) {
                resolvedClip = requestedClip;
            } else if (clips != null && !clips.isEmpty()) {
                Gdx.app.log(
                    "DialogueOverlay",
                    "Clip \"" + requestedClip + "\" not found in " + pamPath
                        + ", falling back to \"" + clips.get(0) + "\". Available: " + clips
                );
                resolvedClip = clips.get(0);
            } else {
                Gdx.app.error("DialogueOverlay", "No clips found for PAM: " + pamPath);
                return;
            }

            Rectangle bounds = Textures.getPamPlayer().bounds(pamPath, resolvedClip);

            if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
                scale = 1f;
            } else {
                scale = Math.min(
                    targetWidth / bounds.width,
                    targetHeight / bounds.height
                );
            }
        }
    }
}
