package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
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
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.CollectionMenu;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.PlantFamily;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;
import com.workshop.view.components.CurrencyHeader;

import pvz.libpvz.pam.PamPlayer;
import pvz.skin.PvzSkin;

public class PlantDetailsScreen extends BaseScreen {

    public interface Listener {
        void onBack();
        void onPlantUpdated();
    }

    private static final float BASE_WIDTH = 1280f;
    private static final float BASE_HEIGHT = 720f;

    private final Stage stage;
    private final Plant plant;
    private final Listener listener;
    private final CollectionMenu menuLogic;
    private final User currentUser;
    private final PamPlayer pamPlayer;
    private CurrencyHeader currencyHeader;

    private float stateTime = 0f;
    private Image bg;

    public PlantDetailsScreen(GameContext ctx, Plant plant, PamPlayer pamPlayer, Listener listener) {
        super(PvzSkin.get());

        this.plant = plant;
        this.pamPlayer = pamPlayer;
        this.listener = listener;

        this.stage = new Stage(new ScreenViewport());
        this.menuLogic = new CollectionMenu(ctx);
        this.currentUser = UserManager.getInstance().getCurrentUser();

        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);

        String bgTexturePath = "IMAGES/Menus/Collection/plantDetailBG.png";

        if (Gdx.files.internal(bgTexturePath).exists()) {
            Texture bgTexture = new Texture(Gdx.files.internal(bgTexturePath));
            bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        } else {
            Gdx.app.error("PlantDetailsScreen", "Background texture not found at: " + bgTexturePath);
            bg = new Image(createWhiteDrawable(Color.valueOf("0d1b3e")));
        }
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);

        stage.addActor(root);

        float scale = getScaleFactor();

        Table topBar = buildTopBar(scale);
        root.add(topBar).fillX().row();

        Table contentArea = new Table();
        Table leftCol = buildLeftColumn(scale);
        Table rightCol = buildRightColumn(scale);

        contentArea.add(leftCol).padRight(50 * scale).top();
        contentArea.add(rightCol).top().expandX().fillX().row();

        root.add(contentArea).expand().center().padTop(10 * scale).row();

        buildActionButton(root, scale);
    }

    private Table buildTopBar(float scale) {
        Table topBar = new Table();

        ImageButton closeButton = new ImageButton(skin, "generic_close_circle");
        closeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });
        topBar.add(closeButton).width(110 * scale).height(45 * scale).left().pad(15);

        Label nameTitle = createSafeLabel(plant.getName(), "big");
        topBar.add(nameTitle).expandX().center();

        currencyHeader = new CurrencyHeader();
        topBar.add(currencyHeader).right().padRight(15);

        return topBar;
    }

    private Table buildLeftColumn(float scale) {
        Table leftCol = new Table();

        Stack plantStack = new Stack();
        Drawable woodBg = getStatIconDrawable("wood_bg");
        if (woodBg != null) {
            plantStack.add(new Image(woodBg));
        } else {
            Table t = new Table();
            t.setBackground(createWhiteDrawable(Color.valueOf("4a3319")));
            plantStack.add(t);
        }

        Actor plantPamActor = createPlantPamActor(scale);
        Table animWrapper = new Table();
        animWrapper.add(plantPamActor).size(180 * scale, 180 * scale).center();
        plantStack.add(animWrapper);

        leftCol.add(plantStack).size(240 * scale, 240 * scale).padBottom(10 * scale).row();

        Label lvlLabel = createSafeLabel("Level " + plant.getLevel(), "big");
        leftCol.add(lvlLabel).padBottom(5 * scale).row();

        int nextLevel = plant.getLevel() + 1;
        int currentSeeds = currentUser != null ? currentUser.getSeedCount(plant.getName()) : 0;
        int seedsNeeded = 5 * nextLevel;

        ProgressBar bar = createAlmanacProgressBar(seedsNeeded, currentSeeds);
        leftCol.add(bar).width(220 * scale).height(16 * scale).padBottom(5 * scale).row();

        String seedText = (plant.getLevel() >= 4) ? "MAX LEVEL" : currentSeeds + " / " + seedsNeeded + " Seeds";
        Label seedsLbl = createSafeLabel(seedText, "big");
        leftCol.add(seedsLbl).row();

        return leftCol;
    }

    private Drawable getStatIconDrawable(String fileNameWithoutExt) {
        String fullPath = "IMAGES/Menus/Collection/" + fileNameWithoutExt + ".png";
        try {
            if (Gdx.files.internal(fullPath).exists()) {
                Texture tex = new Texture(Gdx.files.internal(fullPath));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return new TextureRegionDrawable(new TextureRegion(tex));
            }
        } catch (Exception ignored) {}
        return null;
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

    private Table createStatBox(Drawable icon, String labelText, String valueText, float scale) {
        Table box = new Table();
        box.setBackground(createWhiteDrawable(Color.valueOf("2f6c2f")));

        if (icon != null) {
            box.add(new Image(icon)).size(32 * scale, 32 * scale).pad(6 * scale);
        } else {
            box.add().size(32 * scale, 32 * scale).pad(6 * scale);
        }

        Table textTable = new Table();
        textTable.defaults().left();
        Label lbl = createSafeLabel(labelText, "default");
        lbl.setFontScale(0.75f * scale);
        lbl.setColor(Color.LIGHT_GRAY);
        textTable.add(lbl).row();

        Label val = createSafeLabel(valueText, "big");
        val.setFontScale(0.9f * scale);
        textTable.add(val);

        box.add(textTable).expandX().fillX().padRight(10 * scale);
        return box;
    }

    private Table buildRightColumn(float scale) {
        Table rightCol = new Table();
        rightCol.defaults().left().padBottom(10 * scale);

        Table statsGrid = new Table();
        statsGrid.defaults().space(10 * scale);

        Drawable costIcon = getStatIconDrawable("cost");
        Drawable damageIcon = getStatIconDrawable("damage");
        Drawable rangeIcon = getStatIconDrawable("range");
        Drawable rechargeIcon = getStatIconDrawable("recharge");
        Drawable toughnessIcon = getStatIconDrawable("toughness");
        Drawable familyIcon = getStatIconDrawable("family");

        Table col1 = new Table();
        col1.defaults().padBottom(8 * scale).fillX();
        col1.add(createStatBox(costIcon, "SUN COST", String.valueOf(plant.getSunCost()), scale)).width(220 * scale).row();
        col1.add(createStatBox(toughnessIcon, "TOUGHNESS", String.valueOf(plant.getBaseHp()), scale)).width(220 * scale).row();

        String rangeStr = !plant.getTags().isEmpty() ? plant.getTags().toString() : "None";
        col1.add(createStatBox(familyIcon, "TAGS", rangeStr, scale)).width(220 * scale).row();

        Table col2 = new Table();
        col2.defaults().padBottom(8 * scale).fillX();
        col2.add(createStatBox(rechargeIcon, "RECHARGE", plant.getRechargeTime() + "s", scale)).width(220 * scale).row();
        col2.add(createStatBox(damageIcon, "DAMAGE", String.valueOf(plant.getDamage()), scale)).width(220 * scale).row();

        String familyStr = plant.getFamily() != null ? plant.getFamily().name() : "None";
        col2.add(createStatBox(rangeIcon, "FAMILY", familyStr, scale)).width(220 * scale).row();

        statsGrid.add(col1).padRight(15 * scale);
        statsGrid.add(col2);
        rightCol.add(statsGrid).row();

        Table pfTable = new Table();
        pfTable.defaults().left();
        Label pfTitle = createSafeLabel("Plant Food Effect:", "big");
        pfTitle.setFontScale(0.9f * scale);
        pfTitle.setColor(Color.YELLOW);
        pfTable.add(pfTitle).padBottom(3 * scale).row();

        String pfDescText = plant.getPlantFoodMode() != null ? String.valueOf(plant.getPlantFoodMode()) : "No Special Effect";
        Label pfDesc = createSafeLabel(pfDescText, "default");
        pfDesc.setWrap(true);
        pfTable.add(pfDesc).width(460 * scale);
        rightCol.add(pfTable).padBottom(8 * scale).row();

        return rightCol;
    }

    private void buildActionButton(Table root, float scale) {
        boolean isUnlocked = currentUser != null && currentUser.getUnlockedPlantTypes().stream()
            .anyMatch(p -> p.getName().equalsIgnoreCase(plant.getName()));

        if (isUnlocked) {
            int nextLevel = plant.getLevel() + 1;
            int coinsNeeded = 500 * nextLevel;

            String btnText = (plant.getLevel() >= 4) ? "MAX LEVEL" : "Upgrade (" + coinsNeeded + " Coins)";
            TextButton upgradeBtn = new TextButton(btnText, skin, "default");

            if (plant.getLevel() >= 4) {
                upgradeBtn.setDisabled(true);
            }

            upgradeBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (plant.getLevel() >= 4) return;
                    submitUpgrade();
                }
            });
            root.add(upgradeBtn).width(280 * scale).height(50 * scale).padBottom(20 * scale);
        } else {
            TextButton buyBtn = new TextButton("Buy Plant (2000 Coins)", skin, "purple");
            buyBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    submitPurchase();
                }
            });
            root.add(buyBtn).width(260 * scale).height(50 * scale).padBottom(20 * scale);
        }
    }

    private void submitUpgrade() {
        String result = menuLogic.upgradePlant(plant.getName());
        boolean success = result != null && result.startsWith("Successfully");

        if (success) {
            Toast.showSuccess(stage, skin, result);
            if (listener != null) listener.onPlantUpdated();
            if (currencyHeader != null) currencyHeader.updateValues();
            buildUI();
        } else {
            Toast.showError(stage, skin, result);
        }
    }

    private void submitPurchase() {
        String result = menuLogic.purchasePlant(plant.getName());
        boolean success = result != null && result.startsWith("Successfully");

        if (success) {
            Toast.showSuccess(stage, skin, result);
            if (listener != null) listener.onPlantUpdated();
            if (currencyHeader != null) currencyHeader.updateValues();
            buildUI();
        } else {
            Toast.showError(stage, skin, result);
        }
    }

    private Actor createPlantPamActor(float scale) {
        return new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + 20f * scale;

                String rawName = plant.getName().toUpperCase();
                String folderName = rawName.replace(" ", "").replace("-", "");

                String clip = "idle";
                if (folderName.equalsIgnoreCase("SUNSHROOM") || folderName.equalsIgnoreCase("PUFFSHROOM")) {
                    clip = "idle_stage1";
                } else if (folderName.contains("MINT")) {
                    clip = "intro";
                } else if (folderName.contains("BUSTER")) {
                    clip = "attack";
                } else if (folderName.contains("ELECTRICBLUE") || folderName.equalsIgnoreCase("CAULIPOWER")) {
                    clip = "idle1_1";
                } else if (folderName.contains("KIWIBEAST")) {
                    clip = "idle_stage1_";
                } else if (folderName.contains("DOOMSHROOM")) {
                    clip = "stage1_spawn";
                }

                ScreenResourceManager.drawPlantAnimation(
                    batch, pamPlayer, plant.getName(), clip, stateTime, drawX, drawY, false
                );
            }
        };
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

    private float getScaleFactor() {
        float scaleX = (float) Gdx.graphics.getWidth() / BASE_WIDTH;
        float scaleY = (float) Gdx.graphics.getHeight() / BASE_HEIGHT;
        return Math.max(0.8f, Math.min(scaleX, scaleY));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (currencyHeader != null) currencyHeader.updateValues();
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
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
    public void dispose() {
        stage.dispose();
        if (bg != null && bg.getDrawable() instanceof TextureRegionDrawable) {
            ((TextureRegionDrawable) bg.getDrawable()).getRegion().getTexture().dispose();
        }
        super.dispose();
    }
}
