package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.workshop.controller.SpecialLevelManager.ConveyorBeltManager;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.MiniGame.Izambi.Izambi;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.season.DarkAgesSeason;
import com.workshop.model.season.Season;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;
import com.workshop.view.gameplay.*;
import pvz.libpvz.pam.PamPlayer;
import pvz.skin.PvzSkin;
import com.workshop.controller.repository.Audio;
import com.workshop.controller.repository.Textures;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.workshop.model.level.DialogueLine;

import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commands.Planting;
import com.workshop.controller.commands.Plucking;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.level.LevelType;
import com.workshop.view.gameplay.DroppedSeedLayer;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;

import java.util.ArrayList;

public class GamePlayScreen implements Screen {

    private final Stage stage;
    private final PauseOverlay pauseOverlay;
    private final WinLoseOverlay winLoseOverlay;

    private final GameEngine gameEngine;
    private final GameContext ctx;

    private static final float TICK_DURATION = 0.1f;
    private float timeAccumulator = 0f;

    private final Texture leftTexture;
    private final Texture centerTexture;
    private final Texture rightTexture;

    private Label sunAmountLabel;
    private Label plantFoodAmountLabel;
    private Label waveLabel;
    private ProgressBar zombieProgressBar;
    private TextButton startZombiesButton;

    private final Image leftBackground;
    private final Image centerBackground;
    private final Image rightBackground;

    private final ShapeRenderer shapeRenderer;
    private final Runnable exitAction;

    private final OrthographicCamera worldCamera;

    private final float fullWorldWidth;
    private final float gameplayWorldWidth;

    private final FitViewport worldViewport;
    private final float worldHeight;

    private final float introCameraX;
    private final float gameplayCameraX;
    private final float cameraY;
    private static final float INTRO_WAIT = 3f;
    private static final float INTRO_DURATION = 1.4f;

    private static final float POST_INTRO_WAIT = 3f;

    private float introTime = 0f;
    private boolean introFinished = false;

    private float postIntroTime = 0f;
    private boolean gameplayStarted = false;
    private float shakeTime = 0f;
    private float shakeDuration = 0f;
    private float shakeIntensity = 0f;

    private static final float MISSION_DISPLAY_TIME = 6f;
    private float screenElapsedTime = 0f;
    private final List<PlantCardActor> seedBankCards =
        new ArrayList<>();
    private final List<ZombieCardActor> zombieBankCards =
        new ArrayList<>();

    protected Plant selectedPlantForPlacement;
    private String selectedZombieTypeForPlacement;
    private boolean plantFoodFeedMode;
    private int hoveredPlantRow = -1;
    private int hoveredPlantColumn = -1;
    protected PlantActor mousePlantPreview;
    private ZombiePlacementPreviewActor mouseZombiePreview;

    protected final PlantAnimationResolver plantPreviewResolver =
        new PlantAnimationResolver();

    private final Vector2 mouseStagePosition =
        new Vector2();

    private final Planting plantingCommand;
    private final Plucking pluckingCommand;
    private ImageButton shovelButton;

    private boolean pluckingMode = false;
    private Image shovelMousePreview;
    private Texture shovelCursorTexture;
    private ImageButton plantFoodButton;
    private Image plantFoodMousePreview;
    private Texture plantFoodCursorTexture;

    private Table seedBankContainer;

    private Table seedBankTable;
    private Table zombieBankTable;

    private boolean dialogueBlocking = false;
    private boolean endDialogueShown = false;
    private String lastConveyorSignature = "";

    private ConveyorBeltLayer conveyorBeltLayer;
    private BrainLayer brainLayer;
    private Cursor hiddenCursor;

    private static final float SHOVEL_CURSOR_HEIGHT = 42f;



    public GamePlayScreen(
        GameContext gameContext,
        Runnable restartAction,
        Runnable exitAction
    ) {
        this.exitAction = exitAction;

        this.ctx = gameContext;
        this.gameEngine = gameContext.getGameEngine();

        MenuManager plantingMenuManager =
            new MenuManager(gameContext);

        plantingMenuManager.setGameEngine(gameEngine);

        this.plantingCommand =
            new Planting(plantingMenuManager);

        this.pluckingCommand =
            new Plucking(plantingMenuManager);

        Season season = gameContext.getSeason();
        Level level = gameContext.getLevel();

        Skin skin = PvzSkin.get();
        testPeashooterParts();

        String iceBlockPam =
            "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM";

        Gdx.app.log(
            "IceBlockTest",
            "clips: " + Textures.getPamPlayer().clips(iceBlockPam)
        );

        BackgroundPaths paths =
            fallbackIfMissing(getBackgroundPaths(season));

        leftTexture = new Texture(
            Gdx.files.internal(paths.left)
        );

        centerTexture = new Texture(
            Gdx.files.internal(paths.center)
        );

        rightTexture = new Texture(
            Gdx.files.internal(paths.right)
        );

        shovelCursorTexture = new Texture(
            Gdx.files.internal("IMAGES/Menus/game/shovelOnMouse.png")
        );

        plantFoodCursorTexture =
            new Texture(
                Gdx.files.internal("IMAGES/Menus/game/FoodPlantOnMouse.png")
            );

        fullWorldWidth =
            leftTexture.getWidth()
                + centerTexture.getWidth()
                + rightTexture.getWidth();

        worldHeight = centerTexture.getHeight();

        gameplayWorldWidth =
            leftTexture.getWidth()
                + centerTexture.getWidth();

        worldViewport = new FitViewport(
            fullWorldWidth,
            worldHeight
        );

        stage = new Stage(worldViewport);

        Pixmap emptyPixmap =
            new Pixmap(
                1,
                1,
                Pixmap.Format.RGBA8888
            );

        emptyPixmap.setColor(0f, 0f, 0f, 0f);
        emptyPixmap.fill();

        hiddenCursor =
            Gdx.graphics.newCursor(
                emptyPixmap,
                0,
                0
            );

        emptyPixmap.dispose();


        worldCamera =
            (OrthographicCamera) stage.getCamera();

        introCameraX =
            fullWorldWidth / 2f;

        gameplayCameraX =
            gameplayWorldWidth / 2f;

        cameraY =
            worldHeight / 2f;

        worldCamera.position.set(
            introCameraX,
            cameraY,
            0f
        );

        worldCamera.zoom = 1f;
        worldCamera.update();

        shapeRenderer = new ShapeRenderer();

        leftBackground = new Image(leftTexture);
        centerBackground = new Image(centerTexture);
        rightBackground = new Image(rightTexture);

        buildBackground();

        ZombieIntroLayer zombieIntroLayer =
            new ZombieIntroLayer();

        float rightX =
            leftTexture.getWidth()
                + centerTexture.getWidth();

        float rightWidth =
            rightTexture.getWidth();

        float height =
            rightTexture.getHeight();

        float[][] zombiePoints =
            getZombieIntroPoints(season);

        String[] introZombies =
            getIntroZombiePamNames(season);

        int count = Math.min(
            introZombies.length,
            zombiePoints.length
        );

        for (int i = 0; i < count; i++) {
            float[] point = zombiePoints[i];

            Gdx.app.log(
                "ZombieIntro",
                "Showing intro zombie: " + introZombies[i]
            );

            zombieIntroLayer.addZombie(
                introZombies[i],
                rightX + rightWidth * point[0],
                height * point[1]
            );
        }

        stage.addActor(zombieIntroLayer);

        GraveAnimationLayer graveAnimationLayer =
            new GraveAnimationLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(graveAnimationLayer);

        com.workshop.view.gameplay.SlipperyTileLayer slipperyTileLayer =
            new com.workshop.view.gameplay.SlipperyTileLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(slipperyTileLayer);

        com.workshop.view.gameplay.WaterLayer waterLayer =
            new com.workshop.view.gameplay.WaterLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(waterLayer);
        if (level.getLevelType() == LevelType.Wallnuts_MG) {
            BowlingRedLineLayer redLineLayer = new BowlingRedLineLayer(
                shapeRenderer,
                getGridX() + 3 * getCellWidth(),
                getGridY(),
                getGridHeight()
            );
            stage.addActor(redLineLayer);
            redLineLayer.toFront();
        }

        if (level.getLevelType() == LevelType.Izambie_MG) {
            BowlingRedLineLayer redLineLayer = new BowlingRedLineLayer(
                shapeRenderer,
                getGridX() + IZombieManager.RED_LINE_COLUMN * getCellWidth(),
                getGridY(),
                getGridHeight()
            );
            stage.addActor(redLineLayer);
            redLineLayer.toFront();
        }

        if (level.getLevelType() == LevelType.DEADLINE) {
            int deadlineColumn = 2;
            float lineX = getGridX() + (deadlineColumn * getCellWidth());

            BowlingRedLineLayer redLineLayer = new BowlingRedLineLayer(
                shapeRenderer,
                lineX,
                getGridY(),
                getGridHeight()
            );
            stage.addActor(redLineLayer);
            redLineLayer.toFront();
        }

        if (level.getLevelType() == LevelType.Vase_MG) {

            VaseAnimationLayer vaseAnimationLayer =
                new VaseAnimationLayer(
                    gameContext,
                    gameEngine,
                    getGridX(),
                    getGridY(),
                    getGridWidth(),
                    getGridHeight()
                );

            stage.addActor(vaseAnimationLayer);
        }

        DroppedSeedLayer droppedSeedLayer =
            new DroppedSeedLayer(
                gameContext,
                gameEngine,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight(),
                this::selectDroppedSeed
            );

        stage.addActor(
            droppedSeedLayer
        );

        PlantAnimationLayer plantAnimationLayer =
            new PlantAnimationLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(plantAnimationLayer);

        ProjectileAnimationLayer projectileAnimationLayer =
            new ProjectileAnimationLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(projectileAnimationLayer);

        if (level.getLevelType() == LevelType.Izambie_MG) {

            brainLayer = new BrainLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

            stage.addActor(brainLayer);

        } else {

            stage.addActor(new LawnMowerLayer(
                gameContext,
                gameEngine,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            ));
        }

        ZombieAnimationLayer zombieAnimationLayer =
            new ZombieAnimationLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(zombieAnimationLayer);

        stage.addActor(new ProjectileHitFxLayer(
            gameContext,
            getGridX(),
            getGridY(),
            getGridWidth(),
            getGridHeight()
        ));

        stage.addActor(new ExplosionFxLayer(
            gameContext,
            getGridX(),
            getGridY(),
            getGridWidth(),
            getGridHeight()
        ));

        stage.addActor(new ZombieGibLayer(
            gameContext,
            getGridX(),
            getGridY(),
            getGridWidth(),
            getGridHeight()
        ));

        com.workshop.view.gameplay.ChillWindLayer chillWindLayer =
            new com.workshop.view.gameplay.ChillWindLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(chillWindLayer);

        pauseOverlay = new PauseOverlay(
            stage,
            skin,
            gameContext,
            () -> {
                if (restartAction != null) {
                    restartAction.run();
                }
            },
            () -> {
                gameContext.setPaused(false);

                if (exitAction != null) {
                    exitAction.run();
                }
            }
        );

        winLoseOverlay = new WinLoseOverlay(
            stage,
            skin,
            () -> {
                gameContext.setPaused(false);

                if (restartAction != null) {
                    restartAction.run();
                }
            },
            () -> {
                gameContext.setPaused(false);

                if (exitAction != null) {
                    exitAction.run();
                }
            }
        );

        ImageButton pauseTestButton =
            createPauseButton();

        pauseTestButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                pauseOverlay.show();
            }
        });


        Image sunIcon = new Image(skin, "image_ui_hud_ingame_sun");

        sunAmountLabel = new Label(
            String.valueOf(gameContext.getSunAmount()),
            skin
        );

        Table sunCounter = new Table();
        sunCounter.add(sunIcon).size(56, 57).padRight(8);
        sunCounter.add(sunAmountLabel).minWidth(48).left();

        createPlantFoodButton();

        plantFoodAmountLabel = new Label(
            String.valueOf(currentPlantFoodCount()),
            skin
        );

        Table plantFoodCounter = new Table();

        plantFoodCounter.add(plantFoodButton)
            .size(48, 48)
            .padRight(8);

        plantFoodCounter.add(plantFoodAmountLabel)
            .minWidth(48)
            .left();

        plantFoodButton.addListener(new ClickListener() {
            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y
            ) {
                event.stop();

                togglePlantFoodFeedMode();

                plantFoodButton.setChecked(
                    plantFoodFeedMode
                );
            }
        });

        Table sunRow = new Table();
        sunRow.add(sunCounter).left();

        Table plantFoodRow = new Table();
        plantFoodRow.add(plantFoodCounter).left();

        boolean debugMode = UserManager.getInstance().getCurrentUser() != null
            && UserManager.getInstance().getCurrentUser().isDebugMode();
        if (debugMode) {
            sunRow.add(createDebugAddButton(skin, () ->
                gameContext.addSun(50)
            )).size(40, 40).padLeft(12);
            plantFoodRow.add(createDebugAddButton(skin, () -> {
                com.workshop.model.user.User user =
                    UserManager.getInstance().getCurrentUser();
                if (user != null) {
                    user.setPlantFoodCount(user.getPlantFoodCount() + 1);
                }
            })).size(40, 40).padLeft(12);
        }

        zombieProgressBar = new ProgressBar(
            0f,
            1f,
            0.001f,
            false,
            skin,
            "ingame_progress"
        );

        zombieProgressBar.setAnimateDuration(0.3f);

        float progressBarWidth = 273f;
        float progressBarHeight = 33f;

        com.badlogic.gdx.scenes.scene2d.ui.Stack progressStack =
            new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        progressStack.add(zombieProgressBar);
        progressStack.add(
            buildWaveFlagsOverlay(skin, level, progressBarWidth, progressBarHeight)
        );

        waveLabel = new Label("", skin);
        waveLabel.setFontScale(0.8f);

        Table progressColumn = new Table();
        progressColumn.add(progressStack)
            .size(progressBarWidth, progressBarHeight)
            .row();
        progressColumn.add(waveLabel).padTop(6);

        updateHud();
        createShovelButton();

        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top();
        hudTable.pad(20);
        Table leftCounters = new Table();

        leftCounters.add(sunCounter)
            .left()
            .row();

        leftCounters.add(plantFoodCounter)
            .left()
            .padTop(6)
            .row();

        hudTable.add(leftCounters)
            .left()
            .top();

        hudTable.add(progressColumn)
            .expandX()
            .padLeft(20)
            .padRight(20);

        Table rightControls = new Table();

        rightControls.add(shovelButton)
            .size(50, 50)
            .padRight(4);

        rightControls.add(pauseTestButton)
            .size(50, 50);

        hudTable.add(rightControls)
            .right()
            .top();

        stage.addActor(hudTable);

        if (level.getLevelType() == LevelType.PLANT_WHAT_YOU_GET) {
            String buttonStyle = skin.has("green", TextButton.TextButtonStyle.class)
                ? "green"
                : "default";
            startZombiesButton = new TextButton("Let's Rock!", skin, buttonStyle);
            startZombiesButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    event.stop();
                    gameContext.triggerManualWaveStart();
                    startZombiesButton.setVisible(false);
                }
            });
            Table startTable = new Table();
            startTable.setFillParent(true);
            startTable.bottom();
            startTable.add(startZombiesButton)
                .padBottom(36)
                .width(280)
                .height(64);
            stage.addActor(startTable);
        }

        SunAnimationLayer sunAnimationLayer =
            new SunAnimationLayer(
                gameContext,
                gameEngine,
                sunCounter,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        List<DialogueLine> introDialogue = level.getIntroDialogue();

        if (!suppressDefaultUI()) {
            if (isIZombieLevel()) {
                buildZombieBank(skin);
            } else if (isConveyorLevel()) {
                buildConveyorBelt();
            } else {
                buildSeedBank(skin);
            }
            setupPlantingClick();
        }

        if (introDialogue != null && !introDialogue.isEmpty()) {
            dialogueBlocking = true;


            Toast.showMission(
                stage,
                skin,
                com.workshop.model.level.LevelObjectives.describe(level),
                INTRO_WAIT + INTRO_DURATION
            );
            new DialogueOverlay(
                stage,
                skin,
                introDialogue,
                () -> {
                    dialogueBlocking = false;

                    Toast.showMission(
                        stage,
                        skin,
                        com.workshop.model.level.LevelObjectives.describe(level),
                        INTRO_WAIT + INTRO_DURATION
                    );
                }
            ).show();
        } else {
            Toast.showMission(
                stage,
                skin,
                com.workshop.model.level.LevelObjectives.describe(level),
                INTRO_WAIT + INTRO_DURATION
            );
        }

        stage.addActor(sunAnimationLayer);

        com.workshop.view.gameplay.PlantFoodAnimationLayer plantFoodAnimationLayer =
            new com.workshop.view.gameplay.PlantFoodAnimationLayer(
                gameContext,
                plantFoodCounter,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(plantFoodAnimationLayer);

    }

    private void hideSystemCursor() {
        if (hiddenCursor != null) {
            Gdx.graphics.setCursor(hiddenCursor);
        }
    }

    private void restoreSystemCursor() {
        Gdx.graphics.setSystemCursor(
            Cursor.SystemCursor.Arrow
        );
    }

    private void buildConveyorBelt() {
        conveyorBeltLayer = new ConveyorBeltLayer(
            (ConveyorBeltManager) ctx.getLevelManager(),
            worldHeight,
            seedBankCards,
            this::selectPlant
        );
        stage.addActor(conveyorBeltLayer);
    }
    private int currentPlantFoodCount() {
        com.workshop.model.user.User user = UserManager.getInstance().getCurrentUser();
        return user != null ? user.getPlantFoodCount() : 0;
    }

    private ImageButton createDebugAddButton(Skin skin, Runnable onAdd) {
        ImageButton button = new ImageButton(skin, "generic_close_circle") {
            @Override
            protected void sizeChanged() {
                super.sizeChanged();
                setOrigin(getWidth() / 2f, getHeight() / 2f);
            }
        };
        button.setTransform(true);
        button.setRotation(45f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                event.stop();
                onAdd.run();
                updateHud();
            }
        });
        return button;
    }
    private void createShovelButton() {

        TextureRegion darkRegion =
            Textures.regionOrNull(
                "IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON_DOWN"
            );

        TextureRegion lightRegion =
            Textures.regionOrNull(
                "IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON"
            );

        if (darkRegion == null) {
            throw new IllegalStateException(
                "Dark shovel image was not found: "
                    + "IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON_DOWN"
            );
        }

        if (lightRegion == null) {
            throw new IllegalStateException(
                "Light shovel image was not found: "
                    + "IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON"
            );
        }

        TextureRegionDrawable darkDrawable =
            new TextureRegionDrawable(darkRegion);

        TextureRegionDrawable lightDrawable =
            new TextureRegionDrawable(lightRegion);

        ImageButton.ImageButtonStyle style =
            new ImageButton.ImageButtonStyle();

        // حالت عادی: تیره
        style.imageUp = darkDrawable;

        // موس روی دکمه: روشن
        style.imageOver = lightDrawable;

        // نگه داشتن کلیک: روشن
        style.imageDown = lightDrawable;

        // حالت برداشت فعال: روشن
        style.imageChecked = lightDrawable;

        // حالت برداشت فعال + hover: روشن
        style.imageCheckedOver = lightDrawable;

        shovelButton = new ImageButton(style);

        shovelButton.addListener(new ClickListener() {
            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y
            ) {
                event.stop();

                togglePluckingMode();

                shovelButton.setChecked(pluckingMode);
            }
        });
    }

    private ImageButton createPauseButton() {

        TextureRegion darkRegion =
            Textures.regionOrNull(
                "IMAGE_UI_HUD_INGAME_PAUSE_BUTTON_DOWN"
            );

        TextureRegion lightRegion =
            Textures.regionOrNull(
                "IMAGE_UI_HUD_INGAME_PAUSE_BUTTON"
            );

        if (darkRegion == null) {
            throw new IllegalStateException(
                "Dark pause image was not found: "
                    + "IMAGE_UI_HUD_INGAME_PAUSE_BUTTON_DOWN"
            );
        }

        if (lightRegion == null) {
            throw new IllegalStateException(
                "Light pause image was not found: "
                    + "IMAGE_UI_HUD_INGAME_PAUSE_BUTTON"
            );
        }

        TextureRegionDrawable darkDrawable =
            new TextureRegionDrawable(darkRegion);

        TextureRegionDrawable lightDrawable =
            new TextureRegionDrawable(lightRegion);

        ImageButton.ImageButtonStyle style =
            new ImageButton.ImageButtonStyle();

        // حالت عادی: تیره
        style.imageUp = darkDrawable;

        // موس روی دکمه: روشن
        style.imageOver = lightDrawable;

        // موقع نگه داشتن کلیک: روشن
        style.imageDown = lightDrawable;

        return new ImageButton(style);
    }

    private void createPlantFoodButton() {

        TextureRegion darkRegion =
            Textures.regionOrNull(
                "IMAGE_UI_HUD_EVENTBUTTON_EVENT_ICON_POTW_DOWN"
            );

        TextureRegion lightRegion =
            Textures.regionOrNull(
                "IMAGE_UI_HUD_EVENTBUTTON_EVENT_ICON_POTW_UP"
            );

        if (darkRegion == null) {
            throw new IllegalStateException(
                "Dark plant food image was not found: "
                    + "IMAGE_UI_HUD_EVENTBUTTON_EVENT_ICON_POTW_DOWN"
            );
        }

        if (lightRegion == null) {
            throw new IllegalStateException(
                "Light plant food image was not found: "
                    + "IMAGE_UI_HUD_EVENTBUTTON_EVENT_ICON_POTW_UP"
            );
        }

        TextureRegionDrawable darkDrawable =
            new TextureRegionDrawable(darkRegion);

        TextureRegionDrawable lightDrawable =
            new TextureRegionDrawable(lightRegion);

        ImageButton.ImageButtonStyle style =
            new ImageButton.ImageButtonStyle();

        // عادی: تیره
        style.imageUp = darkDrawable;

        // Hover: روشن
        style.imageOver = lightDrawable;

        // کلیک: روشن
        style.imageDown = lightDrawable;

        // Plant Food فعال: روشن می‌ماند
        style.imageChecked = lightDrawable;

        // فعال + Hover
        style.imageCheckedOver = lightDrawable;

        plantFoodButton =
            new ImageButton(style);
    }

    private void clearPlantFoodFeedMode() {

        plantFoodFeedMode = false;

        if (plantFoodButton != null) {
            plantFoodButton.setChecked(false);
        }

        if (plantFoodMousePreview != null) {
            plantFoodMousePreview.remove();
            plantFoodMousePreview = null;
        }

        restoreSystemCursor();
    }


    private Group buildWaveFlagsOverlay(
        Skin skin,
        Level level,
        float barWidth,
        float barHeight
    ) {
        Group overlay = new Group();
        overlay.setSize(barWidth, barHeight);
        overlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

        com.workshop.model.mechanisms.Wave[] waves = level.getWaves();
        int totalWaves = waves != null ? waves.length : 0;

        if (totalWaves == 0) {
            return overlay;
        }

        for (int i = 1; i <= totalWaves; i++) {
            boolean isFinalWave = (i == totalWaves);

            TextureRegion flagRegion = skin.getRegion(
                isFinalWave
                    ? "image_ui_hud_ingame_progress_meter_flag_pole"
                    : "image_ui_hud_ingame_progress_meter_flag_default"
            );

            if (flagRegion == null) {
                continue;
            }

            Image flag = new Image(flagRegion);

            float flagWidth = flagRegion.getRegionWidth();
            float flagHeight = flagRegion.getRegionHeight();

            // موج ۱ نزدیک سمت چپ نوار، آخرین موج (پرچم بزرگ‌تر) سمت راست —
            // مستقل از جهت پر شدن نوار، فقط ترتیب موج‌ها را نشان می‌دهد.
            float xFraction = (float) i / totalWaves;
            float x = xFraction * barWidth - flagWidth / 2f;
            x = MathUtils.clamp(x, 0f, barWidth - flagWidth);

            float y = barHeight - flagHeight / 2f;

            flag.setPosition(x, y);
            overlay.addActor(flag);
        }

        return overlay;
    }

    private void updateHud() {
        if (startZombiesButton != null) {
            startZombiesButton.setVisible(
                gameplayStarted && !ctx.isManualStartCommandReceived()
            );
        }
        sunAmountLabel.setText(
            String.valueOf(ctx.getSunAmount())
        );
        plantFoodAmountLabel.setText(
            String.valueOf(currentPlantFoodCount())
        );

        com.workshop.model.mechanisms.Wave[] waves =
            ctx.getLevel().getWaves();

        int totalWaves = waves != null ? waves.length : 0;


        int currentWaveIndex = ctx.getCurrentWaveIndex();

        float overallProgress = 0f;

        if (totalWaves > 0 && currentWaveIndex > 0) {
            int activeWaveArrayIndex =
                Math.min(currentWaveIndex - 1, totalWaves - 1);

            com.workshop.model.mechanisms.Wave activeWave =
                waves[activeWaveArrayIndex];


            float waveInternalProgress = activeWave.getProgress();

            overallProgress =
                (activeWaveArrayIndex + waveInternalProgress) / totalWaves;
        }


        zombieProgressBar.setValue(
            MathUtils.clamp(1f - overallProgress, 0f, 1f)
        );

        int currentWaveDisplay = totalWaves > 0
            ? MathUtils.clamp(currentWaveIndex, 1, totalWaves)
            : 0;

        waveLabel.setText(
            totalWaves > 0
                ? "wave " + currentWaveDisplay + " from " + totalWaves
                : ""
        );

    }

    private void showEndOfLevelDialogue() {
        boolean won = ctx.isPlayerWon();

        List<DialogueLine> lines = won
            ? ctx.getLevel().getWinDialogue()
            : ctx.getLevel().getLoseDialogue();

        Runnable showFinalOverlay = () -> {
            if (won) {
                Audio.playMusic("music/winmusic", false);
                winLoseOverlay.showWin();
            } else {
                Audio.playMusic("music/losemusic", false);
                winLoseOverlay.showLose();
            }
        };


        if (lines != null && !lines.isEmpty()) {
            dialogueBlocking = true;

            new DialogueOverlay(
                stage,
                PvzSkin.get(),
                lines,
                () -> {
                    dialogueBlocking = false;
                    showFinalOverlay.run();
                }
            ).show();
        } else {
            showFinalOverlay.run();
        }
    }

    protected boolean handlesOwnEndOfGame() {
        return false;
    }

    protected WinLoseOverlay getWinLoseOverlay() {
        return winLoseOverlay;
    }

    private void buildBackground() {

        float leftWidth = leftTexture.getWidth();
        float centerWidth = centerTexture.getWidth();
        float rightWidth = rightTexture.getWidth();

        float height = centerTexture.getHeight();

        leftBackground.setBounds(
            0,
            0,
            leftWidth,
            height
        );

        centerBackground.setBounds(
            leftWidth,
            0,
            centerWidth,
            height
        );

        rightBackground.setBounds(
            leftWidth + centerWidth,
            0,
            rightWidth,
            height
        );

        stage.addActor(leftBackground);
        stage.addActor(centerBackground);
        stage.addActor(rightBackground);
    }

    private void buildSeedBank(Skin skin) {
        if (isConveyorLevel() || isIZombieLevel()) {
            return;
        }

        seedBankTable = new Table();
        seedBankTable.setFillParent(true);
        seedBankTable.left().top();

        seedBankTable.padLeft(110f);
        seedBankTable.padTop(20f);

        Table seedBankPanel = new Table();
        seedBankPanel.top();

        Table cardsTable = new Table();
        cardsTable.top();

        seedBankCards.clear();

        for (Plant plant : getAvailableSeedBankPlants()) {

            PlantCardActor card = new PlantCardActor(
                plant,
                Textures.getPamPlayer(),
                Textures.getInstance(),
                skin,
                PlantCardActor.Mode.SLOT
            );

            // --- خط اضافه شده: تنظیم حالت بوست روی کارت در گیم‌پلی ---
            card.setBoosted(plant.isPlantFoodActive());

            seedBankCards.add(card);

            card.setOnClick(clickedCard -> {
                if (!isConveyorLevel()
                    && ctx.isOnCooldown(clickedCard.getPlant().getName())) {
                    showPlantCooldownError(clickedCard.getPlant().getName());
                    return;
                }
                selectPlant(clickedCard);
            });

            cardsTable.add(card)
                .size(100f, 58f)
                .padBottom(5f)
                .row();
        }

        seedBankPanel.add(cardsTable)
            .top()
            .padTop(15f);

        seedBankTable.add(seedBankPanel)
            .width(125f)
            .height(worldHeight - 40f)
            .top();

        stage.addActor(seedBankTable);

        if (ctx.getLevelManager()
            instanceof ConveyorBeltManager conveyorManager) {

            lastConveyorSignature =
                getConveyorSignature(conveyorManager);
        }
    }
    private void buildZombieBank(Skin skin) {
        if (!isIZombieLevel()) {
            return;
        }

        if (!(ctx.getLevelManager()
            instanceof IZombieManager manager)) {
            return;
        }

        zombieBankTable = new Table();
        zombieBankTable.setFillParent(true);
        zombieBankTable.left().top();
        zombieBankTable.padLeft(110f);
        zombieBankTable.padTop(20f);

        Table panel = new Table();
        panel.top();

        Table cardsTable = new Table();
        cardsTable.top();

        zombieBankCards.clear();

        for (var entry : manager.getAvailableZombieCosts().entrySet()) {
            Zombie zombie;

            try {
                zombie = ctx
                    .getZombieFactory()
                    .create(entry.getKey());
            } catch (IllegalArgumentException exception) {
                Gdx.app.error(
                    "IZombieUI",
                    "Could not create zombie card for "
                        + entry.getKey(),
                    exception
                );
                continue;
            }

            ZombieCardActor card = new ZombieCardActor(
                zombie,
                entry.getKey(),
                entry.getValue(),
                ctx.getSeason().getName(),
                Textures.getPamPlayer(),
                skin
            );

            zombieBankCards.add(card);

            card.setOnClick(this::selectZombie);

            cardsTable.add(card)
                .size(100f, 58f)
                .padBottom(60f)
                .row();
        }

        panel.add(cardsTable)
            .top()
            .padTop(15f);

        zombieBankTable.add(panel)
            .width(125f)
            .height(worldHeight - 40f)
            .top();

        stage.addActor(zombieBankTable);
    }

    private void selectZombie(
        ZombieCardActor clickedCard
    ) {
        clearPluckingMode();
        if (ctx.getLevelManager()
            instanceof IZombieManager manager) {

            double cooldown =
                manager
                    .getRemainingZombieCooldownSeconds(
                        clickedCard.getZombieType(),
                        ctx
                    );

            if (cooldown > 0) {
                int seconds =
                    (int) Math.ceil(cooldown);

                Toast.showError(
                    stage,
                    PvzSkin.get(),
                    clickedCard.getZombieType()
                        + " is recharging ("
                        + seconds
                        + "s)"
                );

                return;
            }
        }

        clearPlantFoodFeedMode();
        clearPlantSelection();

        selectedZombieTypeForPlacement =
            clickedCard.getZombieType();

        for (ZombieCardActor card : zombieBankCards) {
            card.setFocused(
                card == clickedCard
            );
        }

        showZombieOnMouse(
            clickedCard.getZombie()
        );
    }

    private void showZombieOnMouse(Zombie zombie) {
        if (mouseZombiePreview != null) {
            mouseZombiePreview.remove();
            mouseZombiePreview = null;
        }

        ZombieAnimationSpec spec =
            ZombieAnimationResolver.shared().resolve(
                zombie,
                ctx.getSeason().getName()
            );

        if (spec == null) {
            return;
        }

        mouseZombiePreview =
            new ZombiePlacementPreviewActor(
                spec,
                Textures.getPamPlayer(),
                getCellHeight()
            );

        mouseZombiePreview.setTouchable(Touchable.disabled);
        stage.addActor(mouseZombiePreview);
    }

    private void selectPlant(PlantCardActor clickedCard) {
        clearPluckingMode();
        clearPlantFoodFeedMode();
        selectedPlantForPlacement =
            clickedCard.getPlant();

        for (PlantCardActor card : seedBankCards) {
            card.setFocused(card == clickedCard);
        }

        showPlantOnMouse(selectedPlantForPlacement);
    }

    private BackgroundPaths getBackgroundPaths(Season season) {

        switch (season.getName()) {

            case "Ancient Egypt":
                return new BackgroundPaths(
                    "IMAGES/Menus/GamePlay/AncientEgypt/AncientEgyptBackgroundLeft.png",
                    "IMAGES/Menus/GamePlay/AncientEgypt/AncientEgyptBackground.png",
                    "IMAGES/Menus/GamePlay/AncientEgypt/AncientEgyptBackgroundRight.png"
                );

            case "FrozenCave":
                return new BackgroundPaths(
                    "IMAGES/Menus/GamePlay/FrozenCave/FrozenCaveLeft.png",
                    "IMAGES/Menus/GamePlay/FrozenCave/FrozenCave.png",
                    "IMAGES/Menus/GamePlay/FrozenCave/FrozenCaveRight.png"
                );

            case "Big Wave Beach":
                return new BackgroundPaths(
                    "IMAGES/Menus/GamePlay/BigWaveBeach/BigWaveBeachLeft.png",
                    "IMAGES/Menus/GamePlay/BigWaveBeach/BigWaveBeach.png",
                    "IMAGES/Menus/GamePlay/BigWaveBeach/BigWaveBeachRight.png"
                );

            case "Dark Ages":
                return new BackgroundPaths(
                    "IMAGES/Menus/GamePlay/DarkAges/DarkAgesBackgroundLeft.png",
                    "IMAGES/Menus/GamePlay/DarkAges/DarkAgesBackground.png",
                    "IMAGES/Menus/GamePlay/DarkAges/DarkAgesBackgroundRight.png"
                );

            case "Wallnut Bowling":
                return new BackgroundPaths(
                    "IMAGES/Menus/MiniGame/WallnutBowlingLeft.png",
                    "IMAGES/Menus/MiniGame/WallnutBowling.png",
                    "IMAGES/Menus/MiniGame/WallnutBowlingRight.png"
                );

            case "Vasebreaker":
                return new BackgroundPaths(
                    "IMAGES/Menus/MiniGame/VaseSmasherLeft.png",
                    "IMAGES/Menus/MiniGame/VaseSmasher.png",
                    "IMAGES/Menus/MiniGame/VaseSmasherRight.png"
                );

            case "I, Zombie":
                return new BackgroundPaths(
                    "IMAGES/Menus/MiniGame/IzombieLeft.png",
                    "IMAGES/Menus/MiniGame/Izombie.png",
                    "IMAGES/Menus/MiniGame/IzombieRight.png"
                );

            case "Beghouled":
                return new BackgroundPaths(
                    "IMAGES/Menus/MiniGame/BeghouledLeft.png",
                    "IMAGES/Menus/MiniGame/Beghouled.png",
                    "IMAGES/Menus/MiniGame/BeghouledRight.png"
                );

            case "Zombotany":
                return new BackgroundPaths(
                    "IMAGES/Menus/MiniGame/ZombotanyLeft.png",
                    "IMAGES/Menus/MiniGame/Zombotany.png",
                    "IMAGES/Menus/MiniGame/ZombotanyRight.png"
                );

            default:
                return getEgyptBackground();
        }
    }

    private BackgroundPaths getEgyptBackground() {
        return new BackgroundPaths(
            "IMAGES/Menus/GamePlay/AncientEgypt/AncientEgyptBackgroundLeft.png",
            "IMAGES/Menus/GamePlay/AncientEgypt/AncientEgyptBackground.png",
            "IMAGES/Menus/GamePlay/AncientEgypt/AncientEgyptBackgroundRight.png"
        );
    }

    private void drawDebugGrid() {
        float gridX = getGridX();
        float gridY = getGridY();

        float gridWidth = getGridWidth();
        float gridHeight = getGridHeight();

        int rows = ctx.getLevel().getRows();
        int columns = ctx.getLevel().getColumns();

        float cellWidth = getCellWidth();
        float cellHeight = getCellHeight();

        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        for (int col = 0; col <= columns; col++) {
            float x = gridX + col * cellWidth;

            shapeRenderer.line(
                x,
                gridY,
                x,
                gridY + gridHeight
            );
        }

        for (int row = 0; row <= rows; row++) {
            float y = gridY + row * cellHeight;

            shapeRenderer.line(
                gridX,
                y,
                gridX + gridWidth,
                y
            );
        }

        shapeRenderer.end();
    }

    /**
     * "همچنین خانه‌های نکرومنسی نیز باید روی زمین مشخص باشند" — a plain translucent
     * red fill over each Necromancy cell (Dark Ages only). No fancy effect needed,
     * just something the player can see.
     */
    private void drawNecromancyMarkers() {
        if (!(ctx.getSeason() instanceof DarkAgesSeason darkAges)) {
            return;
        }

        float gridX = getGridX();
        float gridY = getGridY();
        float cellWidth = getCellWidth();
        float cellHeight = getCellHeight();

        int rows = ctx.getLevel().getRows();
        int columns = ctx.getLevel().getColumns();

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0f, 0f, 0.28f);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (!darkAges.isNecromancyCell(row, col)) {
                    continue;
                }

                float x = gridX + col * cellWidth;
                float y = gridY + (rows - 1 - row) * cellHeight;

                shapeRenderer.rect(x, y, cellWidth, cellHeight);
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    private BackgroundPaths fallbackIfMissing(
        BackgroundPaths paths
    ) {
        if (!Gdx.files.internal(paths.left).exists()
            || !Gdx.files.internal(paths.center).exists()
            || !Gdx.files.internal(paths.right).exists()) {

            return getEgyptBackground();
        }

        return paths;
    }

    private static class BackgroundPaths {
        private final String left;
        private final String center;
        private final String right;

        private BackgroundPaths(
            String left,
            String center,
            String right
        ) {
            this.left = left;
            this.center = center;
            this.right = right;
        }
    }

    protected float getGridX() {
        return leftTexture.getWidth() + 252;
    }

    protected float getGridY() {
        return 80;
    }

    protected float getGridWidth() {
        return centerTexture.getWidth() - 285;
    }

    protected float getGridHeight() {
        return centerTexture.getHeight() - 278;
    }

    protected float getCellWidth() {
        return getGridWidth()
            / ctx.getLevel().getColumns();
    }

    protected float getCellHeight() {
        return getGridHeight()
            / ctx.getLevel().getRows();
    }

    protected boolean suppressDefaultUI() {
        return false;
    }

    protected Stage getStage() { return stage; }

    private float getCellCenterX(int column) {
        return getGridX()
            + column * getCellWidth()
            + getCellWidth() / 2f;
    }

    private float getCellCenterY(int row) {
        return getGridY()
            + getGridHeight()
            - row * getCellHeight()
            - getCellHeight() / 2f;
    }

    private void updateIntroCamera(float delta) {
        if (introFinished || pauseOverlay.isVisible()) {
            return;
        }

        introTime += delta;

        if (introTime < INTRO_WAIT) {
            return;
        }

        float progress =
            (introTime - INTRO_WAIT) / INTRO_DURATION;

        progress = MathUtils.clamp(
            progress,
            0f,
            1f
        );

        float smoothProgress =
            Interpolation.smooth.apply(progress);

        float currentWorldWidth = MathUtils.lerp(
            fullWorldWidth,
            gameplayWorldWidth,
            smoothProgress
        );

        worldViewport.setWorldSize(
            currentWorldWidth,
            worldHeight
        );

        worldViewport.update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            false
        );

        worldCamera.position.set(
            MathUtils.lerp(
                introCameraX,
                gameplayCameraX,
                smoothProgress
            ),
            cameraY,
            0f
        );

        worldCamera.zoom = 1f;
        worldCamera.update();

        if (progress >= 1f) {
            worldViewport.setWorldSize(
                gameplayWorldWidth,
                worldHeight
            );

            worldViewport.update(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight(),
                false
            );

            worldCamera.position.set(
                gameplayCameraX,
                cameraY,
                0f
            );

            worldCamera.zoom = 1f;
            worldCamera.update();

            introFinished = true;
        }
    }

    private void applyScreenShake(float delta) {
        if (!introFinished || pauseOverlay.isVisible() || winLoseOverlay.isVisible()) {
            return;
        }

        com.workshop.model.mechanisms.ScreenShake request;
        while ((request = ctx.pollScreenShake()) != null) {
            float remaining = Math.max(0f, shakeDuration - shakeTime) * shakeIntensity;
            if (request.intensity >= remaining) {
                shakeIntensity = request.intensity;
                shakeDuration = request.duration;
                shakeTime = 0f;
            }
        }

        if (shakeTime >= shakeDuration) {
            worldCamera.position.set(gameplayCameraX, cameraY, 0f);
            worldCamera.update();
            return;
        }

        shakeTime += delta;
        float falloff = MathUtils.clamp(1f - shakeTime / shakeDuration, 0f, 1f);
        float mag = shakeIntensity * falloff * falloff;
        worldCamera.position.set(
            gameplayCameraX + MathUtils.random(-mag, mag),
            cameraY + MathUtils.random(-mag * 0.55f, mag * 0.55f),
            0f
        );
        worldCamera.update();
    }

    private void updateGameplayStartDelay(float delta) {
        if (!introFinished || gameplayStarted) {
            return;
        }

        if (pauseOverlay.isVisible() || ctx.isPaused()) {
            return;
        }

        postIntroTime += delta;

        if (postIntroTime >= POST_INTRO_WAIT) {
            gameplayStarted = true;
            timeAccumulator = 0f;
        }
    }

    private float[][] getZombieIntroPoints(Season season) {
        switch (season.getName()) {

            case "Ancient Egypt":
                return new float[][] {
                    {0.32f, 0.38f},
                    {0.21f, 0.46f},
                    {0.25f, 0.60f},
                    {0.19f, 0.32f}

                };

            case "Big Wave Beach":
                return new float[][] {
                    {0.72f, 0.36f},
                    {0.30f, 0.43f},
                    {0.48f, 0.50f},
                    {0.35f, 0.57f},
                    {0.22f, 0.64f}
                };

            case "Dark Ages":
                return new float[][] {
                    {0.5f, 0.5f},
                    {0.18f, 0.55f},
                    {0.35f, 0.40f},
                    {0.2f, 0.30f}
                };

            case "FrozenCave":
                return new float[][] {
                    {0.17f, 0.50f},
                    {0.5f, 0.42f},
                    {0.30f, 0.34f},
                    {0.50f, 0.65f}

                };

            default:
                return new float[][] {
                    {0.28f, 0.38f},
                    {0.20f, 0.46f},
                    {0.12f, 0.54f}
                };
        }
    }

    private String[] getIntroZombiePamNames(Season season) {

        switch (season.getName()) {

            case "Ancient Egypt":
                return new String[] {
                    "ZOMBIE_EGYPT_BASIC",
                    "ZOMBIE_EGYPT_RA",
                    "ZOMBIE_EXPLORER",
                    "ZOMBIE_EGYPT_TOMBRAISER"
                };

            case "FrozenCave":
                return new String[] {
                    "ZOMBIE_ICEAGE_DODORIDER",
                    "HUNTER",
                    "TROGLOBITE"
                };

            case "Big Wave Beach":
                return new String[] {
                    "FISHERMAN",
                    "SNORKELER",
                    "OCTOPUS"
                };

            case "Dark Ages":
                return new String[] {
                    "JESTER",
                    "WIZARD",
                    "KING",
                    "DRAGON"
                };

            default:
                return new String[] {
                    "ZOMBIE_EGYPT_BASIC"
                };
        }
    }

    protected void showPlantOnMouse(Plant plant) {
        if (mousePlantPreview != null) {
            mousePlantPreview.remove();
            mousePlantPreview = null;
        }

        PlantAnimationSpec spec =
            plantPreviewResolver.resolve(
                plant.getName()
            );

        if (spec == null) {
            return;
        }

        mousePlantPreview = new PlantActor(
            plant,
            spec,
            Textures.getPamPlayer(),
            getCellHeight()
        );

        mousePlantPreview.setTouchable(
            Touchable.disabled
        );

        stage.addActor(mousePlantPreview);
    }

    protected void updatePlantMousePreview() {
        if (mousePlantPreview == null) {
            return;
        }

        mouseStagePosition.set(
            Gdx.input.getX(),
            Gdx.input.getY()
        );

        stage.screenToStageCoordinates(
            mouseStagePosition
        );

        mousePlantPreview.setPosition(
            mouseStagePosition.x,
            mouseStagePosition.y
        );
    }

    private void updateZombieMousePreview() {
        if (mouseZombiePreview == null) {
            return;
        }

        mouseStagePosition.set(
            Gdx.input.getX(),
            Gdx.input.getY()
        );

        stage.screenToStageCoordinates(
            mouseStagePosition
        );

        mouseZombiePreview.setPosition(
            mouseStagePosition.x,
            mouseStagePosition.y
        );
    }

    private void setupPlantingClick() {
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (pluckingMode) {
                    pluckPlantAtStage(
                        event.getStageX(),
                        event.getStageY()
                    );
                    return;
                }

                if (plantFoodFeedMode) {
                    feedPlantAtStage(event.getStageX(), event.getStageY());
                    return;
                }

                if (selectedZombieTypeForPlacement != null
                    && isIZombieLevel()) {

                    placeSelectedZombieAtStage(
                        event.getStageX(),
                        event.getStageY()
                    );
                    return;
                }

                if (selectedPlantForPlacement == null) {
                    return;
                }

                float stageX = event.getStageX();
                float stageY = event.getStageY();

                if (stageX < getGridX()
                    || stageX >= getGridX() + getGridWidth()
                    || stageY < getGridY()
                    || stageY >= getGridY() + getGridHeight()) {

                    return;
                }

                int column = (int) (
                    (stageX - getGridX()) / getCellWidth()
                );

                int row = (int) (
                    (getGridY() + getGridHeight() - stageY)
                        / getCellHeight()
                );

                plantSelectedPlant(column, row);
            }
        });
    }

    private void placeSelectedZombieAtStage(
        float stageX,
        float stageY
    ) {
        if (selectedZombieTypeForPlacement == null) {
            return;
        }

        if (stageX < getGridX()
            || stageX >= getGridX() + getGridWidth()
            || stageY < getGridY()
            || stageY >= getGridY() + getGridHeight()) {
            return;
        }

        int column = (int) (
            (stageX - getGridX()) / getCellWidth()
        );

        int row = (int) (
            (getGridY() + getGridHeight() - stageY)
                / getCellHeight()
        );

        if (!(ctx.getLevelManager()
            instanceof IZombieManager manager)) {
            return;
        }

        if (!manager.isValidPlacement(row, column, ctx)) {
            Toast.showError(
                stage,
                PvzSkin.get(),
                "Place zombies to the right of the red line."
            );
            return;
        }

        if (manager.isBrainEaten(row)) {
            Toast.showError(
                stage,
                PvzSkin.get(),
                "The brain in this row is already eaten."
            );
            return;
        }

        int cost = manager.getZombieCost(
            selectedZombieTypeForPlacement
        );

        if (ctx.getSunAmount() < cost) {
            Toast.showError(
                stage,
                PvzSkin.get(),
                "Not enough sun!"
            );
            return;
        }

        Izambi izambi = Izambi.getActiveInstance();

        if (izambi == null) {
            Toast.showError(
                stage,
                PvzSkin.get(),
                "I-Zombie is not active."
            );
            return;
        }

        boolean placed = izambi.placeZombie(
            selectedZombieTypeForPlacement,
            row,
            column
        );

        if (placed) {
            clearZombieSelection();
            updateHud();
        }
    }

    private void togglePlantFoodFeedMode() {

        if (currentPlantFoodCount() <= 0) {

            clearPlantFoodFeedMode();

            if (plantFoodButton != null) {
                plantFoodButton.setChecked(false);
            }

            Toast.showError(
                stage,
                PvzSkin.get(),
                "No plant food!"
            );

            return;
        }

        plantFoodFeedMode =
            !plantFoodFeedMode;

        if (plantFoodFeedMode) {

            clearPluckingMode();
            clearPlantSelection();
            clearZombieSelection();

            hideSystemCursor();
            showPlantFoodOnMouse();

            if (plantFoodButton != null) {
                plantFoodButton.setChecked(true);
            }

            Toast.showInfo(
                stage,
                PvzSkin.get(),
                "Select a plant to feed"
            );

        } else {

            clearPlantFoodFeedMode();
        }
    }

    private void feedPlantAtStage(float stageX, float stageY) {
        if (stageX < getGridX()
            || stageX >= getGridX() + getGridWidth()
            || stageY < getGridY()
            || stageY >= getGridY() + getGridHeight()) {
            return;
        }

        int column = (int) ((stageX - getGridX()) / getCellWidth());
        int row = (int) (
            (getGridY() + getGridHeight() - stageY) / getCellHeight()
        );

        Plant[][] grid = ctx.getPlantGrid();
        if (row < 0 || row >= grid.length
            || column < 0 || column >= grid[row].length) {
            return;
        }

        Plant plant = grid[row][column];
        if (plant == null || plant.isDead()) {
            Toast.showError(stage, PvzSkin.get(), "No plant there.");
            return;
        }

        if (currentPlantFoodCount() <= 0) {
            clearPlantFoodFeedMode();

            Toast.showError(
                stage,
                PvzSkin.get(),
                "No plant food!"
            );

            return;
        }

        if (!UserManager.getInstance().usePlantFood(1)) {
            clearPlantFoodFeedMode();

            Toast.showError(
                stage,
                PvzSkin.get(),
                "No plant food!"
            );

            return;
        }

        plant.activatePlantFood(ctx);

        clearPlantFoodFeedMode();

        updateHud();

        Toast.showSuccess(
            stage,
            PvzSkin.get(),
            "Fed " + plant.getName() + "!"
        );
    }

    private void plantSelectedPlant(int column, int row) {
        if (selectedPlantForPlacement == null) {
            return;
        }

        Gdx.app.log(
            "PlantingUI",
            "TRY " + selectedPlantForPlacement.getName()
                + " row=" + row
                + " col=" + column
                + " sun=" + ctx.getSunAmount()
                + " cost=" + selectedPlantForPlacement.getSunCost()
        );

        boolean usingHeldSeed =
            ctx.getHeldSeed() != null
                && ctx
                .getHeldSeed()
                .equalsIgnoreCase(
                    selectedPlantForPlacement
                        .getName()
                );

        boolean conveyorLevel =
            ctx.getLevelManager()
                instanceof ConveyorBeltManager;

        if (!usingHeldSeed
            && !isConveyorLevel()
            && !conveyorLevel
            && ctx.isOnCooldown(selectedPlantForPlacement.getName())) {

            showPlantCooldownError(selectedPlantForPlacement.getName());
            return;
        }

        if (!usingHeldSeed
            && !isConveyorLevel()
            && !conveyorLevel
            && ctx.getSunAmount()
            < selectedPlantForPlacement.getSunCost()) {

            Toast.showError(
                stage,
                PvzSkin.get(),
                "Not enough sun!"
            );

            return;
        }

        Plant before =
            ctx.getPlantGrid()[row][column];

        plantingCommand.execute(
            new String[] {
                selectedPlantForPlacement.getName(),
                String.valueOf(column),
                String.valueOf(row)
            }
        );

        Plant after =
            ctx.getPlantGrid()[row][column];

        Gdx.app.log(
            "PlantingUI",
            "AFTER = " + (after == null ? "null" : after.getName())
        );

        if (after != null && after != before) {
            // ثبت زمان کاشت گیاه در لحظه موفقیت عملیات کاشت
            int currentSecond = ctx.getTimeManager().getTotalSeconds();
            after.setPlantTimeSecond(currentSecond);

            clearPlantSelection();
            updateHud();
            return;
        }

        boolean graveBuster = selectedPlantForPlacement.getAbilityParams() != null
            && "GRAVE_DESTROY".equals(
            selectedPlantForPlacement.getAbilityParams().get("explosiveType")
        );
        boolean onGrave = row >= 0
            && row < ctx.getGraveGrid().length
            && column >= 0
            && column < ctx.getGraveGrid()[row].length
            && ctx.getGraveGrid()[row][column] != null;

        Toast.showError(
            stage,
            PvzSkin.get(),
            graveBuster && !onGrave
                ? "Plant Grave Buster on a grave."
                : "Can't plant there."
        );
    }
    public void showPlantCooldownError(String plantName) {
        int seconds = (int) Math.ceil(
            ctx.getRemainingCooldownSeconds(plantName)
        );
        String message = seconds > 0
            ? plantName + " is still recharging (" + seconds + "s)"
            : plantName + " is still recharging";
        Toast.showError(stage, PvzSkin.get(), message);
    }

    protected void clearPlantPreview() {
        if (mousePlantPreview != null) {
            mousePlantPreview.remove();
            mousePlantPreview = null;
        }
    }

    protected void clearPlantSelection() {
        selectedPlantForPlacement = null;

        if (mousePlantPreview != null) {
            mousePlantPreview.remove();
            mousePlantPreview = null;
        }

        for (PlantCardActor card : seedBankCards) {
            card.setFocused(false);
        }
    }

    private void clearZombieSelection() {
        selectedZombieTypeForPlacement = null;

        if (mouseZombiePreview != null) {
            mouseZombiePreview.remove();
            mouseZombiePreview = null;
        }

        for (ZombieCardActor card : zombieBankCards) {
            card.setFocused(false);
        }
    }

    private void updatePlantingHover() {
        if (selectedPlantForPlacement == null
            && selectedZombieTypeForPlacement == null
            && !plantFoodFeedMode
            && !pluckingMode) {

            hoveredPlantRow = -1;
            hoveredPlantColumn = -1;
            return;
        }

        mouseStagePosition.set(
            Gdx.input.getX(),
            Gdx.input.getY()
        );

        stage.screenToStageCoordinates(mouseStagePosition);

        float x = mouseStagePosition.x;
        float y = mouseStagePosition.y;

        if (x < getGridX()
            || x >= getGridX() + getGridWidth()
            || y < getGridY()
            || y >= getGridY() + getGridHeight()) {

            hoveredPlantRow = -1;
            hoveredPlantColumn = -1;
            return;
        }

        hoveredPlantColumn =
            (int) ((x - getGridX()) / getCellWidth());

        hoveredPlantRow =
            (int) (
                (getGridY() + getGridHeight() - y)
                    / getCellHeight()
            );
    }

    private void drawPlantingHighlight() {
        if (hoveredPlantRow < 0
            || hoveredPlantColumn < 0) {
            return;
        }

        float cellX =
            getGridX()
                + hoveredPlantColumn * getCellWidth();

        float rowY =
            getGridY()
                + getGridHeight()
                - (hoveredPlantRow + 1) * getCellHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer.setProjectionMatrix(
            stage.getCamera().combined
        );

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        if (pluckingMode) {

            Plant[][] grid =
                ctx.getPlantGrid();

            Plant plant =
                grid[hoveredPlantRow]
                    [hoveredPlantColumn];

            boolean hasPlant =
                plant != null
                    && !plant.isDead();

            if (hasPlant) {
                shapeRenderer.setColor(
                    1f,
                    1f,
                    1f,
                    0.25f
                );
            } else {
                shapeRenderer.setColor(
                    1f,
                    0.15f,
                    0.15f,
                    0.28f
                );
            }

            shapeRenderer.rect(
                cellX,
                rowY,
                getCellWidth(),
                getCellHeight()
            );
        }
        else if (
            selectedZombieTypeForPlacement != null
                && ctx.getLevelManager()
                instanceof IZombieManager manager
        ) {

            boolean validPlacement =
                manager.isValidPlacement(
                    hoveredPlantRow,
                    hoveredPlantColumn,
                    ctx
                )
                    && !manager.isBrainEaten(hoveredPlantRow);

            if (validPlacement) {
                shapeRenderer.setColor(
                    1f,
                    1f,
                    1f,
                    0.20f
                );
            } else {
                shapeRenderer.setColor(
                    1f,
                    0.15f,
                    0.15f,
                    0.28f
                );
            }

            shapeRenderer.rect(
                getGridX(),
                rowY,
                getGridWidth(),
                getCellHeight()
            );
        }

        /*
         * Plant:
         * رفتار قبلی؛ فقط خانه زیر موس روشن شود.
         */
        else {
            shapeRenderer.setColor(
                1f,
                1f,
                1f,
                0.20f
            );

            shapeRenderer.rect(
                cellX,
                rowY,
                getCellWidth(),
                getCellHeight()
            );
        }

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private boolean isConveyorLevel() {
        return ctx.getLevelManager()
            instanceof ConveyorBeltManager;
    }

    private boolean isIZombieLevel() {
        return ctx.getLevel().getLevelType()
            == LevelType.Izambie_MG
            && ctx.getLevelManager()
            instanceof IZombieManager;
    }

    private List<Plant> getAvailableSeedBankPlants() {
        if (ctx.getLevelManager()
            instanceof ConveyorBeltManager conveyorManager) {

            return conveyorManager.getConveyorBelt();
        }

        return ctx.getActivePlants();
    }

    private void rebuildSeedBank() {
        if (seedBankTable != null) {
            seedBankTable.remove();
        }

        buildSeedBank(PvzSkin.get());
    }

    private String getConveyorSignature(
        ConveyorBeltManager manager
    ) {
        StringBuilder result = new StringBuilder();

        for (Plant plant : manager.getConveyorBelt()) {
            result.append(System.identityHashCode(plant))
                .append(":")
                .append(plant.getName())
                .append("|");
        }

        return result.toString();
    }

    private void updateConveyorSeedBank() {
        if (!(ctx.getLevelManager()
            instanceof ConveyorBeltManager conveyorManager)) {
            return;
        }

        String newSignature =
            getConveyorSignature(conveyorManager);

        if (newSignature.equals(lastConveyorSignature)) {
            return;
        }

        lastConveyorSignature = newSignature;

        rebuildSeedBank();
    }

    private void togglePluckingMode() {

        if (pluckingMode) {
            clearPluckingMode();
            return;
        }

        clearPlantFoodFeedMode();

        clearPlantSelection();
        clearZombieSelection();

        hideSystemCursor();
        showShovelOnMouse();

        pluckingMode = true;

        if (shovelButton != null) {
            shovelButton.setChecked(true);
        }

        Toast.showInfo(
            stage,
            PvzSkin.get(),
            "Select a plant to remove"
        );
    }

    private void clearPluckingMode() {

        boolean wasPlucking = pluckingMode;

        pluckingMode = false;

        if (shovelButton != null) {
            shovelButton.setChecked(false);
        }

        if (shovelMousePreview != null) {
            shovelMousePreview.remove();
            shovelMousePreview = null;
        }

        if (wasPlucking) {
            restoreSystemCursor();
        }
    }

    private void showShovelOnMouse() {

        if (shovelMousePreview != null) {
            shovelMousePreview.remove();
            shovelMousePreview = null;
        }

        shovelMousePreview =
            new Image(shovelCursorTexture);

        float height = SHOVEL_CURSOR_HEIGHT;

        float width =
            height
                * shovelCursorTexture.getWidth()
                / shovelCursorTexture.getHeight();

        shovelMousePreview.setSize(
            width,
            height
        );

        shovelMousePreview.setTouchable(
            Touchable.disabled
        );

        stage.addActor(shovelMousePreview);
        shovelMousePreview.toFront();
    }

    private void updateShovelMousePreview() {

        if (!pluckingMode
            || shovelMousePreview == null) {
            return;
        }

        mouseStagePosition.set(
            Gdx.input.getX(),
            Gdx.input.getY()
        );

        stage.screenToStageCoordinates(
            mouseStagePosition
        );

        shovelMousePreview.setPosition(
            mouseStagePosition.x
                - shovelMousePreview.getWidth() * 0.15f,

            mouseStagePosition.y
                - shovelMousePreview.getHeight() * 0.85f
        );

        shovelMousePreview.toFront();
    }

    private void pluckPlantAtStage(
        float stageX,
        float stageY
    ) {

        if (stageX < getGridX()
            || stageX >= getGridX() + getGridWidth()
            || stageY < getGridY()
            || stageY >= getGridY() + getGridHeight()) {

            return;
        }

        int column =
            (int) (
                (stageX - getGridX())
                    / getCellWidth()
            );

        int row =
            (int) (
                (getGridY()
                    + getGridHeight()
                    - stageY)
                    / getCellHeight()
            );

        Plant[][] grid =
            ctx.getPlantGrid();

        if (row < 0
            || row >= grid.length
            || column < 0
            || column >= grid[row].length) {

            return;
        }

        Plant plant =
            grid[row][column];

        if (plant == null || plant.isDead()) {

            Toast.showError(
                stage,
                PvzSkin.get(),
                "No plant there."
            );

            return;
        }

        String plantName =
            plant.getName();

        pluckingCommand.execute(
            new String[]{
                String.valueOf(column),
                String.valueOf(row)
            }
        );

        clearPluckingMode();

        Toast.showSuccess(
            stage,
            PvzSkin.get(),
            "Removed " + plantName + "!"
        );
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        screenElapsedTime += delta;

        updateIntroCamera(delta);
        updateGameplayStartDelay(delta);
        applyScreenShake(delta);

        if (gameplayStarted
            && !pauseOverlay.isVisible()
            && !winLoseOverlay.isVisible()
            && !ctx.isPaused()
            && !ctx.isGameEnded()
            && !dialogueBlocking) {

            timeAccumulator += delta;

            while (timeAccumulator >= TICK_DURATION
                && !ctx.isGameEnded()) {

                ctx.getTimeManager().advanceTime(1);
                gameEngine.update(TICK_DURATION);
                timeAccumulator -= TICK_DURATION;
            }
        }

        updateHud();
        updatePlantMousePreview();
        updateZombieMousePreview();
        updateShovelMousePreview();
        updatePlantFoodMousePreview();
        updatePlantingHover();
        updateConveyorSeedBank();

        for (PlantCardActor card : seedBankCards) {

            card.updateAnimation(delta);

            double cooldown =
                ctx
                    .getRemainingCooldownSeconds(
                        card.getPlant().getName()
                    );

            card.setCooldownRemaining(
                cooldown
            );
        }

        for (ZombieCardActor card : zombieBankCards) {

            card.updateAnimation(delta);

            double cooldown = 0;

            if (ctx.getLevelManager()
                instanceof IZombieManager manager) {

                cooldown =
                    manager
                        .getRemainingZombieCooldownSeconds(
                            card.getZombieType(),
                            ctx
                        );
            }

            card.setCooldownRemaining(
                cooldown
            );
        }

        if (ctx.isGameEnded() && !endDialogueShown && !handlesOwnEndOfGame()) {
            endDialogueShown = true;
            showEndOfLevelDialogue();
        }

        if (screenElapsedTime >= MISSION_DISPLAY_TIME && !ctx.isGameEnded()) {
            String announcement;

            while ((announcement = ctx.pollAnnouncement()) != null) {
                Toast.showAnnouncement(
                    stage,
                    PvzSkin.get(),
                    announcement
                );
            }

            String soundCue;
            while ((soundCue = ctx.pollSoundCue()) != null) {
                if (soundCue.startsWith("sfx:")) {
                    Audio.playSfx(soundCue.substring("sfx:".length()));
                } else {
                    Audio.playMusic(soundCue, false);
                }
            }
        }

        if (pauseOverlay.isVisible() || winLoseOverlay.isVisible()) {
            stage.act(0);
        } else {
            stage.act(delta);
        }

        Textures.getInstance().update();

        stage.draw();
        drawPlantingHighlight();

        if (!pauseOverlay.isVisible() && !winLoseOverlay.isVisible()) {
            drawNecromancyMarkers();
        }

        if (!pauseOverlay.isVisible()
            && !winLoseOverlay.isVisible()
            && UserManager.getInstance().getCurrentUser() != null
            && UserManager.getInstance().getCurrentUser().isGridEnabled()) {

            drawDebugGrid();
        }
    }

    private void selectDroppedSeed(
        String plantName
    ) {
        clearPluckingMode();
        try {
            Plant plant =
                ctx
                    .getPlantFactory()
                    .create(plantName);

            clearPlantFoodFeedMode();
            selectedPlantForPlacement = plant;

            for (PlantCardActor card
                : seedBankCards) {

                card.setFocused(false);
            }

            showPlantOnMouse(
                plant
            );

        } catch (Exception e) {
            ctx.setHeldSeed(null);

            Gdx.app.error(
                "VaseSeed",
                "Could not pick plant: "
                    + plantName,
                e
            );
        }
    }

    private void showPlantFoodOnMouse() {

        if (plantFoodMousePreview != null) {
            plantFoodMousePreview.remove();
            plantFoodMousePreview = null;
        }

        plantFoodMousePreview =
            new Image(plantFoodCursorTexture);

        float height = 42f;

        float width =
            height
                * plantFoodCursorTexture.getWidth()
                / plantFoodCursorTexture.getHeight();

        plantFoodMousePreview.setSize(
            width,
            height
        );

        plantFoodMousePreview.setTouchable(
            Touchable.disabled
        );

        stage.addActor(plantFoodMousePreview);

        plantFoodMousePreview.toFront();
    }

    private void updatePlantFoodMousePreview() {

        if (!plantFoodFeedMode
            || plantFoodMousePreview == null) {
            return;
        }

        mouseStagePosition.set(
            Gdx.input.getX(),
            Gdx.input.getY()
        );

        stage.screenToStageCoordinates(
            mouseStagePosition
        );

        plantFoodMousePreview.setPosition(
            mouseStagePosition.x
                - plantFoodMousePreview.getWidth() * 0.15f,

            mouseStagePosition.y
                - plantFoodMousePreview.getHeight() * 0.85f
        );

        plantFoodMousePreview.toFront();
    }

    private void testPeashooterParts() {

        PamPlayer pamPlayer =
            Textures.getPamPlayer();

        PamPlayer.AnimationPart root =
            pamPlayer.getParts(
                "768/INITIAL/PLANT/PEASHOOTER/PEASHOOTER.PAM"
            );

        printParts(root, "");
    }


    private void printParts(
        PamPlayer.AnimationPart part,
        String space
    ) {

        if (part == null) {
            return;
        }

        Gdx.app.log(
            "PEASHOOTER_PART",
            space + part.name
        );

        if (part.children != null) {
            for (Object child : part.children) {

                printParts(
                    (PamPlayer.AnimationPart) child,
                    space + "  "
                );
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(
            width,
            height,
            false
        );

        worldCamera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        restoreSystemCursor();
    }

    @Override
    public void dispose() {
        restoreSystemCursor();

        if (hiddenCursor != null) {
            hiddenCursor.dispose();
            hiddenCursor = null;
        }

        stage.dispose();

        Audio.stopMusic();

        leftTexture.dispose();
        centerTexture.dispose();
        rightTexture.dispose();
        shapeRenderer.dispose();
        pauseOverlay.dispose();
        winLoseOverlay.dispose();
        if (conveyorBeltLayer != null) {
            conveyorBeltLayer.dispose();
        }
    }
}
