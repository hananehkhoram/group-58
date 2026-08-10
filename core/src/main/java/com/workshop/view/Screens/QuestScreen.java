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
import com.workshop.controller.repository.DataManager;
import com.workshop.model.Quest;
import com.workshop.model.user.User;

import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuestScreen implements Screen {

    private final Stage stage;

    public QuestScreen(PvzGame game, User user) {
        Skin skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Label title = new Label("Quests", skin, "big");
        root.add(title).padBottom(20).row();

        Table questTable = new Table();
        questTable.setFillParent(true);

        List<Quest> quests = new ArrayList<>(DataManager.getInstance().quests.getAll());
        quests.sort(
            Comparator.comparingInt(
                (Quest quest) -> quest.getPriority().ordinal()
            ).reversed()
        );

        for (Quest quest : quests){
            addQuest(questTable, quest, user, skin);
        }

        ScrollPane scrollPane = new ScrollPane(questTable, skin);
        scrollPane.setFadeScrollBars(false);

        root.add(scrollPane)
            .width(800)
            .height(500)
            .padBottom(50)
            .row();

        ImageButton backButton = new ImageButton(skin, "generic_close_circle");
        backButton.addListener(new  ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showTravelMenu();
            }
        });

        root.add(backButton).width(200);
    }

    private void addQuest(Table table, Quest quest, User user, Skin skin) {
        boolean completed = user.isQuestCompleted(quest.getId());
        int progress = user.getQuestProgress(quest.getId());
        String status;

        if (completed){
            status = "Completed";
        } else {
            status = progress + " / " + quest.getTargetProgress();
        }

        Label name = new Label(quest.getName(), skin);

        Label description = new Label(quest.getDescription(), skin);

        description.setWrap(true);

        Label info = new Label("Category: " + quest.getCategory() + " | Priority: " + quest.getPriority() + " | Progress: " + status + " | Rewaerd: " + quest.getRewardAmount() + " " + quest.getRewardType(), skin);

        table.add(name).left().padTop(15).row();
        table.add(description).width(700).left().padTop(5).row();
        table.add(info).left().padTop(5).padBottom(15).row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(v);
        stage.draw();
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
        stage.dispose();
    }
}
