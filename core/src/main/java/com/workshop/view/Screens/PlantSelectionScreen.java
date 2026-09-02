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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.workshop.controller.repository.Textures;
import com.workshop.controller.SpecialLevelManager.PlantWhatYouGetManager;
import com.workshop.model.GameContext;
import com.workshop.model.level.LevelType;
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
    private static final int SEED_BANK_CARD_WIDTH = 110;
    private static final int SEED_BANK_CARD_HEIGHT = 85;
    private static final int SEED_BANK_PADDING = 4;

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
    private Drawable fallbackEmptyCardBg;

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
            bg = new Image(createWhiteDrawable(Color.valueOf("0d1b3e")));
        }
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);

        String seedBankBgPath = "IMAGES/Menus/selection/seedbankBG.png";
        Drawable seedBankDrawable;
        if (Gdx.files.internal(seedBankBgPath).exists()) {
            seedBankDrawable = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(seedBankBgPath))));
        } else {
            seedBankDrawable = createWhiteDrawable(Color.valueOf("5a3214"));
        }

        Table seedBankContainer = new Table();
        seedBankContainer.setBackground(seedBankDrawable);

        sidebarSlotsTable = new Table();
        sidebarSlotsTable.defaults().pad(SEED_BANK_PADDING);

        ScrollPane slotScroll = new ScrollPane(sidebarSlotsTable, skin);
        slotScroll.setFadeScrollBars(false);
        slotScroll.setScrollingDisabled(true, false);
        slotScroll.setOverscroll(false, false);

        Stack seedBankStack = new Stack();
        seedBankStack.add(seedBankContainer);
        seedBankStack.add(slotScroll);

        ImageButton closeButton = new ImageButton(skin, "generic_close_circle");
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        root.pad(10);
        stage.addActor(root);

        Table bodyTable = new Table();
        bodyTable.defaults().pad(6);

        Table leftSidebar = new Table();
        leftSidebar.add(closeButton).size(45, 45).padTop(4).padBottom(6).center().row();
        leftSidebar.add(seedBankStack).width(130).growY().top();

        Table mainArea = new Table();
        mainArea.defaults().pad(4);

        Table topBar = new Table();
        currencyHeader = new CurrencyHeader();
        topBar.add(currencyHeader).right().padRight(5);
        mainArea.add(topBar).fillX().height(45).pad(0, 0, 6, 0).row();

        detailPanel = new Table();
        detailPanel.setBackground(getFallbackCardBackground());
        mainArea.add(detailPanel).growX().height(260).padBottom(6).row();

        availableGrid = new Table();
        availableGrid.defaults().pad(6);
        ScrollPane scrollPane = new ScrollPane(availableGrid, skin);
        scrollPane.setFadeScrollBars(false);
        mainArea.add(scrollPane).grow().row();

        bodyTable.add(leftSidebar).width(140).growY().padRight(10).top();
        bodyTable.add(mainArea).grow();
        root.add(bodyTable).grow();

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

        Table startBar = new Table();
        startBar.setFillParent(true);
        startBar.bottom().right();
        startBar.padRight(20).padBottom(16);
        startBar.add(letsRockBtn).width(190).height(48);
        startBar.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.childrenOnly);
        stage.addActor(startBar);
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

    private void renderPlantWithClipFallback(Batch batch, String plantName, float time, float x, float y, boolean grayedOut) {
        String[] clips = {"idle","idle_stage1_", "idle_stage1", "intro", "animation", "anim"};
        for (String clip : clips) {
            try {
                ScreenResourceManager.drawPlantAnimation(
                    batch, pamPlayer, plantName, clip, time, x, y, grayedOut
                );
                return;
            } catch (Exception ignored) {
            }
        }
        try {
            TextureRegion reg = Textures.regionOrNull("PLANT_" + plantName.toUpperCase().replace(" ", "_"));
            if (reg != null) {
                if (grayedOut) batch.setColor(0.3f, 0.3f, 0.3f, 1f);
                batch.draw(reg, x - 35, y, 70, 70);
                if (grayedOut) batch.setColor(Color.WHITE);
            }
        } catch (Exception ignored) {
        }
    }

    private void rebuildDetailPanel() {
        detailPanel.clearChildren();
        if (focusedPlant == null) return;

        Table animTable = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + 15f;
                renderPlantWithClipFallback(
                    batch, focusedPlant.getName(), globalAnimTestTime(), drawX, drawY, false
                );
            }
            private float globalAnimTestTime() {
                return globalAnimTime;
            }
        };
        detailPanel.add(animTable).size(130, 100).padLeft(25).padRight(20);

        Table infoColumn = new Table();
        infoColumn.top().left();

        Label nameLbl = createSafeLabel(focusedPlant.getName() + " (LVL " + focusedPlant.getLevel() + ")", "big");
        nameLbl.setFontScale(0.85f);
        infoColumn.add(nameLbl).left().padBottom(6).row();

        Table statsGrid = new Table();
        statsGrid.defaults().left().padRight(24).padBottom(6);
        statsGrid.add(createSafeLabel("SUN COST: " + focusedPlant.getSunCost(), "big"));
        statsGrid.add(createSafeLabel("RECHARGE: " + focusedPlant.getRechargeTime() + "s", "big")).row();
        statsGrid.add(createSafeLabel("HP: " + focusedPlant.getBaseHp(), "big"));
        statsGrid.add(createSafeLabel("DAMAGE: " + focusedPlant.getDamage(), "big")).row();

        for (Cell<?> cell : statsGrid.getCells()) {
            if (cell.getActor() instanceof Label) {
                ((Label) cell.getActor()).setFontScale(0.65f);
            }
        }
        infoColumn.add(statsGrid).left().padBottom(10).row();

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
        int upgradeCost = isMaxLevel ? 0 : (focusedPlant.getLevel() + 1) * 5;
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

        btnRow.add(upgradeBtn).width(210).height(42).padRight(14);
        btnRow.add(boostBtn).width(190).height(42);

        infoColumn.add(btnRow).left();
        detailPanel.add(infoColumn).expandX().fillX().left().pad(12, 0, 12, 20);
    }

    private void refreshAvailableGrid(List<Plant> allUnlocked) {
        availableGrid.clear();
        availableCards.clear();
        int maxCols = 6;
        int col = 0;

        for (Plant plant : allUnlocked) {
            PlantCardActor card = new PlantCardActor(plant, pamPlayer, textureBank, skin, PlantCardActor.Mode.SLOT);

            boolean selected = isPlantSelected(plant);
            card.setSelected(selected);
            card.setBoosted(plant.isPlantFoodActive());

            card.setOnClick(c -> {
                focusedPlant = plant;
                if (isSunflowerBanned(plant)) {
                    Toast.showError(stage, skin, "You cannot select Sun Producer in this level!");
                    refreshAll();
                    return;
                }
                if (isPlantSelected(plant)) {
                    menuLogic.removePlant(plant.getName());
                } else if (ctx.getActivePlants().size() < MAX_SELECTED_PLANTS) {
                    String result = menuLogic.addPlant(plant.getName());
                    if (result != null && result.startsWith("You cannot select")) {
                        Toast.showError(stage, skin, result);
                    } else if (plant.isPlantFoodActive()) {
                        Plant newlyAdded = getActivePlantInstance(plant);
                        if (newlyAdded != null) newlyAdded.setPlantFoodActive(true);
                    }
                } else {
                    Toast.showError(stage, skin, "You can only select up to " + MAX_SELECTED_PLANTS + " plants!");
                }
                refreshAll();
            });

            availableCards.put(plant.getName(), card);
            availableGrid.add(card).size(SEED_BANK_CARD_WIDTH, SEED_BANK_CARD_HEIGHT).pad(4);
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
            slotContainer.size(SEED_BANK_CARD_WIDTH, SEED_BANK_CARD_HEIGHT);
            sidebarSlotsTable.add(slotContainer).top().row();
        }

        for (int i = selected.size(); i < MAX_SELECTED_PLANTS; i++) {
            sidebarSlotsTable.add(buildEmptySeedBankSlot()).size(SEED_BANK_CARD_WIDTH, SEED_BANK_CARD_HEIGHT).top().row();
        }
    }

    private Table buildEmptySeedBankSlot() {
        Table empty = new Table();
        empty.setBackground(getFallbackEmptyCardBackground());
        return empty;
    }

    private Drawable getFallbackEmptyCardBackground() {
        if (fallbackEmptyCardBg == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.valueOf("381d0b"));
            pixmap.fill();
            fallbackEmptyCardBg = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
            pixmap.dispose();
        }
        return fallbackEmptyCardBg;
    }

    private boolean isPlantSelected(Plant plant) {
        return ctx.getActivePlants().stream().anyMatch(p -> p.getName().equalsIgnoreCase(plant.getName()));
    }

    private boolean isSunflowerBanned(Plant plant) {
        return ctx.getLevel() != null
            && ctx.getLevel().getLevelType() == LevelType.PLANT_WHAT_YOU_GET
            && PlantWhatYouGetManager.isSunflower(plant.getName());
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
            pixmap.setColor(new Color(0.08f, 0.08f, 0.08f, 0.75f));
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
