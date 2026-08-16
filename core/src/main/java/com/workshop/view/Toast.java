package com.workshop.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;


public final class Toast {//اعلان موقت

    private static final float FADE_DURATION = 0.25f;
    private static final float VISIBLE_DURATION = 3.5f;
    private static final float TOP_MARGIN = 32f;
    private static final float MAX_WIDTH = 380f;
    private static final float MISSION_MAX_WIDTH = 640f;

    private Toast() {}

    public static void showError(Stage stage, Skin skin, String message) {
        show(stage, skin, message, Color.valueOf("E85D5D"), MAX_WIDTH, VISIBLE_DURATION);
    }

    public static void showSuccess(Stage stage, Skin skin, String message) {
        show(stage, skin, message, Color.valueOf("6FCF6F"), MAX_WIDTH, VISIBLE_DURATION);
    }

    public static void showInfo(Stage stage, Skin skin, String message) {
        show(stage, skin, message, Color.WHITE, MAX_WIDTH, VISIBLE_DURATION);
    }

    public static void showMission(
        Stage stage,
        Skin skin,
        String message,
        float totalDuration
    ) {
        showCentered(
            stage,
            skin,
            message,
            Color.valueOf("5B3A29"),
            MISSION_MAX_WIDTH,
            totalDuration
        );
    }

    private static void show(Stage stage, Skin skin, String message, Color textColor, float maxWidth, float visibleDuration) {
        if (message == null || message.isBlank()) return;

        Table toast = buildToast(skin, message, textColor, maxWidth, "default");
        toast.getColor().a = 0f;
        stage.addActor(toast);

        float restY = stage.getHeight() - toast.getHeight() - TOP_MARGIN;
        toast.setPosition((stage.getWidth() - toast.getWidth()) / 2f, restY + 16f);

        toast.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(FADE_DURATION),
                Actions.moveTo(toast.getX(), restY, FADE_DURATION)
            ),
            Actions.delay(visibleDuration),
            Actions.fadeOut(FADE_DURATION),
            Actions.removeActor()
        ));
    }

    private static void showCentered(Stage stage, Skin skin, String message, Color textColor, float maxWidth, float visibleDuration) {
        if (message == null || message.isBlank()) return;

        Table toast = buildToast(skin, message, textColor, maxWidth, "big");
        toast.getColor().a = 0f;
        stage.addActor(toast);

        float centerX = (stage.getWidth() - toast.getWidth()) / 2f;
        float centerY = (stage.getHeight() - toast.getHeight()) / 2f;
        toast.setPosition(centerX, centerY);

        float holdDuration = Math.max(
            0f,
            visibleDuration - 2f * FADE_DURATION
        );

        toast.addAction(Actions.sequence(
            Actions.fadeIn(FADE_DURATION),
            Actions.delay(holdDuration),
            Actions.fadeOut(FADE_DURATION),
            Actions.removeActor()
        ));
    }

    private static Table buildToast(Skin skin, String message, Color textColor, float maxWidth, String labelStyle) {
        Label label = new Label(message.trim(), skin, labelStyle);
        label.setColor(textColor);
        label.setWrap(true);
        label.setAlignment(Align.center);

        Table toast = new Table();
        toast.pad(20, 28, 20, 28);
        toast.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        toast.add(label).width(maxWidth - 56);
        toast.pack();

        return toast;
    }
    public static void showAnnouncement(Stage stage, Skin skin, String message) {
        if (message == null || message.isBlank()) return;

        Label label = new Label(message.trim(), skin, "big");
        label.setColor(Color.valueOf("D6231C"));
        label.setFontScale(1.3f);
        label.setWrap(true);
        label.setAlignment(Align.center);

        Table holder = new Table();
        holder.add(label).width(MISSION_MAX_WIDTH - 40);
        holder.pack();
        holder.getColor().a = 0f;
        stage.addActor(holder);

        holder.setPosition(
            (stage.getWidth() - holder.getWidth()) / 2f,
            stage.getHeight() * 0.55f
        );

        holder.addAction(Actions.sequence(
            Actions.fadeIn(FADE_DURATION),
            Actions.delay(VISIBLE_DURATION),
            Actions.fadeOut(FADE_DURATION),
            Actions.removeActor()
        ));
    }
}
