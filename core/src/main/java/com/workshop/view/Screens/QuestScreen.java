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
    private ImageButton backButton;

    public QuestScreen(PvzGame game, User user) {
        Skin skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Label title = new Label("Quests", skin, "big");
        root.add(title).padBottom(5).row();

        TextButton dailyButton =
            new TextButton("Daily", skin, "default");

        TextButton mainButton =
            new TextButton("Main", skin, "default");

        TextButton epicButton =
            new TextButton("Epic", skin, "default");

        Table categoryButtons = new Table();

        categoryButtons.add(dailyButton)
            .width(150)
            .padRight(15);

        categoryButtons.add(mainButton)
            .width(150)
            .padRight(15);

        categoryButtons.add(epicButton)
            .width(150);

        root.add(categoryButtons)
            .padBottom(15)
            .row();

        Table questTable = new Table();
        questTable.top().left();


        ScrollPane scrollPane = new ScrollPane(questTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);


        dailyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showCategory(
                    questTable,
                    Quest.QuestCategory.DAILY,
                    user,
                    skin
                );

                scrollPane.setScrollY(0);
            }
        });

        mainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showCategory(
                    questTable,
                    Quest.QuestCategory.MAIN,
                    user,
                    skin
                );

                scrollPane.setScrollY(0);
            }
        });



        epicButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showCategory(
                    questTable,
                    Quest.QuestCategory.EPIC,
                    user,
                    skin
                );

                scrollPane.setScrollY(0);
            }
        });

        showCategory(
            questTable,
            Quest.QuestCategory.MAIN,
            user,
            skin
        );

        root.add(scrollPane)
            .width(800)
            .height(500)
            .padBottom(50)
            .row();

        backButton = new ImageButton(skin, "generic_close_circle");

        backButton.setSize(70, 70);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showTravelMenu();
            }
        });

        stage.addActor(backButton);

        updateBackButtonPosition();
    }

    private void updateBackButtonPosition() {
        float width = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();

        float x = width * 0.94f - backButton.getWidth() / 2f;
        float y = height * 0.92f - backButton.getHeight() / 2f;

        backButton.setPosition(x, y);
    }

    private void showCategory(
        Table questTable,
        Quest.QuestCategory category,
        User user,
        Skin skin
    ) {
        questTable.clearChildren();

        List<Quest> quests =
            new ArrayList<>(DataManager.getInstance().quests.getAll());

        quests.removeIf(quest -> quest.getCategory() != category);

        quests.sort(
            Comparator.comparingInt(
                (Quest quest) -> quest.getPriority().ordinal()
            ).reversed()
        );

        for (Quest quest : quests) {
            addQuest(questTable, quest, user, skin);
        }
    }

    private void addQuest(Table table, Quest quest, User user, Skin skin) {
        boolean completed = user.isQuestCompleted(quest.getId());

        int progress = user.getQuestProgress(quest.getId());
        int target = Math.max(1, quest.getTargetProgress());

        int shownProgress = Math.min(progress, target);

        Label name = new Label(quest.getName(), skin);

        Label description = new Label(
            quest.getDescription(),
            skin
        );
        description.setWrap(true);

        Label info = new Label(
            "Category: " + quest.getCategory()
                + " | Priority: " + quest.getPriority()
                + " | Reward: " + quest.getRewardAmount()
                + " " + quest.getRewardType(),
            skin
        );

        ProgressBar progressBar = new ProgressBar(
            0,
            target,
            1,
            false,
            skin,
            "xp_green"
        );

        if (completed) {
            progressBar.setValue(target);
        } else {
            progressBar.setValue(shownProgress);
        }

        progressBar.setAnimateDuration(0.2f);

        Label progressLabel;

        if (completed) {
            progressLabel = new Label("Completed", skin);
        } else {
            progressLabel = new Label(
                shownProgress + " / " + target,
                skin
            );
        }

        Table progressTable = new Table();

        progressTable.add(progressBar)
            .width(350)
            .height(20)
            .padRight(15);

        progressTable.add(progressLabel)
            .left();

        table.add(name)
            .left()
            .padTop(15)
            .row();

        table.add(description)
            .width(700)
            .left()
            .padTop(5)
            .row();

        table.add(info)
            .left()
            .padTop(5)
            .row();

        table.add(progressTable)
            .left()
            .padTop(8)
            .padBottom(15)
            .row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClearColor(
            0.32f,
            0.18f,
            0.42f,
            1f
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(v);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        updateBackButtonPosition();
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
