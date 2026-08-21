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
import com.badlogic.gdx.math.Vector2;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.CollectionMenu;
import com.workshop.model.plants.Plant;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Toast;
import com.workshop.view.components.CurrencyHeader;
import com.workshop.view.gameplay.ZombieAnimationResolver;
import com.workshop.view.gameplay.ZombieAnimationSpec;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class CollectionScreen implements Screen {

    public interface Listener {
        void onBack();
        void onNavigateToScreen(Screen screen);
    }

    private static final float BASE_WIDTH = 1280f;
    private static final float BASE_HEIGHT = 720f;
    public static TextureBank textureBank;
    public static PamPlayer pamPlayer;
    private static ZombieAnimationResolver zombieAnimationResolver;
    private final GameContext ctx;
    private final Stage stage;
    private final Skin skin;
    private final Listener listener;
    private final CollectionMenu menuLogic;
    private User currentUser;
    private Table rootTable;
    private Table gridTable;
    private SelectBox<String> filterBox;
    private CurrencyHeader currencyHeader;
    private boolean showingPlants = true;
    private Texture fallbackCardBg;
    private Texture menuBgTexture;
    private float stateTime = 0f;
    private SpriteBatch batch;

    public CollectionScreen(GameContext ctx, Listener listener) {
        this.ctx = ctx;
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());
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
        if (zombieAnimationResolver == null) {
            zombieAnimationResolver = new ZombieAnimationResolver();
        }
    }

    private float getScaleFactor() {
        float scaleX = (float) Gdx.graphics.getWidth() / BASE_WIDTH;
        float scaleY = (float) Gdx.graphics.getHeight() / BASE_HEIGHT;
        return Math.max(0.8f, Math.min(scaleX, scaleY)) * 1.0f;
    }

    private void buildUI() {
        stage.clear();
        rootTable = new Table();
        rootTable.setFillParent(true);

        if (menuBgTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(0.35f, 0.15f, 0.08f, 1f);
            pixmap.fill();
            menuBgTexture = new Texture(pixmap);
            pixmap.dispose();
        }

        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(menuBgTexture)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        stage.addActor(rootTable);

        float scale = getScaleFactor();

        Table header = new Table();
        ImageButton closeButton = new ImageButton(skin, "generic_close_circle");
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });
        header.add(closeButton).width(110 * scale).height(45 * scale).left().pad(10 * scale);

        Label titleLabel = new Label("ALMANAC OF PLANTS & ZOMBIES", skin, "big");
        header.add(titleLabel).expandX().center();

        currencyHeader = new CurrencyHeader();
        header.add(currencyHeader).right().padRight(15 * scale);
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

        tabTable.add(plantsTabBtn).width(140 * scale).height(45 * scale).pad(5 * scale);
        tabTable.add(zombiesTabBtn).width(140 * scale).height(45 * scale).pad(5 * scale);
        tabTable.add(filterBox).width(160 * scale).height(40 * scale).padLeft(30 * scale);
        rootTable.add(tabTable).left().pad(10 * scale).row();

        Table divider = new Table();
        if (skin.has("image_ui_almanac_general_line_10", Drawable.class)) {
            divider.setBackground(skin.getDrawable("image_ui_almanac_general_line_10"));
        } else {
            Pixmap pixmap = new Pixmap(1, 2, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(0.6f, 0.4f, 0.2f, 0.8f));
            pixmap.fill();
            Texture lineTexture = new Texture(pixmap);
            pixmap.dispose();
            divider.setBackground(new TextureRegionDrawable(new TextureRegion(lineTexture)));
        }
        rootTable.add(divider).fillX().height(2 * scale).padLeft(10 * scale).padRight(10 * scale).padBottom(5 * scale).row();

        gridTable = new Table();
        gridTable.top().left();
        ScrollPane scrollPane = createAlmanacScrollPane(gridTable);

        rootTable.add(scrollPane).grow().pad(10 * scale);

        if (showingPlants) {
            refreshPlantsList();
        } else {
            refreshZombiesList();
        }
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

    private void updateUserData() {
        this.currentUser = UserManager.getInstance().getCurrentUser();
        if (currencyHeader != null) {
            currencyHeader.updateValues();
        }
    }

    private void refreshPlantsList() {
        gridTable.clear();
        updateUserData();

        if (DataManager.getInstance().plants == null || DataManager.getInstance().plants.getPlantDataMap() == null) {
            Toast.showError(stage, skin, "Plant data not available!");
            return;
        }

        List<Plant> allPlants = new ArrayList<>(DataManager.getInstance().plants.getPlantDataMap().values());
        List<Plant> unlockedPlants = currentUser != null ? currentUser.getUnlockedPlantTypes() : new ArrayList<>();
        String filter = filterBox.getSelected();

        int maxCols = 6;
        int col = 0;
        float scale = getScaleFactor();

        for (Plant plant : allPlants) {
            boolean isUnlocked = unlockedPlants.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(plant.getName()));

            int currentSeeds = currentUser != null ? currentUser.getSeedCount(plant.getName()) : 0;
            int seedsNeeded = 5 * (plant.getLevel() + 1);
            boolean canUpgrade = isUnlocked && plant.getLevel() < 4 && currentSeeds >= seedsNeeded;

            if ("Unlocked".equals(filter) && !isUnlocked) continue;
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
                    batch.setColor(0.35f, 0.35f, 0.35f, 0.85f);
                } else {
                    batch.setColor(Color.WHITE);
                }

                String rawName = plant.getName().toUpperCase();
                String folderName = rawName.replace(" ", "").replace("-", "");
                if (folderName.equalsIgnoreCase("PRIMALPOTATOMINE")) {
                    folderName = "PRIMAL_POTATOMINE";
                }

                Vector2 stagePos = localToStageCoordinates(new Vector2(0, 0));
                float drawX = stagePos.x + getWidth() / 2f;
                float drawY = stagePos.y + 12f * scale;

                String pamPath = "PLANT/" + folderName + "/" + folderName + ".PAM";
                if (!pamPath.equals("PLANT/CATTAILMINT/CATTAILMINT.PAM") && !pamPath.equals("PLANT/CATTAIL/CATTAIL.PAM")) {
                    List<String> preferredClips = new ArrayList<>();

                    if (folderName.equalsIgnoreCase("SUNSHROOM") || folderName.equalsIgnoreCase("PUFFSHROOM")) {
                        preferredClips.add("idle_stage1");
                    } else if (folderName.contains("MINT")) {
                        preferredClips.add("intro");
                    } else if (folderName.contains("BUSTER")) {
                        preferredClips.add("attack");
                    } else if (folderName.contains("ELECTRICBLUE") || folderName.equalsIgnoreCase("CAULIPOWER")) {
                        preferredClips.add("idle1_1");
                    } else if (folderName.contains("KIWIBEAST")) {
                        preferredClips.add("idle_stage1_");
                    } else if (folderName.contains("DOOMSHROOM")) {
                        preferredClips.add("stage1_spawn");
                    }

                    String[] defaultClips = {"idle", "idle_stage1", "intro", "animation", "anim", "attack", "idle1_1", "stage1_spawn"};
                    for (String c : defaultClips) {
                        if (!preferredClips.contains(c)) {
                            preferredClips.add(c);
                        }
                    }

                    boolean drawn = false;
                    for (String clip : preferredClips) {
                        try {
                            pamPlayer.draw(batch, pamPath, clip, stateTime, drawX, drawY, true);
                            drawn = true;
                            break;
                        } catch (Exception ignored) {
                        }
                    }

                    if (!drawn) {
                        try {
                            TextureRegion reg = Textures.regionOrNull("PLANT_" + plant.getName().toUpperCase().replace(" ", "_"));
                            if (reg != null) {
                                batch.draw(reg, drawX - 25 * scale, drawY, 50 * scale, 50 * scale);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                batch.setColor(Color.WHITE);
                if (!isUnlocked) {
                    String lockPamPath = "768/FULL/UI/LOCK_ANIMS/LOCK_ANIMS.PAM";
                    try {
                        pamPlayer.draw(batch, lockPamPath, "locked", stateTime, drawX + 230f, drawY + 60f * scale, true);
                    } catch (Exception ignored) {
                    }
                }
            }
        };

        stack.add(pamContainer);

        card.add(stack).size(130 * scale, 95 * scale).padTop(10 * scale).row();

        Label nameLbl = createSafeLabel(plant.getName(), "default");
        nameLbl.setWrap(true);
        nameLbl.setAlignment(Align.center);
        card.add(nameLbl).width(150 * scale).height(32 * scale).center().padTop(6 * scale).row();

        Label levelLbl = createSafeLabel("Lvl " + plant.getLevel(), "default");
        card.add(levelLbl).padTop(4 * scale).row();

        ProgressBar seedBar = createAlmanacProgressBar(seedsNeeded, currentSeeds);
        card.add(seedBar).width(135 * scale).height(12 * scale).padTop(6 * scale).row();

        Label seedsTxt = createSafeLabel(currentSeeds + "/" + seedsNeeded, "default");
        card.add(seedsTxt).padTop(4 * scale).padBottom(10 * scale).row();

        card.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (listener != null) {
                    PlantDetailsScreen detailsScreen = new PlantDetailsScreen(
                        ctx,
                        plant,
                        pamPlayer,
                        new PlantDetailsScreen.Listener() {
                            @Override
                            public void onBack() {
                                listener.onNavigateToScreen(CollectionScreen.this);
                            }

                            @Override
                            public void onPlantUpdated() {
                                refreshPlantsList();
                            }
                        }
                    );
                    listener.onNavigateToScreen(detailsScreen);
                } else {
                    Toast.showError(stage, skin, "Navigation Listener is null!");
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
        updateUserData();

        if (DataManager.getInstance().zombies == null || DataManager.getInstance().zombies.getZombieDataMap() == null) {
            Toast.showError(stage, skin, "Zombie data not available!");
            return;
        }

        List<Zombie> allZombies = new ArrayList<>(DataManager.getInstance().zombies.getZombieDataMap().values());
        List<Zombie> seenZombies = currentUser != null ? currentUser.getSeenZombies() : new ArrayList<>();

        int maxCols = 6;
        int col = 0;
        float scale = getScaleFactor();

        for (Zombie zombie : allZombies) {
            boolean isSeen = seenZombies.stream()
                .anyMatch(z -> z.getName().equalsIgnoreCase(zombie.getName()));

            Table card = createZombieCard(zombie, isSeen, scale);
            gridTable.add(card).size(160 * scale, 210 * scale).pad(8 * scale);

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
            String seasonName = null;
            ZombieAnimationSpec spec = zombieAnimationResolver.resolve(zombie, seasonName);

            Table pamContainer = new Table() {
                @Override
                public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
                    super.draw(batch, parentAlpha);
                    if (spec == null) return;

                    String pamPath = spec.getPamPath();
                    String idleClip = spec.getIdleClip();
                    if (pamPath == null || idleClip == null) return;

                    textureBank.update();

                    Vector2 stagePos = localToStageCoordinates(new Vector2(0, 0));
                    float drawX = stagePos.x + getWidth() / 2f;
                    float drawY = stagePos.y + 15f * scale;

                    try {
                        pamPlayer.draw(batch, pamPath, idleClip, stateTime, drawX, drawY, true);
                    } catch (Exception ignored) {
                    }
                }
            };

            card.add(pamContainer).size(140 * scale, 135 * scale).padTop(10 * scale).row();

            Label nameLbl = createSafeLabel(zombie.getName(), "default");
            nameLbl.setWrap(true);
            nameLbl.setAlignment(Align.center);
            card.add(nameLbl).width(140 * scale).height(35 * scale).center().padBottom(8 * scale).row();

            card.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (listener != null) {
                        listener.onNavigateToScreen(new ZombieDetailsScreen(
                            zombie,
                            pamPlayer,
                            textureBank,
                            () -> listener.onNavigateToScreen(CollectionScreen.this)
                        ));
                    } else {
                        Toast.showError(stage, skin, "Navigation Listener is null!");
                    }
                }
            });
        } else {
            Label questionMark = createSafeLabel("?", "default");
            questionMark.setFontScale(2.5f * scale);
            questionMark.setColor(Color.GRAY);
            card.add(questionMark).expand().center();

            card.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Toast.showError(stage, skin, "Zombie not encountered yet!");
                }
            });
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
        updateUserData();
        if (showingPlants) {
            refreshPlantsList();
        } else {
            refreshZombiesList();
        }
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
}
