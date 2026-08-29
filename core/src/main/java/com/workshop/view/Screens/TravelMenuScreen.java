package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.workshop.PvzGame;
import com.workshop.model.season.Season;
import com.workshop.model.user.User;
import com.workshop.view.components.CurrencyHeader;
import pvz.skin.PvzSkin;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class TravelMenuScreen implements Screen {

    private final Stage stage;
    private final Texture backgroundTexture;
    private final Image background;
    private CurrencyHeader currencyHeader;

    private static final float MAP_WIDTH = 1280f;
    private static final float MAP_HEIGHT = 720f;

    private LevelNodeActor.Listener listener;

    public TravelMenuScreen(PvzGame game, User user, Season season) {

        listener = new LevelNodeActor.Listener() {
            @Override
            public void onEnterMiniGame(
                int miniGameId,
                int levelId,
                String levelName
            ) {

                if (miniGameId == 1) {
                    game.showVaseBreaker(levelId);
                }
                else if (miniGameId == 2) {
                    game.showWallnutBowling(levelId);
                }
            }
        };

        Skin skin = PvzSkin.get();
        stage = new Stage(new FitViewport(1280,720));

        backgroundTexture = new Texture(
            Gdx.files.internal("IMAGES/Menus/travel/travelBackground.png")
        );

        background = new Image(backgroundTexture);
        background.setScaling(Scaling.stretch);
        background.setBounds(
            0,
            0,
            stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight()
        );

        stage.addActor(background);

        Table root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);

        Table topBar = new Table();
        currencyHeader = new CurrencyHeader();
        topBar.add(currencyHeader).right().padRight(10);
        root.add(topBar).fillX().height(45).pad(10, 0, 0, 0).row();

        ImageButton questButton = new ImageButton(skin, "hud_quests");
        questButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showQuest();
            }
        });

        Texture leaderboardTexture = new Texture(
            Gdx.files.internal("IMAGES/Menus/leaderBoard/LBicon.png")
        );

        TextureRegionDrawable leaderboardDrawable =
            new TextureRegionDrawable(leaderboardTexture);

        ImageButton leaderboardButton =
            new ImageButton(leaderboardDrawable);

        leaderboardButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showLeaderboard();
            }
        });

        ImageButton backButton = new ImageButton(skin, "generic_close_circle");
        backButton.addListener(new  ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showGame();
            }
        });

        int columns = 1;
        int rows = 1;

        float startX = -10;
        float startY = 200;

        float gapX = 50;
        float gapY = 100;

        float distance = 250;

        for (int j = 0; j < 5; j++){

            for(int i = 0; i < 3; i++){

                int miniGameLevel = i + 1;

                int miniGameId = 0;

                if (j == 0) {
                    miniGameId = 1; // Vase
                }
                else if (j == 1) {
                    miniGameId = 2; // Wallnuts
                }
                else if (j == 2) {
                    miniGameId = 3; // Izombie
                }
                else if (j == 3) {
                    miniGameId = 4; // Beghouled
                }
                else if (j == 4) {
                    miniGameId = 5; // Zombotany
                }

                String levelName = "";

                if (miniGameId == 1) {
                    levelName = "Vase - Day " + (i + 1);
                }
                else if (miniGameId == 2) {
                    levelName = "Wallnuts - Day " + (i + 1);
                }
                else if (miniGameId == 3) {
                    levelName = "Izombie - Day " + (i + 1);
                }
                else if (miniGameId == 4) {
                    levelName = "Beghouled - Day " + (i + 1);
                }
                else if (miniGameId == 5) {
                    levelName = "Zombotany - Day " + (i + 1);
                }


                LevelNodeActor node =
                    new LevelNodeActor(
                        miniGameId,
                        i + 1,
                        levelName,
                        listener
                    );

                int col = columns + i;
                int row = rows;

                float x = startX + col * gapX;
                float y = startY - row * gapY;


                float scaleX =
                    stage.getViewport().getWorldWidth() / MAP_WIDTH;

                float scaleY =
                    stage.getViewport().getWorldHeight() / MAP_HEIGHT;

                node.setSize(35.4f, 23.28f);

                node.setPosition(
                    x * scaleX,
                    y * scaleY +10
                );


                stage.addActor(node);
            }

            startX += distance;
        }

        Table topLeft = new Table();
        topLeft.setFillParent(true);
        topLeft.top().left();
        topLeft.padTop(20);
        topLeft.padLeft(20);

        topLeft.add(questButton)
            .width(180)
            .padRight(15);

        topLeft.add(leaderboardButton)
            .width(89.4f)
            .height(26.1f);

        stage.addActor(topLeft);


        Table topRight = new Table();
        topRight.setFillParent(true);
        topRight.top().right();
        topRight.padTop(20);
        topRight.padRight(20);

        topRight.add(backButton)
            .width(180);

        stage.addActor(topRight);

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (currencyHeader != null) {
            currencyHeader.updateValues();
        }
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

        background.setBounds(
            0,
            0,
            stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight()
        );
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
        backgroundTexture.dispose();
    }
}
