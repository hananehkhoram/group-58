package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.workshop.PvzGame;
import com.workshop.controller.NewsManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.News.News;
import com.workshop.model.user.User;
import pvz.skin.PvzSkin;

import java.util.List;

public class NewsScreen implements Screen {

    private final Stage stage;

    public NewsScreen(PvzGame game, User user) {
        Skin skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.pad(30);
        stage.addActor(root);

        Label title = new Label("News", skin, "big");

        TextButton backButton = new TextButton("Back", skin, "default");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showMain();
            }
        });

        Table newsTable = new Table();
        newsTable.top();

        List<News> newsList = NewsManager.getAllNews();

        if (newsList.isEmpty()) {
            newsTable.add(new Label("No news available.", skin));
        } else {
            for (News news : newsList) {
                addNews(newsTable, news, skin);
            }

            int latestId = newsList.get(newsList.size() - 1).getId();
            user.setLastReadNewsId(latestId);
            DataManager.getInstance().saveUser();
        }

        ScrollPane scrollPane = new ScrollPane(newsTable, skin);
        scrollPane.setFadeScrollBars(false);

        root.add(title)
            .expandX()
            .left();

        root.add(backButton)
            .width(150)
            .right()
            .row();

        root.add(scrollPane)
            .colspan(2)
            .expand()
            .fill()
            .padTop(20);
    }

    private void addNews(Table table, News news, Skin skin) {
        Table card = new Table(skin);

        Label title = new Label(news.getTitle(), skin);
        Label date = new Label(news.getDate().toString(), skin);
        Label content = new Label(news.getContent(), skin);

        content.setWrap(true);

        card.add(title)
            .left()
            .expandX()
            .pad(10);

        card.add(date)
            .right()
            .pad(10)
            .row();

        card.add(content)
            .colspan(2)
            .width(600)
            .left()
            .pad(10)
            .row();

        table.add(card)
            .width(650)
            .padBottom(15)
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
