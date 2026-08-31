package com.workshop.view.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class WinLoseOverlay {

    private final Table overlay;
    private final Label titleLabel;
    private final Label messageLabel;
    private final TextButton retryButton;

    private final Texture dimTexture;
    private final Texture borderTexture;
    private final Texture panelTexture;
    private final Texture innerTexture;

    public WinLoseOverlay(
        Stage stage,
        Skin skin,
        Runnable retryAction,
        Runnable exitAction
    ) {
        dimTexture = createTexture(
            new Color(0f, 0f, 0f, 0.65f)
        );

        borderTexture = createTexture(
            new Color(0.25f, 0.12f, 0.04f, 1f)
        );

        panelTexture = createTexture(
            new Color(0.58f, 0.34f, 0.16f, 1f)
        );

        innerTexture = createTexture(
            new Color(0.92f, 0.86f, 0.65f, 1f)
        );

        overlay = new Table();
        overlay.setFillParent(true);
        overlay.setVisible(false);
        overlay.setTouchable(Touchable.enabled);

        overlay.setBackground(
            new TextureRegionDrawable(dimTexture)
        );

        Table border = new Table();
        border.setBackground(
            new TextureRegionDrawable(borderTexture)
        );

        Table panel = new Table();
        panel.setBackground(
            new TextureRegionDrawable(panelTexture)
        );
        panel.pad(20);

        titleLabel = new Label(
            "",
            skin,
            "big"
        );

        Table messagePanel = new Table();
        messagePanel.setBackground(
            new TextureRegionDrawable(innerTexture)
        );

        messageLabel = new Label(
            "",
            skin
        );

        messagePanel.add(messageLabel)
            .pad(35);

        retryButton = new TextButton(
            "RETRY",
            skin,
            "purple"
        );

        TextButton exitButton = new TextButton(
            "EXIT",
            skin,
            "brown"
        );

        retryButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                hide();

                if (retryAction != null) {
                    retryAction.run();
                }
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(
                ChangeEvent event,
                Actor actor
            ) {
                hide();

                if (exitAction != null) {
                    exitAction.run();
                }
            }
        });

        panel.add(titleLabel)
            .padBottom(20)
            .row();

        panel.add(messagePanel)
            .width(500)
            .height(150)
            .padBottom(20)
            .row();

        Table buttons = new Table();

        buttons.add(retryButton)
            .width(150)
            .height(55)
            .padRight(15);

        buttons.add(exitButton)
            .width(150)
            .height(55);

        panel.add(buttons);

        border.add(panel)
            .grow()
            .pad(5);

        overlay.add(border)
            .width(600)
            .height(400);

        stage.addActor(overlay);
    }

    public void showWin() {
        titleLabel.setText("VICTORY!");

        messageLabel.setText(
            "Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz."
        );

        retryButton.setVisible(false);

        overlay.setVisible(true);
        overlay.toFront();
    }

    public void showLose() {
        titleLabel.setText("YOU LOST!");

        messageLabel.setText(
            "The zombies ate your brain! LOSER."
        );

        retryButton.setVisible(true);

        overlay.setVisible(true);
        overlay.toFront();
    }

    public void hide() {
        overlay.setVisible(false);
    }

    public boolean isVisible() {
        return overlay.isVisible();
    }

    private Texture createTexture(Color color) {
        Pixmap pixmap = new Pixmap(
            1,
            1,
            Pixmap.Format.RGBA8888
        );

        pixmap.setColor(color);
        pixmap.fill();

        Texture texture = new Texture(pixmap);

        pixmap.dispose();

        return texture;
    }

    public void dispose() {
        dimTexture.dispose();
        borderTexture.dispose();
        panelTexture.dispose();
        innerTexture.dispose();
    }
}
