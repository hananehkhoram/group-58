package com.workshop.view.Screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.workshop.PvzGame;
import com.workshop.model.user.User;
import pvz.skin.PvzSkin;

public class TravelMenuScreen implements Screen {

    private final Stage stage;

    public TravelMenuScreen(PvzGame game, User user) {

        Skin skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float v) {

    }

    @Override
    public void resize(int i, int i1) {

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

    }
}
