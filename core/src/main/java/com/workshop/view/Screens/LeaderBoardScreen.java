package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.PvzGame;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.net.GameClient;
import com.workshop.net.UserSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
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
    private final EnumMap<SortColumn, TextButton> headerButtons =
        new EnumMap<>(SortColumn.class);
    private com.workshop.view.components.CurrencyHeader currencyHeader;

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

        Table topBar = new Table();
        currencyHeader = new com.workshop.view.components.CurrencyHeader();
        topBar.add(currencyHeader).right().padRight(10);
        root.add(topBar).fillX().height(45).pad(0, 0, 10, 0).row();

        Label title = new Label("Leaderboard", skin, "big");
        root.add(title).padBottom(25).row();

        Table header = new Table();

        addHeader(header, "#", null, 55);
        addHeader(header, "Username", SortColumn.USERNAME, 180);
        addHeader(header, "Season / Level", SortColumn.SEASON_LEVEL, 170);
        addHeader(header, "Minigames", SortColumn.MINIGAMES, 120);
        addHeader(header, "Daily", SortColumn.DAILY_QUESTS, 110);
        addHeader(header, "Other", SortColumn.OTHER_QUESTS, 110);
        addHeader(header, "My Point", SortColumn.SCORE, 130);

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
                game.showTravelMenu();
            }
        });

        Table backContainer = new Table();
        backContainer.setFillParent(true);
        backContainer.top().right();
        backContainer.padTop(20);
        backContainer.padRight(20);

        backContainer.add(backButton)
            .size(70, 70);

        stage.addActor(backContainer);

        updateHeaderTexts();
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

        headerButtons.put(column, button);

        button.addListener(new ClickListener(Input.Buttons.LEFT) {
            @Override
            public void clicked(InputEvent event, float x, float y) {
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

        updateHeaderTexts();
        refreshRows();
    }

    private void updateHeaderTexts() {
        setHeaderText(SortColumn.USERNAME, "Username");
        setHeaderText(SortColumn.SEASON_LEVEL, "Season / Level");
        setHeaderText(SortColumn.MINIGAMES, "Minigames");
        setHeaderText(SortColumn.DAILY_QUESTS, "Daily");
        setHeaderText(SortColumn.OTHER_QUESTS, "Other");
        setHeaderText(SortColumn.SCORE, "My Point");

        TextButton activeButton = headerButtons.get(sortColumn);
        if (activeButton == null) {
            return;
        }

        String direction = ascending ? " ^" : " v";
        activeButton.setText(activeButton.getText().toString() + direction);
    }

    private void setHeaderText(SortColumn column, String text) {
        TextButton button = headerButtons.get(column);
        if (button != null) {
            button.setText(text);
        }
    }

    private void refreshRows() {
        if (currencyHeader != null) {
            currencyHeader.updateValues();
        }

        rowsTable.clearChildren();

        List<User> users = loadUsers();

        users.sort(getComparator());

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
                    .thenComparingInt(User::getLastLevel)
                    .thenComparing(
                        User::getUsername,
                        String.CASE_INSENSITIVE_ORDER
                    );
                break;

            case MINIGAMES:
                comparator = Comparator
                    .comparingInt(User::getMinigamesCompleted)
                    .thenComparing(
                        User::getUsername,
                        String.CASE_INSENSITIVE_ORDER
                    );
                break;

            case DAILY_QUESTS:
                comparator = Comparator
                    .comparingInt(User::getDailyQuestsCompletedCount)
                    .thenComparing(
                        User::getUsername,
                        String.CASE_INSENSITIVE_ORDER
                    );
                break;

            case OTHER_QUESTS:
                comparator = Comparator
                    .comparingInt(User::getOtherQuestsCompletedCount)
                    .thenComparing(
                        User::getUsername,
                        String.CASE_INSENSITIVE_ORDER
                    );
                break;

            case SCORE:
            default:
                comparator = Comparator
                    .comparingInt(LeaderBoardScreen::networkBonusOrHidden)
                    .thenComparing(
                        User::getUsername,
                        String.CASE_INSENSITIVE_ORDER
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
        addCell(String.valueOf(user.getMinigamesCompleted()), 120);
        addCell(String.valueOf(user.getDailyQuestsCompletedCount()), 110);
        addCell(String.valueOf(user.getOtherQuestsCompletedCount()), 110);
        addCell(formatMyPoint(user), 130);

        rowsTable.row();
    }

    private List<User> loadUsers() {
        GameClient client = GameClient.get();

        if (client.isConnected()) {
            List<User> users = new ArrayList<>();

            for (UserSnapshot snapshot : client.getLeaderboard()) {
                User user = new User();
                user.setUsername(snapshot.username);

                snapshot.applyTo(user);

                users.add(user);
            }

            return users;
        }

        return UserManager.getInstance().getUsers();
    }

    private static int networkBonusOrHidden(User user) {
        return user.hasNetworkBonusScore() ? user.getMaxMewPoint() : -1;
    }

    private static String formatMyPoint(User user) {
        return user.hasNetworkBonusScore() ? String.valueOf(user.getMaxMewPoint()) : "-";
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
        refreshRows();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(
            0.32f,
            0.18f,
            0.42f,
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
