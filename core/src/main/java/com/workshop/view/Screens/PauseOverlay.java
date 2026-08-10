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
import com.workshop.model.GameContext;

public class PauseOverlay {

    private final Table overlay;
    private final GameContext gameContext;

    private final Texture dimTexture;
    private final Texture borderTexture;
    private final Texture panelTexture;
    private final Texture innerTexture;

    public PauseOverlay(
        Stage stage,
        Skin skin,
        GameContext gameContext,
        Runnable restartAction,
        Runnable saveExitAction
    ) {
        this.gameContext = gameContext;

        dimTexture = createTexture(new Color(0f, 0f, 0f, 0.62f));
        borderTexture = createTexture(new Color(0.25f, 0.12f, 0.04f, 1f));
        panelTexture = createTexture(new Color(0.58f, 0.34f, 0.16f, 1f));
        innerTexture = createTexture(new Color(0.92f, 0.86f, 0.65f, 1f));

        overlay = createOverlay();

        Table pauseMenu = createPauseMenu(
            skin,
            restartAction,
            saveExitAction
        );

        overlay.add(pauseMenu)
            .width(600)
            .height(400)
            .pad(6);

        stage.addActor(overlay);
    }

    private Table createOverlay() {
        Table table = new Table();

        table.setFillParent(true);
        table.setVisible(false);
        table.setTouchable(Touchable.enabled);

        table.setBackground(
            new TextureRegionDrawable(dimTexture)
        );

        return table;
    }

    private Table createPauseMenu(
        Skin skin,
        Runnable restartAction,
        Runnable saveExitAction
    ) {
        Table border = new Table();

        border.setBackground(
            new TextureRegionDrawable(borderTexture)
        );

        Table panel = new Table();

        panel.setBackground(
            new TextureRegionDrawable(panelTexture)
        );

        panel.pad(18);

        Label title = new Label(
            "GAME PAUSED",
            skin,
            "big"
        );

        Table innerPanel = new Table();

        innerPanel.setBackground(
            new TextureRegionDrawable(innerTexture)
        );

        Label message = new Label(
            "Are you scared? Do you want to LEAVE?",
            skin
        );

        innerPanel.add(message).pad(30);

        TextButton resumeButton =
            new TextButton("RESUME", skin, "purple");

        TextButton restartButton =
            new TextButton("RESTART", skin, "brown");

        TextButton saveExitButton =
            new TextButton("SAVE AND EXIT", skin, "brown");

        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();

                if (restartAction != null) {
                    restartAction.run();
                }
            }
        });

        saveExitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();

                if (saveExitAction != null) {
                    saveExitAction.run();
                }
            }
        });

        panel.add(title)
            .padBottom(18)
            .row();

        panel.add(innerPanel)
            .growX()
            .height(130)
            .padBottom(20)
            .row();

        Table buttons = new Table();

        buttons.add(saveExitButton)
            .width(160)
            .height(50)
            .padRight(10);

        buttons.add(restartButton)
            .width(130)
            .height(50)
            .padRight(10);

        buttons.add(resumeButton)
            .width(130)
            .height(50);

        panel.add(buttons);

        border.add(panel)
            .grow()
            .pad(5);

        return border;
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

    public void show() {
        if (gameContext != null) {
            gameContext.setPaused(true);
        }

        overlay.setVisible(true);
        overlay.toFront();
    }

    public void hide() {
        if (gameContext != null) {
            gameContext.setPaused(false);
        }

        overlay.setVisible(false);
    }

    public boolean isVisible() {
        return overlay.isVisible();
    }

    public void toggle() {
        if (isVisible()) {
            hide();
        } else {
            show();
        }
    }

    public void dispose() {
        dimTexture.dispose();
        borderTexture.dispose();
        panelTexture.dispose();
        innerTexture.dispose();
    }
}
