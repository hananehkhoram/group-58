package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.season.DarkAgesSeason;
import com.workshop.model.season.Season;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;
import pvz.skin.PvzSkin;
import com.workshop.controller.repository.Audio;
import com.workshop.controller.repository.Textures;
import com.workshop.view.gameplay.GraveAnimationLayer;
import com.workshop.view.gameplay.PlantAnimationLayer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.workshop.view.gameplay.ZombieIntroLayer;
import com.workshop.view.gameplay.ZombieAnimationLayer;
import com.workshop.model.level.DialogueLine;

import java.util.List;
import com.workshop.view.gameplay.SunAnimationLayer;

public class GamePlayScreen implements Screen {

    private final Stage stage;
    private final PauseOverlay pauseOverlay;
    private final WinLoseOverlay winLoseOverlay;
    private boolean resultOverlayShown = false;

    private final GameEngine gameEngine;
    private final GameContext gameContext;

    private static final float TICK_DURATION = 0.1f;
    private float timeAccumulator = 0f;

    private final Texture leftTexture;
    private final Texture centerTexture;
    private final Texture rightTexture;

    private Label sunAmountLabel;
    private Label plantFoodAmountLabel;
    private Label waveLabel;
    private ProgressBar zombieProgressBar;

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

    private static final float MISSION_DISPLAY_TIME = 6f;
    private float screenElapsedTime = 0f;

    private boolean dialogueBlocking = false;
    private boolean endDialogueShown = false;


    public GamePlayScreen(
        GameContext gameContext,
        Runnable restartAction,
        Runnable exitAction
    ) {
        this.exitAction = exitAction;

        this.gameContext = gameContext;
        this.gameEngine = gameContext.getGameEngine();
        Season season = gameContext.getSeason();
        Level level = gameContext.getLevel();

        Skin skin = PvzSkin.get();

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

        com.workshop.view.gameplay.WaterLayer waterLayer =
            new com.workshop.view.gameplay.WaterLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(waterLayer);

        PlantAnimationLayer plantAnimationLayer =
            new PlantAnimationLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(plantAnimationLayer);

        ZombieAnimationLayer zombieAnimationLayer =
            new ZombieAnimationLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(zombieAnimationLayer);

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
            new ImageButton(skin, "ingame_pause");

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
        sunCounter.add(sunAmountLabel);

        Image plantFoodIcon = new Image(
            skin.get("plantfood", ImageButton.ImageButtonStyle.class).imageUp
        );

        plantFoodAmountLabel = new Label(
            String.valueOf(currentPlantFoodCount()),
            skin
        );

        Table plantFoodCounter = new Table();
        plantFoodCounter.add(plantFoodIcon).size(48, 48).padRight(8);
        plantFoodCounter.add(plantFoodAmountLabel);

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

        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top();
        hudTable.pad(20);
        Table leftCounters = new Table();
        leftCounters.add(sunCounter).left().row();
        leftCounters.add(plantFoodCounter).left().padTop(6);

        hudTable.add(leftCounters)
            .left()
            .top();

        hudTable.add(progressColumn)
            .expandX()
            .padLeft(20)
            .padRight(20);

        hudTable.add(pauseTestButton)
            .size(70, 70)
            .right()
            .top();

        stage.addActor(hudTable);

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

        // --- دیالوگ شروع مرحله (اختیاری) و بعد از آن، منوی آغاز مرحله ---
        List<DialogueLine> introDialogue = level.getIntroDialogue();

        if (introDialogue != null && !introDialogue.isEmpty()) {
            dialogueBlocking = true;

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

    }
    private int currentPlantFoodCount() {
        com.workshop.model.user.User user = UserManager.getInstance().getCurrentUser();
        return user != null ? user.getPlantFoodCount() : 0;
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
        sunAmountLabel.setText(
            String.valueOf(gameContext.getSunAmount())
        );
        plantFoodAmountLabel.setText(
            String.valueOf(currentPlantFoodCount())
        );

        com.workshop.model.mechanisms.Wave[] waves =
            gameContext.getLevel().getWaves();

        int totalWaves = waves != null ? waves.length : 0;


        int currentWaveIndex = gameContext.getCurrentWaveIndex();

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
        boolean won = gameContext.isPlayerWon();

        List<DialogueLine> lines = won
            ? gameContext.getLevel().getWinDialogue()
            : gameContext.getLevel().getLoseDialogue();

        Runnable showFinalOverlay = won
            ? winLoseOverlay::showWin
            : winLoseOverlay::showLose;

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
                    "IMAGES/Menus/GamePlay/WallnutBowlingLeft.png",
                    "IMAGES/Menus/GamePlay/WallnutBowling.png",
                    "IMAGES/Menus/GamePlay/WallnutBowlingRight.png"
                );

            case "Vasebreaker":
                return new BackgroundPaths(
                    "IMAGES/Menus/GamePlay/VasebreakerLeft.png",
                    "IMAGES/Menus/GamePlay/Vasebreaker.png",
                    "IMAGES/Menus/GamePlay/VasebreakerRight.png"
                );

            case "I, Zombie":
                return new BackgroundPaths(
                    "IMAGES/Menus/GamePlay/IZombieLeft.png",
                    "IMAGES/Menus/GamePlay/IZombie.png",
                    "IMAGES/Menus/GamePlay/IZombieRight.png"
                );

            case "Beghouled":
                return new BackgroundPaths(
                    "IMAGES/Menus/GamePlay/BeghouledLeft.png",
                    "IMAGES/Menus/GamePlay/Beghouled.png",
                    "IMAGES/Menus/GamePlay/BeghouledRight.png"
                );

            case "Zombotany":
                return new BackgroundPaths(
                    "IMAGES/Menus/GamePlay/ZombotanyLeft.png",
                    "IMAGES/Menus/GamePlay/Zombotany.png",
                    "IMAGES/Menus/GamePlay/ZombotanyRight.png"
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

        int rows = gameContext.getLevel().getRows();
        int columns = gameContext.getLevel().getColumns();

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
        if (!(gameContext.getSeason() instanceof DarkAgesSeason darkAges)) {
            return;
        }

        float gridX = getGridX();
        float gridY = getGridY();
        float cellWidth = getCellWidth();
        float cellHeight = getCellHeight();

        int rows = gameContext.getLevel().getRows();
        int columns = gameContext.getLevel().getColumns();

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

    private float getGridX() {
        return leftTexture.getWidth() + 252;
    }

    private float getGridY() {
        return 80;
    }

    private float getGridWidth() {
        return centerTexture.getWidth() - 285;
    }

    private float getGridHeight() {
        return centerTexture.getHeight() - 278;
    }

    private float getCellWidth() {
        return getGridWidth()
            / gameContext.getLevel().getColumns();
    }

    private float getCellHeight() {
        return getGridHeight()
            / gameContext.getLevel().getRows();
    }

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

    private void updateGameplayStartDelay(float delta) {
        if (!introFinished || gameplayStarted) {
            return;
        }

        if (pauseOverlay.isVisible() || gameContext.isPaused()) {
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

        if (gameplayStarted
            && !pauseOverlay.isVisible()
            && !winLoseOverlay.isVisible()
            && !gameContext.isPaused()
            && !gameContext.isGameEnded()
            && !dialogueBlocking) {

            timeAccumulator += delta;

            while (timeAccumulator >= TICK_DURATION
                && !gameContext.isGameEnded()) {

                gameContext.getTimeManager().advanceTime(1);
                gameEngine.update(TICK_DURATION);
                timeAccumulator -= TICK_DURATION;
            }
        }

        // نمایش پنجره برد یا باخت فقط یک بار
        if (gameContext.isGameEnded() && !resultOverlayShown) {
            resultOverlayShown = true;

            if (gameContext.isPlayerWon()) {
                winLoseOverlay.showWin();
                Audio.playMusic("music/winmusic", false);
            } else {
                winLoseOverlay.showLose();
                Audio.playMusic("music/losemusic", false);
            }
        }

        updateHud();

        if (gameContext.isGameEnded() && !endDialogueShown) {
            endDialogueShown = true;
            showEndOfLevelDialogue();
        }

        if (screenElapsedTime >= MISSION_DISPLAY_TIME && !gameContext.isGameEnded()) {
            String announcement;

            while ((announcement = gameContext.pollAnnouncement()) != null) {
                Toast.showAnnouncement(
                    stage,
                    PvzSkin.get(),
                    announcement
                );
            }

            String soundCue;
            while ((soundCue = gameContext.pollSoundCue()) != null) {
                Audio.playMusic(soundCue, false);
            }
        }

        if (pauseOverlay.isVisible() || winLoseOverlay.isVisible()) {
            stage.act(0);
        } else {
            stage.act(delta);
        }

        Textures.getInstance().update();

        stage.draw();

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
    }

    @Override
    public void dispose() {
        stage.dispose();

        Audio.stopMusic();

        leftTexture.dispose();
        centerTexture.dispose();
        rightTexture.dispose();
        shapeRenderer.dispose();
        pauseOverlay.dispose();
        winLoseOverlay.dispose();
    }
}
