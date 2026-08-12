package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.PvzGame;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.Textures;
import com.workshop.model.user.User;

import pvz.skin.PvzSkin;

public class SettingsScreen implements Screen {

    private final Stage stage;
    private Texture backgroundTexture;

    public SettingsScreen(PvzGame game, User user) {
        Skin skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        buildBackground();
        buildUi(game, user, skin);
    }

    private void buildBackground() {
        backgroundTexture = new Texture(
            Gdx.files.internal(
                "IMAGES/Menus/Settings/SettingsBackground.png"
            )
        );

        Image background = new Image(backgroundTexture);
        background.setScaling(Scaling.fill);
        background.setFillParent(true);

        stage.addActor(background);
    }

    private void buildUi(PvzGame game, User user, Skin skin) {
        Table root = new Table();
        root.setFillParent(true);

        root.left();
        root.padLeft(180);

        stage.addActor(root);

        Table settingsPanel = new Table();

        Label title = new Label(
            "Settings",
            skin,
            "big"
        );

        SelectBox<Integer> difficultyBox =
            new SelectBox<>(skin);

        difficultyBox.setItems(
            1,
            2,
            3,
            4,
            5
        );

        difficultyBox.setSelected(
            user.getDifficultyLevel()
        );

        SelectBox<Integer> gameSpeedBox =
            new SelectBox<>(skin);

        gameSpeedBox.setItems(
            1,
            2,
            3
        );

        gameSpeedBox.setSelected(
            user.getGameSpeed()
        );

        CheckBox gridCheckBox =
            new CheckBox(
                " Show Grid",
                skin
            );

        gridCheckBox.setChecked(
            user.isGridEnabled()
        );

        CheckBox debugCheckBox =
            new CheckBox(
                " Debug Mode",
                skin
            );

        debugCheckBox.setChecked(
            user.isDebugMode()
        );

        TextButton backButton =
            new TextButton(
                "Back",
                skin,
                "default"
            );

        addListeners(
            game,
            user,
            difficultyBox,
            gameSpeedBox,
            gridCheckBox,
            debugCheckBox,
            backButton
        );

        settingsPanel.add(title)
            .colspan(2)
            .padBottom(35)
            .row();

        settingsPanel.add(
                new Label(
                    "Difficulty",
                    skin
                )
            )
            .pad(10)
            .right();

        settingsPanel.add(difficultyBox)
            .width(150)
            .pad(10)
            .row();

        settingsPanel.add(
                new Label(
                    "Game Speed",
                    skin
                )
            )
            .pad(10)
            .right();

        settingsPanel.add(gameSpeedBox)
            .width(150)
            .pad(10)
            .row();

        Table checkBoxTable = new Table();

        checkBoxTable.add(gridCheckBox)
            .left()
            .row();

        checkBoxTable.add(debugCheckBox)
            .left()
            .padTop(12)
            .row();

        settingsPanel.add(checkBoxTable)
            .colspan(2)
            .left()
            .padLeft(65)
            .padTop(15)
            .padBottom(15)
            .row();

        settingsPanel.add(backButton)
            .colspan(2)
            .width(200)
            .height(55)
            .padTop(25);

        root.add(settingsPanel);
    }

    private void addListeners(
        PvzGame game,
        User user,
        SelectBox<Integer> difficultyBox,
        SelectBox<Integer> gameSpeedBox,
        CheckBox gridCheckBox,
        CheckBox debugCheckBox,
        TextButton backButton
    ) {
        difficultyBox.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    user.setDifficultyLevel(
                        difficultyBox.getSelected()
                    );
                }
            }
        );

        gameSpeedBox.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    user.setGameSpeed(
                        gameSpeedBox.getSelected()
                    );
                }
            }
        );

        gridCheckBox.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    user.setGridEnabled(
                        gridCheckBox.isChecked()
                    );
                }
            }
        );

        debugCheckBox.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    user.setDebugMode(
                        debugCheckBox.isChecked()
                    );
                }
            }
        );

        backButton.addListener(
            new ChangeListener() {
                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ) {
                    DataManager
                        .getInstance()
                        .saveUser();

                    game.showMain();
                }
            }
        );
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(
            0f,
            0f,
            0f,
            1f
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(
        int width,
        int height
    ) {
        stage.getViewport().update(
            width,
            height,
            true
        );
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
