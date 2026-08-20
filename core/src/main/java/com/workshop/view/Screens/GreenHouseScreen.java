package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.workshop.model.GameContext;
import com.workshop.model.GreenHouseData.GreenHouse;
import com.workshop.model.GreenHouseData.Pot;
import com.workshop.model.menus.allmenus.GreenHouseMenu;
import com.workshop.model.user.UserManager;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

public class GreenHouseScreen implements Screen {

    public interface Listener {
        void onBack();
    }

    private static final String PAM_ASSETS_PATH = "assets";

    private static final float VIRTUAL_WIDTH = 1024f;
    private static final float VIRTUAL_HEIGHT = 576f;

    private final GameContext ctx;
    private final Listener listener;

    private GreenHouseMenu greenHouseMenu;
    private GreenHouse greenHouse;
    private Stage stage;
    private Skin skin;
    private Texture background;
    private PotActor[][] potActors;

    private TextureBank textureBank;
    private PamPlayer pamPlayer;

    private static final float[][] SLOT_OFFSET_X = {
        {80, 280, 480, 680},
        {80, 280, 480, 680},
        {80, 280, 480, 680}
    };
    private static final float[][] SLOT_OFFSET_Y = {
        {380, 380, 380, 380},
        {220, 220, 220, 220},
        {60, 60, 60, 60}
    };

    public GreenHouseScreen(GameContext ctx, Listener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    @Override
    public void show() {
        greenHouseMenu = new GreenHouseMenu(ctx);
        greenHouse = UserManager.getInstance().getCurrentUser().getGreenHouse();

        skin = PvzSkin.get();
        background = new Texture(Gdx.files.internal("IMAGES/Menus/GreenHouse/greenhouse_bg.png"));

        FileHandle assetsFolder = Gdx.files.internal("assets");
        textureBank = new TextureBank("768", assetsFolder);
        pamPlayer = new PamPlayer(textureBank, Gdx.files.internal(PAM_ASSETS_PATH));

        stage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
        potActors = new PotActor[GreenHouse.ROWS][GreenHouse.COLS];
        Gdx.input.setInputProcessor(stage);

        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(background)));
        bg.setFillParent(true);
        stage.addActor(bg);

        PotActor.Listener potListener = new PotActor.Listener() {
            @Override public void onBuy(int x, int y) { handleResult(greenHouseMenu.buyPot(x, y), x, y); }
            @Override public void onPlant(int x, int y) { handleResult(greenHouseMenu.plantPot(x, y), x, y); }
            @Override public void onFasterGrow(int x, int y) { handleResult(greenHouseMenu.growPlant(x, y), x, y); }
            @Override public void onCollect(int x, int y) {
                String result = greenHouseMenu.collectPlant(x, y);
                potActors[x][y].refresh();
                showRewardDialog(result);
            }
        };

        for (int i = 0; i < GreenHouse.ROWS; i++) {
            for (int j = 0; j < GreenHouse.COLS; j++) {
                Pot pot = greenHouse.getPot(i, j);
                PotActor actor = new PotActor(i, j, pot, pamPlayer, textureBank, skin, potListener);
                actor.setPosition(SLOT_OFFSET_X[i][j], SLOT_OFFSET_Y[i][j]);
                potActors[i][j] = actor;
                stage.addActor(actor);
            }
        }

        TextButton backButton = new TextButton("Back", skin);
        backButton.setPosition(20, VIRTUAL_HEIGHT - 56);
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                listener.onBack();
            }
        });
        stage.addActor(backButton);
    }

    private void handleResult(String result, int x, int y) {
        potActors[x][y].refresh();
        Gdx.app.log("GreenHouse", result);
    }

    private void showRewardDialog(String rewardText) {
        Dialog dialog = new Dialog("Prize collected!", skin) {
            @Override
            protected void result(Object object) {
                hide();
            }
        };
        dialog.text(rewardText);
        dialog.button("okay", true);
        dialog.show(stage);
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

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        background.dispose();
        if (textureBank != null) textureBank.dispose();
    }
}
