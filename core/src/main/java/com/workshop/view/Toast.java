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

    private Toast() {}

    public static void showError(Stage stage, Skin skin, String message) {
        show(stage, skin, message, Color.valueOf("E85D5D"));
    }

    public static void showSuccess(Stage stage, Skin skin, String message) {
        show(stage, skin, message, Color.valueOf("6FCF6F"));
    }

    public static void showInfo(Stage stage, Skin skin, String message) {
        show(stage, skin, message, Color.WHITE);
    }

    private static void show(Stage stage, Skin skin, String message, Color textColor) {
        if (message == null || message.isBlank()) return;

        Label label = new Label(message.trim(), skin, "default");
        label.setColor(textColor);
        label.setWrap(true);
        label.setAlignment(Align.center);

        Table toast = new Table();
        toast.pad(14, 22, 14, 22);
        toast.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));
        toast.add(label).width(MAX_WIDTH - 44);
        toast.pack();

        toast.getColor().a = 0f;
        stage.addActor(toast);

        float restY = stage.getHeight() - toast.getHeight() - TOP_MARGIN;
        toast.setPosition((stage.getWidth() - toast.getWidth()) / 2f, restY + 16f);

        toast.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(FADE_DURATION),
                Actions.moveTo(toast.getX(), restY, FADE_DURATION)
            ),
            Actions.delay(VISIBLE_DURATION),
            Actions.fadeOut(FADE_DURATION),
            Actions.removeActor()
        ));
    }
}
