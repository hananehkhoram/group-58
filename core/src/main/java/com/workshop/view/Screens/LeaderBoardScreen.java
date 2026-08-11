package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.PvzGame;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.user.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import pvz.skin.PvzSkin;

public class LeaderBoardScreen implements Screen {

    private enum SortColumn {
        USERNAME,
        SEASON_LEVEL,
        MINIGAMES,
        DAILY_QUESTS,
        OTHER_QUESTS,
        SCORE
    }

    private final Stage stage;
    private final Skin skin;
    private final Table rowsTable;

    private SortColumn sortColumn = SortColumn.SCORE;
    private boolean ascending = false;

    public LeaderBoardScreen(PvzGame game) {
        skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        root.top();
        root.padTop(30);
        stage.addActor(root);

        Label title = new Label("Leaderboard", skin, "big");
        root.add(title).padBottom(25).row();

        Table header = new Table();

        addHeader(header, "#", null, 55);
        addHeader(header, "Username", SortColumn.USERNAME, 180);
        addHeader(header, "Season / Level", SortColumn.SEASON_LEVEL, 170);
        addHeader(header, "Minigames", SortColumn.MINIGAMES, 120);
        addHeader(header, "Daily", SortColumn.DAILY_QUESTS, 110);
        addHeader(header, "Other", SortColumn.OTHER_QUESTS, 110);
        addHeader(header, "Mew Point", SortColumn.SCORE, 130);

        root.add(header).padBottom(5).row();

        rowsTable = new Table();
        rowsTable.top();

        ScrollPane scrollPane = new ScrollPane(rowsTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        root.add(scrollPane)
            .width(900)
            .height(480)
            .padBottom(20)
            .row();

        ImageButton backButton =
            new ImageButton(skin, "generic_close_circle");

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showMain();
            }
        });

        root.add(backButton)
            .size(70, 70)
            .padTop(10);

        refreshRows();
    }

    private void addHeader(
        Table header,
        String text,
        SortColumn column,
        float width
    ) {
        if (column == null) {
            Label label = new Label(text, skin);
            label.setAlignment(Align.center);

            header.add(label)
                .width(width)
                .center();

            return;
        }

        TextButton button =
            new TextButton(text, skin, "default");

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sortBy(column);
            }
        });

        header.add(button)
            .width(width)
            .height(50);
    }

    private void sortBy(SortColumn column) {
        if (sortColumn == column) {
            ascending = !ascending;
        } else {
            sortColumn = column;
            ascending = true;
        }

        refreshRows();
    }

    private void refreshRows() {
        rowsTable.clearChildren();

        List<User> users = new ArrayList<>(
            DataManager.getInstance()
                .users
                .getUserMap()
                .values()
        );

        Comparator<User> comparator = getComparator();

        users.sort(comparator);

        int rank = 1;

        for (User user : users) {
            addUserRow(rank, user);
            rank++;
        }
    }

    private Comparator<User> getComparator() {
        Comparator<User> comparator;

        switch (sortColumn) {
            case USERNAME:
                comparator = Comparator.comparing(
                    User::getUsername,
                    String.CASE_INSENSITIVE_ORDER
                );
                break;

            case SEASON_LEVEL:
                comparator = Comparator
                    .comparingInt(User::getLastSeason)
                    .thenComparingInt(User::getLastLevel);
                break;

            case MINIGAMES:
                comparator = Comparator.comparingInt(
                    User::getMinigamesCompleted
                );
                break;

            case DAILY_QUESTS:
                comparator = Comparator.comparingInt(
                    User::getDailyQuestsCompletedCount
                );
                break;

            case OTHER_QUESTS:
                comparator = Comparator.comparingInt(
                    User::getOtherQuestsCompletedCount
                );
                break;

            case SCORE:
            default:
                comparator = Comparator.comparingInt(
                    User::getMaxMewPoint
                );
                break;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private void addUserRow(int rank, User user) {
        String seasonLevel =
            user.getLastSeason() + " / " + user.getLastLevel();

        addCell(String.valueOf(rank), 55);
        addCell(user.getUsername(), 180);
        addCell(seasonLevel, 170);

        addCell(
            String.valueOf(user.getMinigamesCompleted()),
            120
        );

        addCell(
            String.valueOf(user.getDailyQuestsCompletedCount()),
            110
        );

        addCell(
            String.valueOf(user.getOtherQuestsCompletedCount()),
            110
        );

        addCell(
            String.valueOf(user.getMaxMewPoint()),
            130
        );

        rowsTable.row();
    }

    private void addCell(String text, float width) {
        Label label = new Label(text, skin);
        label.setAlignment(Align.center);

        rowsTable.add(label)
            .width(width)
            .height(45)
            .padTop(3)
            .padBottom(3)
            .center();
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
