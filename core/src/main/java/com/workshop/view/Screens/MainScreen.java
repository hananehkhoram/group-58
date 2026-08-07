package com.workshop.view.Screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.PvzGame;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;

import pvz.skin.PvzSkin;

/**
 * Placeholder landing screen shown right after a successful login/registration.
 * Replace this with the real main-menu UI once it's ready — for now it just proves
 * the login/register -> main flow works end to end.
 */
public class MainScreen implements Screen {

    private final Stage stage;

    public MainScreen(PvzGame game, User user) {
        Skin skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        String nickName = user != null ? user.getNickName() : "player";
        Label welcome = new Label("Welcome, " + nickName + "!", skin, "big");

        TextButton logoutButton = new TextButton("Logout", skin, "default");
        logoutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                UserManager.getInstance().logOut();
                game.showLogin();
            }
        });

        root.add(welcome).padBottom(20).row();
        root.add(logoutButton).width(200);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
