package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.CollectionMenu;
import com.workshop.model.menus.allmenus.PlantSelectionMenu;
import com.workshop.model.plants.Plant;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;
import com.workshop.view.components.CurrencyHeader;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;
import com.workshop.view.widgets.PlantCardActor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlantSelectionScreen implements Screen {

    public interface Listener {
        void onBack();
        void onStartBattle();
    }

    private static final int MAX_SELECTED_PLANTS = 8;
    private static final float BASE_WIDTH = 1280f;
    private static final float BASE_HEIGHT = 720f;

    private static TextureBank textureBank;
    private static PamPlayer pamPlayer;

    private final GameContext ctx;
    private final Listener listener;
    private final Stage stage;
    private final Skin skin;
    private final PlantSelectionMenu menuLogic;
    private final CollectionMenu collectionMenu;
    private final User currentUser;

    private Table sidebarSlotsTable;
    private Table availableGrid;
    private Table detailPanel;
    private Texture fallbackCardBg;
    private CurrencyHeader currencyHeader;
    private Image bg;

    private Plant focusedPlant = null;
    private float globalAnimTime = 0f;

    private final Map<String, PlantCardActor> availableCards = new LinkedHashMap<>();
    private final List<PlantCardActor> activeSlotActors = new ArrayList<>();

    public PlantSelectionScreen(GameContext ctx, Listener listener) {
        this.ctx = ctx;
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());
        this.menuLogic = new PlantSelectionMenu(ctx);
        this.collectionMenu = new CollectionMenu(ctx);
        this.currentUser = UserManager.getInstance().getCurrentUser();

        initPvzLibrary();
        buildUI();
        refreshAll();
    }

    private void initPvzLibrary() {
        if (textureBank == null || pamPlayer == null) {
            FileHandle assetsFolder = Gdx.files.internal("assets");
            textureBank = new TextureBank("768", assetsFolder);
            pamPlayer = new PamPlayer(textureBank, assetsFolder);
        }
    }

    private void buildUI() {
        stage.clear();

        String bgTexturePath = "IMAGES/Menus/selection/selectionBG.png";
        if (Gdx.files.internal(bgTexturePath).exists()) {
            Texture bgTexture = new Texture(Gdx.files.internal(bgTexturePath));
            bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        } else {
            Gdx.app.error("PlantSelectionScreen", "Background texture not found at: " + bgTexturePath);
            bg = new Image(createWhiteDrawable(Color.valueOf("0d1b3e")));
        }
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(6);
        stage.addActor(root);

        Table bodyTable = new Table();
        bodyTable.defaults().pad(4);

        Table leftSidebar = new Table();
        leftSidebar.setBackground(getFallbackCardBackground());

        ImageButton closeButton = new ImageButton(skin, "generic_close_circle");
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });
        leftSidebar.add(closeButton).size(60, 30).padTop(4).row();

        Label sidebarTitle = createSafeLabel("SLOTS", "big");
        sidebarTitle.setFontScale(0.35f);
        leftSidebar.add(sidebarTitle).padBottom(2).row();

        sidebarSlotsTable = new Table();
        leftSidebar.add(sidebarSlotsTable).expandY().top().padTop(2).row();
        bodyTable.add(leftSidebar).width(130).growY().padRight(4);

        Table mainArea = new Table();
        mainArea.defaults().pad(4);

        Table topBar = new Table();
        currencyHeader = new CurrencyHeader();
        topBar.add(currencyHeader).right().padRight(10);
        mainArea.add(topBar).fillX().height(45).pad(0, 0, 4, 0).row();

        detailPanel = new Table();
        detailPanel.setBackground(getFallbackCardBackground());
        mainArea.add(detailPanel).fillX().height(260).padBottom(4).row();

        availableGrid = new Table();
        availableGrid.defaults().pad(3);
        ScrollPane scrollPane = new ScrollPane(availableGrid, skin);
        scrollPane.setFadeScrollBars(false);
        mainArea.add(scrollPane).grow().row();

        TextButton letsRockBtn = new TextButton("LET'S ROCK!", skin, "purple");
        letsRockBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String result = menuLogic.startGame();
                if (ctx.isBattleStarted()) {
                    if (listener != null) listener.onStartBattle();
                } else {
                    Toast.showError(stage, skin, result != null && !result.isEmpty() ? result : "Cannot start battle!");
                }
            }
        });
        mainArea.add(letsRockBtn).width(180).height(40).right().padTop(2);

        bodyTable.add(mainArea).grow();
        root.add(bodyTable).grow();
    }

    private Drawable createWhiteDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private void refreshAll() {
        if (currencyHeader != null) currencyHeader.updateValues();
        List<Plant> allUnlocked = currentUser != null ? currentUser.getUnlockedPlantTypes() : new ArrayList<>();
        if (focusedPlant == null && !allUnlocked.isEmpty()) focusedPlant = allUnlocked.get(0);

        rebuildDetailPanel();
        refreshAvailableGrid(allUnlocked);
        refreshSidebarSlots();
    }

    private void rebuildDetailPanel() {
        detailPanel.clearChildren();
        if (focusedPlant == null) return;

        Table animTable = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + 10f;
                String rawName = focusedPlant.getName().toUpperCase().replace(" ", "").replace("-", "");
                try {
                    pamPlayer.draw(batch, "PLANT/" + rawName + "/" + rawName + ".PAM", "idle", globalAnimTime, drawX, drawY, true);
                } catch (Exception e) {
                    TextureRegion reg = Textures.regionOrNull("PLANT_" + focusedPlant.getName().toUpperCase().replace(" ", "_"));
                    if (reg != null) batch.draw(reg, drawX - 25, drawY, 50, 50);
                }
            }
        };
        detailPanel.add(animTable).size(100, 100).padLeft(50).padRight(50);

        Table infoColumn = new Table();
        infoColumn.top().left();

        Label nameLbl = createSafeLabel(focusedPlant.getName() + " (LVL " + focusedPlant.getLevel() + ")", "big");
        nameLbl.setFontScale(1f);
        infoColumn.add(nameLbl).left().padBottom(6).row();

        Table statsGrid = new Table();
        statsGrid.defaults().left().padRight(20).padBottom(4);
        statsGrid.add(createSafeLabel("SUN COST: " + focusedPlant.getSunCost(), "big"));
        statsGrid.add(createSafeLabel("RECHARGE: " + focusedPlant.getRechargeTime() + "s", "big")).row();
        statsGrid.add(createSafeLabel("HP: " + focusedPlant.getBaseHp(), "big"));
        statsGrid.add(createSafeLabel("DAMAGE: " + focusedPlant.getDamage(), "big")).row();

        for (Cell<?> cell : statsGrid.getCells()) {
            if (cell.getActor() instanceof Label) {
                ((Label) cell.getActor()).setFontScale(0.8f);
            }
        }
        infoColumn.add(statsGrid).left().padBottom(8).row();

        Table btnRow = new Table();

        Plant activeInst = getActivePlantInstance(focusedPlant);
        boolean isBoosted = (activeInst != null && activeInst.isPlantFoodActive()) || focusedPlant.isPlantFoodActive();

        TextButton boostBtn = new TextButton(isBoosted ? "BOOSTED" : "BOOST (2 Gems)", skin, "green");
        boostBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (isBoosted) {
                    Toast.showError(stage, skin, "Plant is already boosted!");
                    return;
                }
                if (currentUser.getGems() < 2) {
                    Toast.showError(stage, skin, "You don't have enough gems.");
                    return;
                }
                currentUser.setGems(currentUser.getGems() - 2);
                focusedPlant.setPlantFoodActive(true);
                if (activeInst != null) activeInst.setPlantFoodActive(true);
                Toast.showSuccess(stage, skin, "Successfully boosted " + focusedPlant.getName() + "!");
                refreshAll();
            }
        });

        boolean isMaxLevel = focusedPlant.getLevel() >= 4;
        int upgradeCost = isMaxLevel ? 0 : focusedPlant.getLevel() * 100 + 100;
        String upgradeText = isMaxLevel ? "MAX LEVEL" : "UPGRADE (" + upgradeCost + " Coins)";
        TextButton upgradeBtn = new TextButton(upgradeText, skin, "purple");
        upgradeBtn.setDisabled(isMaxLevel);

        upgradeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String result = collectionMenu.upgradePlant(focusedPlant.getName());
                if (result != null && result.toLowerCase().contains("success")) {
                    Toast.showSuccess(stage, skin, result);
                } else {
                    Toast.showError(stage, skin, result != null ? result : "Upgrade failed!");
                }
                refreshAll();
            }
        });

        btnRow.add(upgradeBtn).width(130).height(30).padRight(10);
        btnRow.add(boostBtn).width(130).height(30);

        infoColumn.add(btnRow).left();
        detailPanel.add(infoColumn).expand().left().pad(5);
    }

    private Table createPlantCard(Plant plant, boolean isUnlocked, int currentSeeds, int seedsNeeded, float scale) {
        Table card = new Table();
        card.top();

        Plant activeInst = getActivePlantInstance(plant);
        boolean isBoosted = (activeInst != null && activeInst.isPlantFoodActive()) || plant.isPlantFoodActive();

        if (isBoosted) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.GOLD);
            pixmap.fill();
            Texture goldTex = new Texture(pixmap);
            goldTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pixmap.dispose();
            card.setBackground(new TextureRegionDrawable(new TextureRegion(goldTex)));
        } else if (skin.has("PlantAlmanacBorder", Drawable.class)) {
            card.setBackground(skin.getDrawable("PlantAlmanacBorder"));
        } else {
            card.setBackground(getFallbackCardBackground());
        }

        Stack stack = new Stack();

        Table pamContainer = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                textureBank.update();

                if (!isUnlocked) {
                    batch.setColor(0.3f, 0.3f, 0.3f, 1f);
                } else {
                    batch.setColor(Color.WHITE);
                }

                String rawName = plant.getName().toUpperCase();
                String folderName = rawName.replace(" ", "").replace("-", "");
                if (folderName.equalsIgnoreCase("PRIMALPOTATOMINE")) {
                    folderName = "PRIMAL_POTATOMINE";
                }
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + 8f * scale;
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
                        pamPlayer.draw(batch, pamPath, "idle", globalAnimTime, drawX, drawY, true);
                        drawn = true;
                        break;
                    } catch (Exception ignored) {
                    }
                }

                if (!drawn) {
                    TextureRegion reg = Textures.regionOrNull("PLANT_" + rawName.replace(" ", "_"));
                    if (reg != null) {
                        batch.draw(reg, drawX - 25 * scale, drawY, 50 * scale, 50 * scale);
                    }
                }

                batch.setColor(Color.WHITE);
            }
        };

        stack.add(pamContainer);
        card.add(stack).size(110 * scale, 75 * scale).padTop(6 * scale).row();

        Label nameLbl = createSafeLabel(plant.getName(), "default");
        nameLbl.setWrap(true);
        nameLbl.setAlignment(Align.center);
        card.add(nameLbl).width(120 * scale).height(24 * scale).center().padTop(2 * scale).row();

        Label levelLbl = createSafeLabel("Lvl " + plant.getLevel(), "default");
        card.add(levelLbl).padTop(2 * scale).row();

        ProgressBar seedBar = createAlmanacProgressBar(seedsNeeded, currentSeeds);
        card.add(seedBar).width(110 * scale).height(10 * scale).padTop(4 * scale).row();

        Label seedsTxt = createSafeLabel(currentSeeds + "/" + seedsNeeded, "default");
        card.add(seedsTxt).padTop(2 * scale).padBottom(6 * scale).row();

        card.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                focusedPlant = plant;
                if (isPlantSelected(plant)) {
                    menuLogic.removePlant(plant.getName());
                } else if (ctx.getActivePlants().size() < MAX_SELECTED_PLANTS) {
                    menuLogic.addPlant(plant.getName());
                    if (plant.isPlantFoodActive()) {
                        Plant newlyAdded = getActivePlantInstance(plant);
                        if (newlyAdded != null) newlyAdded.setPlantFoodActive(true);
                    }
                } else {
                    Toast.showError(stage, skin, "You can only select up to " + MAX_SELECTED_PLANTS + " plants!");
                }
                refreshAll();
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

    private void refreshAvailableGrid(List<Plant> allUnlocked) {
        availableGrid.clear();
        availableCards.clear();
        int maxCols = 6;
        int col = 0;
        float scale = 0.75f;

        for (Plant plant : allUnlocked) {
            int currentSeeds = currentUser != null ? currentUser.getSeedCount(plant.getName()) : 0;
            int seedsNeeded = 5 * (plant.getLevel() + 1);

            Table card = createPlantCard(plant, true, currentSeeds, seedsNeeded, scale);

            if (isPlantSelected(plant)) {
                card.setColor(Color.GREEN);
            }

            availableGrid.add(card).size(135 * scale * 1.3f, 175 * scale * 1.3f).pad(4);
            col++;
            if (col % maxCols == 0) availableGrid.row();
        }
    }

    private void refreshSidebarSlots() {
        sidebarSlotsTable.clear();
        activeSlotActors.clear();
        List<Plant> selected = new ArrayList<>(ctx.getActivePlants());
        for (Plant plant : selected) {
            PlantCardActor card = new PlantCardActor(plant, pamPlayer, textureBank, skin, PlantCardActor.Mode.SLOT);
            card.setBoosted(plant.isPlantFoodActive());
            card.setOnClick(c -> {
                focusedPlant = plant;
                menuLogic.removePlant(plant.getName());
                refreshAll();
            });
            activeSlotActors.add(card);
            Container<PlantCardActor> slotContainer = new Container<>(card);
            slotContainer.size(110, 100);
            sidebarSlotsTable.add(slotContainer).padBottom(6).row();
        }
        for (int i = selected.size(); i < MAX_SELECTED_PLANTS; i++) {
            sidebarSlotsTable.add(buildEmptySlot()).size(110, 48).padBottom(6).row();
        }
    }

    private Table buildEmptySlot() {
        Table empty = new Table();
        empty.setBackground(getFallbackCardBackground());
        empty.setColor(0.2f, 0.2f, 0.2f, 0.4f);
        return empty;
    }

    private boolean isPlantSelected(Plant plant) {
        return ctx.getActivePlants().stream().anyMatch(p -> p.getName().equalsIgnoreCase(plant.getName()));
    }

    private Plant getActivePlantInstance(Plant plant) {
        return ctx.getActivePlants().stream()
            .filter(p -> p.getName().equalsIgnoreCase(plant.getName()))
            .findFirst()
            .orElse(null);
    }

    private Drawable getFallbackCardBackground() {
        if (fallbackCardBg == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(0, 0, 0, 0.6f));
            pixmap.fill();
            fallbackCardBg = new Texture(pixmap);
            fallbackCardBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pixmap.dispose();
        }
        return new TextureRegionDrawable(new TextureRegion(fallbackCardBg));
    }

    private Label createSafeLabel(String text, String style) {
        return skin.has(style, Label.LabelStyle.class) ? new Label(text, skin, style) : new Label(text, skin);
    }

    @Override
    public void render(float delta) {
        globalAnimTime += delta;
        for (PlantCardActor card : availableCards.values()) card.updateAnimation(delta);
        for (PlantCardActor card : activeSlotActors) card.updateAnimation(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (bg != null) {
            bg.setSize(width, height);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        refreshAll();
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (bg != null && bg.getDrawable() instanceof TextureRegionDrawable) {
            ((TextureRegionDrawable) bg.getDrawable()).getRegion().getTexture().dispose();
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
