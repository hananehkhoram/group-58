package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.workshop.model.GameContext;
import com.workshop.model.GreenHouseData.GreenHouse;
import com.workshop.model.GreenHouseData.Pot;
import com.workshop.model.menus.allmenus.GreenHouseMenu;
import com.workshop.model.user.UserManager;
import com.workshop.view.components.CurrencyHeader;
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

    private Texture overlayTexture;
    private Texture panelTexture;
    private CurrencyHeader currencyHeader;

    private static final float[][] SLOT_OFFSET_X = {
        {312, 422, 532, 642},
        {312, 422, 532, 642},
        {312, 422, 532, 642}
    };

    // Y offsets updated slightly for optimal spacing between wooden tiles
    private static final float[][] SLOT_OFFSET_Y = {
        {340, 340, 340, 340},
        {215, 215, 215, 215},
        {90, 90, 90, 90}
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
        if (CollectionScreen.textureBank == null || CollectionScreen.pamPlayer == null) {
            CollectionScreen.textureBank = new TextureBank("768", assetsFolder);
            CollectionScreen.pamPlayer = new PamPlayer(CollectionScreen.textureBank, Gdx.files.internal(PAM_ASSETS_PATH));
        }
        textureBank = CollectionScreen.textureBank;
        pamPlayer = CollectionScreen.pamPlayer;

        stage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
        potActors = new PotActor[GreenHouse.ROWS][GreenHouse.COLS];
        Gdx.input.setInputProcessor(stage);

        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(background)));
        bg.setFillParent(true);
        stage.addActor(bg);

        currencyHeader = new CurrencyHeader();
        currencyHeader.setPosition(VIRTUAL_WIDTH - currencyHeader.getPrefWidth() - 20, VIRTUAL_HEIGHT - 50);
        stage.addActor(currencyHeader);

        PotActor.Listener potListener = new PotActor.Listener() {
            @Override public void onBuy(int x, int y) { showStoreRedirectDialog(); }
            @Override public void onPlant(int x, int y) { handleResult(greenHouseMenu.plantPot(x, y), x, y); }
            @Override public void onFasterGrow(int x, int y) { handleResult(greenHouseMenu.growPlant(x, y), x, y); }
            @Override public void onCollect(int x, int y) {
                String result = greenHouseMenu.collectPlant(x, y);
                potActors[x][y].refresh();
                if (currencyHeader != null) currencyHeader.updateValues();
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

        TextButton backButton = new TextButton("Back", skin, "green_small");
        backButton.setPosition(55, VIRTUAL_HEIGHT - 65);
        backButton.setSize(100, 40);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onBack();
            }
        });
        stage.addActor(backButton);
    }

    private void handleResult(String result, int x, int y) {
        potActors[x][y].refresh();
        if (currencyHeader != null) currencyHeader.updateValues();
        Gdx.app.log("GreenHouse", result);
    }

    private Table buildOverlayRoot(Table panel) {
        Table overlayRoot = new Table();
        overlayRoot.setFillParent(true);

        if (overlayTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(0f, 0f, 0f, 0.6f);
            pixmap.fill();
            overlayTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        overlayRoot.setBackground(new TextureRegionDrawable(new TextureRegion(overlayTexture)));

        overlayRoot.add(panel);
        return overlayRoot;
    }

    private Table buildPanel() {
        if (panelTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(0.18f, 0.12f, 0.08f, 0.97f);
            pixmap.fill();
            panelTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        Table panel = new Table();
        panel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        panel.pad(20);
        return panel;
    }

    private Label createSafeLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        }
        return new Label(text, skin);
    }

    private void showStoreRedirectDialog() {
        Table panel = buildPanel();

        Label titleLbl = createSafeLabel("Locked Slot!", "big");
        panel.add(titleLbl).padBottom(15).row();

        Label messageLbl = createSafeLabel("You need to purchase this pot slot from the Store first!", "default");
        messageLbl.setWrap(true);
        messageLbl.setAlignment(Align.center);
        panel.add(messageLbl).width(320).padBottom(20).row();

        TextButton okBtn = new TextButton("OK", skin, "green_small");

        Table overlayRoot = buildOverlayRoot(panel);

        okBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                overlayRoot.remove();
            }
        });

        panel.add(okBtn).width(120).height(45);
        stage.addActor(overlayRoot);
    }

    private void showRewardDialog(String rewardText) {

        Table panel = buildPanel();

        Label titleLbl = createSafeLabel("Prize Collected!", "big");
        panel.add(titleLbl).padBottom(15).row();

        Label rewardLbl = createSafeLabel(rewardText, "default");
        rewardLbl.setWrap(true);
        rewardLbl.setAlignment(Align.center);
        panel.add(rewardLbl).width(320).padBottom(20).row();

        TextButton okBtn = new TextButton("OK", skin, "green_small");

        Table overlayRoot = buildOverlayRoot(panel);

        okBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                overlayRoot.remove();
            }
        });

        panel.add(okBtn).width(120).height(45);
        stage.addActor(overlayRoot);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        for (int i = 0; i < GreenHouse.ROWS; i++) {
            for (int j = 0; j < GreenHouse.COLS; j++) {
                if (potActors[i][j] != null) {
                    potActors[i][j].update(delta);
                }
            }
        }

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
        if (overlayTexture != null) overlayTexture.dispose();
        if (panelTexture != null) panelTexture.dispose();
    }
}
