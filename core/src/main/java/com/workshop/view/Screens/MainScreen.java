package com.workshop.view.Screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.PvzGame;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.workshop.controller.NewsManager;
import com.workshop.view.Screens.PauseOverlay;

import pvz.skin.PvzSkin;

/**
 * Placeholder landing screen shown right after a successful login/registration.
 * Replace this with the real main-menu UI once it's ready — for now it just proves
 * the login/register -> main flow works end to end.
 */
public class MainScreen implements Screen {

    private final Stage stage;
    private final PauseOverlay pauseOverlay;

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

        TextButton travelMenuButton =
            new TextButton("**Travel Menu**", skin, "default");

        travelMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showTravelMenu();
            }
        });

        ImageButton settingsButton =
            new ImageButton(skin, "default");

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showSettings();
            }
        });

        TextButton newsButton = new TextButton("News", skin, "brown");

        newsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showNews();
            }
        });

        boolean hasUnreadNews = NewsManager.getAllNews()
            .stream()
            .anyMatch(news -> news.getId() > user.getLastReadNewsId());

        Stack newsStack = new Stack();
        newsStack.add(newsButton);

        if (hasUnreadNews) {
            Label unreadLabel = new Label("!", skin, "big");
            unreadLabel.setColor(Color.RED);

            Table badgeLayer = new Table();
            badgeLayer.top().right();

            badgeLayer.add(unreadLabel)
                .padTop(2)
                .padRight(15);

            newsStack.add(badgeLayer);
        }

        // ========================= TEST ==================================

        TextButton SkinButton = new TextButton("**SkinTest**", skin, "default");
        SkinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showSkinTest();
            }
        });

        ImageButton pauseTestButton =
            new ImageButton(skin, "ingame_pause");

        pauseTestButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                pauseOverlay.show();
            }
        });

        pauseOverlay = new PauseOverlay(
            stage,
            skin,
            null,
            () -> {
                System.out.println("Restart clicked");
            },
            () -> {
                System.out.println("Save and Exit clicked");
            }
        );

        // ============================================================================

        Table topBar = new Table();
        topBar.setFillParent(true);
        topBar.top();
        topBar.pad(20);

        topBar.add(settingsButton)
            .size(70, 70)
            .right()
            .expandX();

        topBar.add(pauseTestButton)
            .size(70, 70)
            .right();

        stage.addActor(topBar);

        root.add(welcome).padBottom(20).row();
        root.add(travelMenuButton).width(200).row();
        root.add(SkinButton).width(200).row();
        root.add(newsStack).width(200).row();
        root.add(logoutButton).width(200).row();

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
