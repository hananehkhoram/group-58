package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.CollectionMenu;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.PlantFamily;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;

import pvz.libpvz.pam.PamPlayer;
import pvz.skin.PvzSkin;

public class PlantDetailsScreen implements Screen {

    public interface BackListener {
        void onBack();
    }

    private final Stage stage;
    private final Skin skin;
    private final Plant plant;
    private final BackListener backListener;
    private final CollectionMenu menuLogic;
    private final User currentUser;
    private final PamPlayer pamPlayer;

    private Texture menuBgTexture;
    private Image bg;
    private float stateTime = 0f;

    private static final float BASE_WIDTH = 1280f;
    private static final float BASE_HEIGHT = 720f;
    private static final Color BG_COLOR = Color.valueOf("0d1b3e");

    public PlantDetailsScreen(GameContext ctx, Plant plant, PamPlayer pamPlayer, BackListener backListener) {
        this.plant = plant;
        this.pamPlayer = pamPlayer;
        this.backListener = backListener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new FitViewport(BASE_WIDTH, BASE_HEIGHT));
        this.menuLogic = new CollectionMenu(ctx);
        this.currentUser = UserManager.getInstance().getCurrentUser();

        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);

        if (menuBgTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(BG_COLOR);
            pixmap.fill();
            menuBgTexture = new Texture(pixmap);
            pixmap.dispose();
        }

        bg = new Image(new TextureRegionDrawable(new TextureRegion(menuBgTexture)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        stage.addActor(root);

        float scale = getScaleFactor();

        Table topBar = new Table();
        TextButton backBtn = new TextButton("Back", skin, "brown");
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (backListener != null) backListener.onBack();
            }
        });
        topBar.add(backBtn).width(110 * scale).height(45 * scale).left().pad(15);

        Label nameTitle = createSafeLabel(plant.getName(), "big");
        nameTitle.setFontScale(1.3f * scale);
        topBar.add(nameTitle).expandX().center();
        root.add(topBar).fillX().row();

        Table contentArea = new Table();

        Table leftCol = new Table();

        Actor plantPamActor = createPlantPamActor(plant.getName(), scale);
        leftCol.add(plantPamActor).size(200 * scale, 200 * scale).padBottom(15 * scale).row();

        Label lvlLabel = createSafeLabel("Level " + plant.getLevel(), "big");
        lvlLabel.setFontScale(0.9f * scale);
        leftCol.add(lvlLabel).padBottom(5 * scale).row();

        int currentSeeds = currentUser != null ? currentUser.getSeedCount(plant.getName()) : 0;
        int seedsNeeded = 5 * (plant.getLevel() + 1);
        ProgressBar bar = createAlmanacProgressBar(seedsNeeded, currentSeeds);
        leftCol.add(bar).width(200 * scale).height(16 * scale).padBottom(5 * scale).row();

        Label seedsLbl = createSafeLabel(currentSeeds + " / " + seedsNeeded, "big");
        seedsLbl.setFontScale(0.75f * scale);
        leftCol.add(seedsLbl).row();

        contentArea.add(leftCol).padRight(40 * scale).top();

        Table rightCol = new Table();
        rightCol.defaults().left().pad(4 * scale);

        Table statsGrid = new Table();
        statsGrid.defaults().left().pad(6 * scale);

        statsGrid.add(createSafeLabel("SUN COST:", "big")).right();
        statsGrid.add(createSafeLabel(String.valueOf(plant.getSunCost()), "big")).padRight(20 * scale);

        statsGrid.add(createSafeLabel("RECHARGE:", "big")).right();
        statsGrid.add(createSafeLabel(plant.getRechargeTime() + "s", "big")).row();

        statsGrid.add(createSafeLabel("TOUGHNESS:", "big")).right();
        statsGrid.add(createSafeLabel(String.valueOf(plant.getBaseHp()), "big")).padRight(20 * scale);

        statsGrid.add(createSafeLabel("DAMAGE:", "big")).right();

        Table damageCell = new Table();

        Actor damagePamActor = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (pamPlayer != null) {
                    float drawX = getX() + getWidth() / 2f;
                    float drawY = getY() + getHeight() / 2f;
                    boolean drawn = false;

                    String[] paths = {
                        "EFFECTS/EMPEACH_DAMAGE/EMPEACH_DAMAGE.PAM",
                        "EFFECTS/EMPEACH_DAMAGE/EMPEACH_DAMAGE",
                        "IMAGES/768/FULL/EFFECTS/EMPEACH_DAMAGE/EMPEACH_DAMAGE.PAM"
                    };

                    String[] anims = {"idle", "anim_idle", "animation", "", null};

                    for (String path : paths) {
                        for (String anim : anims) {
                            try {
                                pamPlayer.draw(batch, path, anim, stateTime, drawX, drawY, true);
                                drawn = true;
                                break;
                            } catch (Exception ignored) {}
                        }
                        if (drawn) break;
                    }
                }
            }
        };

        damageCell.add(damagePamActor).size(28 * scale, 28 * scale).padRight(5 * scale);
        damageCell.add(createSafeLabel(String.valueOf(plant.getDamage()), "big"));
        statsGrid.add(damageCell).row();

        statsGrid.add(createSafeLabel("RANGE:", "big")).right();
        statsGrid.add(createSafeLabel(plant.getFamily() != null && plant.getFamily().equals(PlantFamily.LOBBER) ? "Lobbed" : "Straight", "big")).padRight(20 * scale);

        rightCol.add(statsGrid).padBottom(10 * scale).row();

        Table familyTable = new Table();
        familyTable.add(createSafeLabel("FAMILY: ", "big")).left();
        familyTable.add(createSafeLabel(plant.getFamily() != null ? plant.getFamily().name() : "None", "big")).left();
        rightCol.add(familyTable).padBottom(10 * scale).row();

        Table pfTable = new Table();
        Label pfTitle = createSafeLabel("Plant Food: ", "big");
        pfTitle.setColor(Color.YELLOW);
        pfTable.add(pfTitle).top().left();

        Label pfDesc = createSafeLabel(plant.getPlantFoodMode() != null ? String.valueOf(plant.getPlantFoodMode()) : "No Plant Food Effect", "big");
        pfDesc.setWrap(true);
        pfTable.add(pfDesc).width(320 * scale).left();
        rightCol.add(pfTable).padBottom(10 * scale).row();

        if (plant.getAbilityParams() != null && !plant.getAbilityParams().isEmpty()) {
            Label descLbl = createSafeLabel(String.valueOf(plant.getAbilityParams()), "big");
            descLbl.setWrap(true);
            rightCol.add(descLbl).width(420 * scale).padTop(5 * scale).row();
        }

        contentArea.add(rightCol).top().row();

        root.add(contentArea).expand().center().row();

        boolean isUnlocked = currentUser != null && currentUser.getUnlockedPlantTypes().stream()
            .anyMatch(p -> p.getName().equalsIgnoreCase(plant.getName()));

        if (isUnlocked) {
            TextButton upgradeBtn = new TextButton("Upgrade Plant", skin, "green_small");
            upgradeBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String msg = menuLogic.upgradePlant(plant.getName());
                    Toast.showError(stage, skin, msg);
                    buildUI();
                }
            });
            root.add(upgradeBtn).width(220 * scale).height(50 * scale).padBottom(30 * scale);
        } else {
            TextButton buyBtn = new TextButton("Buy Plant (2000 Coins)", skin, "purple");
            buyBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String msg = menuLogic.purchasePlant(plant.getName());
                    Toast.showError(stage, skin, msg);
                    buildUI();
                }
            });
            root.add(buyBtn).width(260 * scale).height(50 * scale).padBottom(30 * scale);
        }
    }

    private Actor createPlantPamActor(String plantName, float scale) {
        String rawName = plantName.toUpperCase();
        String folderName = rawName.replace(" ", "").replace("-", "").replace("-", "");
        if (folderName.equalsIgnoreCase("PRIMALPOTATOMINE")) {
            folderName = "PRIMAL_POTATOMINE";
        }

        final String finalFolderName = folderName;

        return new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);

                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + 20f * scale;
                boolean drawn = false;

                if (pamPlayer != null) {
                    String[] possiblePaths = {
                        "PLANT/" + finalFolderName + "/" + finalFolderName + ".PAM"
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
                }

                if (!drawn) {
                    TextureRegion reg = Textures.regionOrNull("PLANT_" + rawName.replace(" ", "_"));
                    if (reg != null) {
                        batch.draw(reg, drawX - (40 * scale), drawY, 80 * scale, 80 * scale);
                    }
                }
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
        float scaleX = stage.getWidth() / BASE_WIDTH;
        float scaleY = stage.getHeight() / BASE_HEIGHT;
        return Math.max(0.8f, Math.min(scaleX, scaleY));
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
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (bg != null) {
            bg.setSize(stage.getWidth(), stage.getHeight());
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (menuBgTexture != null) menuBgTexture.dispose();
    }
}
