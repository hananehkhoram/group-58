package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.workshop.PvzGame;
import com.workshop.model.user.User;
import com.workshop.view.components.CurrencyHeader;
import pvz.skin.PvzSkin;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class TravelMenuScreen implements Screen {

    private final Stage stage;
    private final Texture backgroundTexture;
    private final Image background;
    private CurrencyHeader currencyHeader;

    public TravelMenuScreen(PvzGame game, User user) {
        Skin skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        backgroundTexture = new Texture(
            Gdx.files.internal("IMAGES/Menus/travel/travelBackground.png")
        );

        background = new Image(backgroundTexture);
        background.setScaling(Scaling.fill);
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

        ImageButton backButton = new ImageButton(skin, "generic_close_circle");
        backButton.addListener(new  ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showGame();
            }
        });

        TextButton vaseBreakerButton =
            new TextButton("Vase Breaker", skin, "purple");

        TextButton wallnutBowlingButton =
            new TextButton("Wallnut Bowling", skin, "purple");

        TextButton zombotanyButton =
            new TextButton("Zombotany", skin, "purple");

        TextButton iZombieButton =
            new TextButton("I, Zombie", skin, "purple");

        TextButton beghouledButton =
            new TextButton("Beghouled", skin, "purple");

        Table miniGameRow = new Table();

        miniGameRow.add(vaseBreakerButton)
            .width(170)
            .padRight(15);

        miniGameRow.add(wallnutBowlingButton)
            .width(170)
            .padRight(15);

        miniGameRow.add(zombotanyButton)
            .width(170)
            .padRight(15);

        miniGameRow.add(iZombieButton)
            .width(170)
            .padRight(15);

        miniGameRow.add(beghouledButton)
            .width(170);

        Table miniGameContainer = new Table();
        miniGameContainer.setFillParent(true);

        miniGameContainer.bottom();
        miniGameContainer.padBottom(70);

        miniGameContainer.add(miniGameRow);

        stage.addActor(miniGameContainer);

        Table topLeft = new Table();
        topLeft.setFillParent(true);
        topLeft.top().left();
        topLeft.padTop(20);
        topLeft.padLeft(20);

        topLeft.add(questButton)
            .width(180);

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
