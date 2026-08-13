package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.CollectionMenu;
import com.workshop.model.plants.Plant;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.model.zombie.Zombie;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class CollectionScreen implements Screen {

    private static final float BASE_WIDTH = 1280f;
    private static final float BASE_HEIGHT = 720f;
    private static TextureBank textureBank;
    private static PamPlayer pamPlayer;
    private final GameContext ctx;
    private final Stage stage;
    private final Skin skin;
    private final Listener listener;
    private final CollectionMenu menuLogic;
    private final User currentUser;
    private Table rootTable;
    private Table gridTable;
    private SelectBox<String> filterBox;
    private boolean showingPlants = true;
    private Texture fallbackCardBg;
    private Texture menuBgTexture;
    private float stateTime = 0f;
    private SpriteBatch batch;

    public CollectionScreen(GameContext ctx, Listener listener) {
        this.ctx = ctx;
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new FitViewport(BASE_WIDTH, BASE_HEIGHT));
        this.menuLogic = new CollectionMenu(ctx);
        this.currentUser = UserManager.getInstance().getCurrentUser();
        this.batch = new SpriteBatch();

        initPvzLibrary();
        buildUI();
    }

    private void initPvzLibrary() {
        if (textureBank == null || pamPlayer == null) {
            FileHandle assetsFolder = Gdx.files.internal("assets");
            textureBank = new TextureBank("768", assetsFolder);
            pamPlayer = new PamPlayer(textureBank, assetsFolder);
        }
    }

    private void buildUI() {
        rootTable = new Table();
        rootTable.setFillParent(true);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.35f, 0.15f, 0.08f, 1f));
        pixmap.fill();
        menuBgTexture = new Texture(pixmap);
        pixmap.dispose();

        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(menuBgTexture)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        stage.addActor(rootTable);

        Table header = new Table();
        TextButton backBtn = new TextButton("Back", skin, "brown");
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });
        header.add(backBtn).width(110).height(45).left().pad(10);

        Label titleLabel = createSafeLabel("ALMANAC OF PLANTS & ZOMBIES", "big");
        header.add(titleLabel).expandX().center();

        Table currencyTable = new Table();
        long gems = currentUser != null ? currentUser.getGems() : 0;
        long coins = currentUser != null ? currentUser.getCoins() : 0;

        final String diamondPamPath = "768/INITIAL/EFFECTS/COIN_DIAMOND/COIN_DIAMOND.PAM";
        final String coinPamPath = "768/INITIAL/EFFECTS/COIN_STACK/COIN_STACK.PAM";
        final float iconScale = 0.3f;

        Table diamondPamContainer = new Table() {
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                textureBank.update();

                Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + getHeight() / 2f;

                Matrix4 scaleMatrix = oldMatrix.cpy();
                scaleMatrix.translate(drawX, drawY, 0);
                scaleMatrix.scale(iconScale, iconScale, 1f);
                scaleMatrix.translate(-drawX, -drawY, 0);
                batch.setTransformMatrix(scaleMatrix);

                try {
                    pamPlayer.draw(batch, diamondPamPath, "idle", stateTime, drawX, drawY, true);
                } catch (Exception ignored) {
                }

                batch.setTransformMatrix(oldMatrix);
            }
        };

        currencyTable.add(diamondPamContainer).size(35, 35).padRight(6);

        Label gemLbl = createSafeLabel(String.valueOf(gems), "big");
        gemLbl.setFontScale(0.55f);
        currencyTable.add(gemLbl).padRight(25);

        Table coinPamContainer = new Table() {
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                textureBank.update();

                Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + getHeight() / 2f;

                Matrix4 scaleMatrix = oldMatrix.cpy();
                scaleMatrix.translate(drawX, drawY, 0);
                scaleMatrix.scale(iconScale, iconScale, 1f);
                scaleMatrix.translate(-drawX, -drawY, 0);
                batch.setTransformMatrix(scaleMatrix);


                try {
                    pamPlayer.draw(batch, coinPamPath, "idle", stateTime, drawX, drawY, true);
                } catch (Exception ignored) {
                }

                batch.setTransformMatrix(oldMatrix);
            }
        };

        currencyTable.add(coinPamContainer).size(35, 35).padRight(6);

        Label coinLbl = createSafeLabel(String.valueOf(coins), "big");
        coinLbl.setFontScale(0.55f);
        currencyTable.add(coinLbl);

        header.add(currencyTable).right().padRight(15);
        rootTable.add(header).fillX().row();

        Table tabTable = new Table();
        TextButton plantsTabBtn = new TextButton("Plants", skin, "purple");
        TextButton zombiesTabBtn = new TextButton("Zombies", skin, "purple");

        filterBox = new SelectBox<>(skin);
        filterBox.setItems("All", "Unlocked", "Locked", "Upgradable");
        filterBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (showingPlants) refreshPlantsList();
            }
        });

        plantsTabBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showingPlants = true;
                filterBox.setVisible(true);
                refreshPlantsList();
            }
        });

        zombiesTabBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showingPlants = false;
                filterBox.setVisible(false);
                refreshZombiesList();
            }
        });

        tabTable.add(plantsTabBtn).width(140).height(45).pad(5);
        tabTable.add(zombiesTabBtn).width(140).height(45).pad(5);
        tabTable.add(filterBox).width(160).height(40).padLeft(30);
        rootTable.add(tabTable).left().pad(10).row();

        gridTable = new Table();
        gridTable.top().left();
        ScrollPane scrollPane = createAlmanacScrollPane(gridTable);

        rootTable.add(scrollPane).grow().pad(10);
        refreshPlantsList();
    }

    private ScrollPane createAlmanacScrollPane(Actor actor) {
        ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
        if (skin.has("image_ui_almanac_general_scrollbar_bkgd_10", Drawable.class)) {
            style.vScroll = skin.getDrawable("image_ui_almanac_general_scrollbar_bkgd_10");
        }
        if (skin.has("image_ui_almanac_general_scrollbar_10", Drawable.class)) {
            style.vScrollKnob = skin.getDrawable("image_ui_almanac_general_scrollbar_10");
        }

        ScrollPane scrollPane = new ScrollPane(actor, style.vScroll != null ? style : skin.get(ScrollPane.ScrollPaneStyle.class));
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        return scrollPane;
    }

    private void refreshPlantsList() {
        gridTable.clear();

        List<Plant> allPlants = new ArrayList<>(DataManager.getInstance().plants.getPlantDataMap().values());
        List<Plant> unlockedPlants = currentUser != null ? currentUser.getUnlockedPlantTypes() : new ArrayList<>();
        String filter = filterBox.getSelected();

        int maxCols = 6;
        int col = 0;
        float scale = 1.0f;

        for (Plant plant : allPlants) {
            boolean isUnlocked = unlockedPlants.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(plant.getName()));

            int currentSeeds = currentUser != null ? currentUser.getSeedCount(plant.getName()) : 0;
            int seedsNeeded = 5 * (plant.getLevel() + 1);
            boolean canUpgrade = isUnlocked && plant.getLevel() < 4 && currentSeeds >= seedsNeeded;

            if ("Locked".equals(filter) && isUnlocked) continue;
            if ("Upgradable".equals(filter) && !canUpgrade) continue;

            Table card = createPlantCard(plant, isUnlocked, currentSeeds, seedsNeeded, scale);
            gridTable.add(card).size(165 * scale, 220 * scale).pad(10 * scale);

            col++;
            if (col % maxCols == 0) gridTable.row();
        }
    }

    private Table createPlantCard(Plant plant, boolean isUnlocked, int currentSeeds, int seedsNeeded, float scale) {
        Table card = new Table();
        card.top();

        if (skin.has("PlantAlmanacBorder", Drawable.class)) {
            card.setBackground(skin.getDrawable("PlantAlmanacBorder"));
        } else {
            card.setBackground(getFallbackCardBackground());
        }

        Stack stack = new Stack();

        Table pamContainer = new Table() {
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                textureBank.update();

                if (!isUnlocked) {
                    batch.setColor(0.3f, 0.3f, 0.3f, 1f);
                } else {
                    batch.setColor(Color.WHITE);
                }

                String rawName = plant.getName().toUpperCase();
                String folderName = rawName.replace(" ", "").replace("-", "").replace("-", "");
                if (folderName.equalsIgnoreCase("PRIMALPOTATOMINE")) {
                    folderName = "PRIMAL_POTATOMINE";
                }
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + 12f;
                boolean drawn = false;
                String[] possiblePaths = {
                    "PLANT/" + folderName + "/" + folderName + ".PAM"
                };

                for (String pamPath : possiblePaths) {
                    if (pamPath.equals("PLANT/CATTAILMINT/CATTAILMINT.PAM") ||
                        pamPath.equals("PLANT/CATTAIL/CATTAIL.PAM")) {
                        continue;
                    }
                    try {
                        pamPlayer.draw(batch, pamPath, "idle", stateTime, drawX, drawY, true);
                        drawn = true;
                        break;
                    } catch (Exception ignored) {
                    }
                }

                if (!drawn) {
                    TextureRegion reg = Textures.regionOrNull("PLANT_" + rawName.replace(" ", "_"));
                    if (reg != null) {
                        batch.draw(reg, drawX - 30, drawY, 60, 60);
                    }
                }

                batch.setColor(Color.WHITE);
            }
        };

        stack.add(pamContainer);

        if (!isUnlocked) {
            TextureRegion lockRegion = Textures.regionOrNull("image_ui_generic_buttons_hud_zg_locked");
            if (lockRegion != null) {
                Image lockImg = new Image(lockRegion);
                Table lockWrapper = new Table();
                lockWrapper.add(lockImg).size(30 * scale, 30 * scale).center();
                stack.add(lockWrapper);
            }
        }

        card.add(stack).size(130 * scale, 95 * scale).padTop(10 * scale).row();

        Label nameLbl = createSafeLabel(plant.getName(), "big");
        nameLbl.setFontScale(0.55f * scale);
        nameLbl.setWrap(true);
        nameLbl.setAlignment(Align.center);
        card.add(nameLbl).width(150 * scale).height(32 * scale).center().padTop(6 * scale).row();

        Label levelLbl = createSafeLabel("Lvl " + plant.getLevel(), "big");
        levelLbl.setFontScale(0.52f * scale);
        card.add(levelLbl).padTop(4 * scale).row();

        ProgressBar seedBar = createAlmanacProgressBar(seedsNeeded, currentSeeds);
        card.add(seedBar).width(135 * scale).height(12 * scale).padTop(6 * scale).row();

        Label seedsTxt = createSafeLabel(currentSeeds + "/" + seedsNeeded, "big");
        seedsTxt.setFontScale(0.50f * scale);
        card.add(seedsTxt).padTop(4 * scale).padBottom(10 * scale).row();

        card.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    listener.onNavigateToScreen(new PlantDetailsScreen(
                        ctx,
                        plant,
                        pamPlayer,
                        () -> listener.onNavigateToScreen(CollectionScreen.this)
                    ));
                }
            }
        });

        return card;
    }

    private ProgressBar createAlmanacProgressBar(int seedsNeeded, int currentSeeds) {
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        if (skin.has("image_ui_almanac_plants_plant_fuelbar_10", Drawable.class)) {
            style.background = skin.getDrawable("image_ui_almanac_plants_plant_fuelbar_10");
        } else {
            style.background = skin.get(ProgressBar.ProgressBarStyle.class).background;
        }

        if (skin.has("image_ui_almanac_general_fuelbar_fill_10", Drawable.class)) {
            style.knobBefore = skin.getDrawable("image_ui_almanac_general_fuelbar_fill_10");
        } else {
            style.knobBefore = skin.get(ProgressBar.ProgressBarStyle.class).knobBefore;
        }

        ProgressBar seedBar = new ProgressBar(0, seedsNeeded, 1, false, style);
        seedBar.setValue(Math.min(currentSeeds, seedsNeeded));
        return seedBar;
    }

    private void refreshZombiesList() {
        gridTable.clear();

        List<Zombie> allZombies = new ArrayList<>(DataManager.getInstance().zombies.getZombieDataMap().values());
        List<Zombie> seenZombies = currentUser != null ? currentUser.getSeenZombies() : new ArrayList<>();

        int maxCols = 9;
        int col = 0;
        float scale = 1.0f;

        for (Zombie zombie : allZombies) {
            boolean isSeen = seenZombies.stream()
                .anyMatch(z -> z.getName().equalsIgnoreCase(zombie.getName()));

            Table card = createZombieCard(zombie, isSeen, scale);
            gridTable.add(card).size(125 * scale, 175 * scale).pad(6 * scale);

            col++;
            if (col % maxCols == 0) gridTable.row();
        }
    }

    private Table createZombieCard(Zombie zombie, boolean isSeen, float scale) {
        Table card = new Table();
        card.top();

        if (skin.has("ZombieAlmanacBorder", Drawable.class)) {
            card.setBackground(skin.getDrawable("ZombieAlmanacBorder"));
        } else {
            card.setBackground(getFallbackCardBackground());
        }

        if (isSeen) {
            Table pamContainer = new Table() {
                @Override
                public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                    super.draw(batch, parentAlpha);
                    textureBank.update();

                    String rawName = zombie.getName().toUpperCase();
                    String folderName = rawName.replace(" ", "");

                    float drawX = getX() + getWidth() / 2f;
                    float drawY = getY() + 10f;

                    boolean drawn = false;
                    String[] possiblePaths = {
                        "768/INITIAL/ZOMBIE/" + folderName + "/" + folderName + ".PAM",
                        "768/INITIAL/ZOMBIE/" + rawName.replace(" ", "_") + "/" + rawName.replace(" ", "_") + ".PAM"
                    };

                    for (String pamPath : possiblePaths) {
                        try {
                            pamPlayer.draw(batch, pamPath, "idle", stateTime, drawX, drawY, true);
                            drawn = true;
                            break;
                        } catch (Exception ignored) {
                        }
                    }

                    if (!drawn) {
                        TextureRegion reg = Textures.regionOrNull("ZOMBIE_" + rawName.replace(" ", "_"));
                        if (reg != null) {
                            batch.draw(reg, drawX - 25, drawY, 55, 55);
                        }
                    }
                }
            };
            card.add(pamContainer).size(55 * scale, 55 * scale).padTop(10 * scale).row();

            Label nameLbl = createSafeLabel(zombie.getName(), "big");
            nameLbl.setFontScale(0.58f * scale);
            nameLbl.setWrap(true);
            nameLbl.setAlignment(Align.center);
            card.add(nameLbl).width(115 * scale).height(34 * scale).center().padTop(4 * scale).row();

            card.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (listener != null) {
                        listener.onNavigateToScreen(new ZombieDetailsScreen(
                            zombie,
                            pamPlayer,
                            () -> listener.onNavigateToScreen(CollectionScreen.this)
                        ));
                    }
                }
            });
        } else {
            Label questionMark = createSafeLabel("?", "big");
            questionMark.setFontScale(2.5f * scale);
            questionMark.setColor(Color.GRAY);
            card.add(questionMark).expand().center();
        }

        return card;
    }

    private Drawable getFallbackCardBackground() {
        if (fallbackCardBg == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(0f, 0f, 0f, 0.65f));
            pixmap.fill();
            fallbackCardBg = new Texture(pixmap);
            pixmap.dispose();
        }
        return new TextureRegionDrawable(new TextureRegion(fallbackCardBg));
    }

    private Label createSafeLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        }
        return new Label(text, skin);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.08f, 1);
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
        if (fallbackCardBg != null) fallbackCardBg.dispose();
        if (menuBgTexture != null) menuBgTexture.dispose();
        if (batch != null) batch.dispose();
    }

    public interface Listener {
        void onBack();

        void onNavigateToScreen(Screen screen);
    }
}
