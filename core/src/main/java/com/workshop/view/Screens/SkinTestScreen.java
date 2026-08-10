package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.workshop.PvzGame;

import com.workshop.model.user.User;
import pvz.skin.PvzSkin;

public class SkinTestScreen implements Screen {

    private final Stage stage;

    public SkinTestScreen(PvzGame game, User user) {
        Skin skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("PVZ Skin Test", skin, "big");

        root.add(title)
            .padTop(20)
            .padBottom(20)
            .row();

        Table content = new Table();
        content.top().left();

        addLabelStyles(content, skin);
        addTextButtonStyles(content, skin);
        addImageButtonStyles(content, skin);
        addProgressBarStyles(content, skin);
        addScrollPaneStyles(content, skin);

        ScrollPane scrollPane = new ScrollPane(content, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        root.add(scrollPane)
            .expand()
            .fill()
            .pad(20)
            .row();

        TextButton backButton =
            new TextButton("Back", skin, "default");

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showMain();
            }
        });

        root.add(backButton)
            .width(200)
            .padBottom(20);
    }

    private void addLabelStyles(Table table, Skin skin) {
        addSectionTitle(table, skin, "LABEL STYLES");

        ObjectMap<String, Label.LabelStyle> styles =
            skin.getAll(Label.LabelStyle.class);

        if (styles == null) {
            return;
        }

        for (ObjectMap.Entry<String, Label.LabelStyle> entry
            : styles.entries()) {

            Label label =
                new Label("Label style: " + entry.key, skin, entry.key);

            table.add(label)
                .left()
                .pad(8)
                .row();
        }
    }

    private void addTextButtonStyles(Table table, Skin skin) {
        addSectionTitle(table, skin, "TEXT BUTTON STYLES");

        ObjectMap<String, TextButton.TextButtonStyle> styles =
            skin.getAll(TextButton.TextButtonStyle.class);

        if (styles == null) {
            return;
        }

        for (ObjectMap.Entry<String, TextButton.TextButtonStyle> entry
            : styles.entries()) {

            TextButton button =
                new TextButton(entry.key, skin, entry.key);

            table.add(button)
                .width(260)
                .pad(8)
                .left()
                .row();
        }
    }

    private void addImageButtonStyles(Table table, Skin skin) {
        addSectionTitle(table, skin, "IMAGE BUTTON STYLES");

        ObjectMap<String, ImageButton.ImageButtonStyle> styles =
            skin.getAll(ImageButton.ImageButtonStyle.class);

        if (styles == null) {
            return;
        }

        for (ObjectMap.Entry<String, ImageButton.ImageButtonStyle> entry
            : styles.entries()) {

            Label name = new Label(entry.key, skin);

            ImageButton button =
                new ImageButton(skin, entry.key);

            table.add(name)
                .left()
                .pad(8);

            table.add(button)
                .pad(8)
                .left()
                .row();
        }
    }

    private void addProgressBarStyles(Table table, Skin skin) {
        addSectionTitle(table, skin, "PROGRESS BAR STYLES");

        ObjectMap<String, ProgressBar.ProgressBarStyle> styles =
            skin.getAll(ProgressBar.ProgressBarStyle.class);

        if (styles == null) {
            return;
        }

        for (ObjectMap.Entry<String, ProgressBar.ProgressBarStyle> entry
            : styles.entries()) {

            Label name = new Label(entry.key, skin);

            ProgressBar progressBar =
                new ProgressBar(
                    0,
                    100,
                    1,
                    false,
                    skin,
                    entry.key
                );

            progressBar.setValue(65);

            table.add(name)
                .left()
                .pad(8);

            table.add(progressBar)
                .width(300)
                .pad(8)
                .row();
        }
    }

    private void addScrollPaneStyles(Table table, Skin skin) {
        addSectionTitle(table, skin, "SCROLL PANE STYLES");

        ObjectMap<String, ScrollPane.ScrollPaneStyle> styles =
            skin.getAll(ScrollPane.ScrollPaneStyle.class);

        if (styles == null) {
            return;
        }

        for (ObjectMap.Entry<String, ScrollPane.ScrollPaneStyle> entry
            : styles.entries()) {

            Label label =
                new Label("ScrollPane style: " + entry.key, skin);

            table.add(label)
                .left()
                .pad(8)
                .row();
        }
    }

    private void addSectionTitle(
        Table table,
        Skin skin,
        String text
    ) {
        Label section = new Label(text, skin, "big");

        table.add(section)
            .left()
            .padTop(30)
            .padBottom(10)
            .colspan(2)
            .row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(
            95f / 255f,
            59f / 255f,
            35f / 255f,
            1f
        );

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
    }
}
