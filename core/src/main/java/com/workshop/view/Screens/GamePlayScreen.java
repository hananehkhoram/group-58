package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.workshop.model.GameContext;
import com.workshop.model.season.Season;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import pvz.skin.PvzSkin;
import com.workshop.controller.repository.Textures;
import com.workshop.view.gameplay.PlantAnimationLayer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.workshop.view.gameplay.ZombieIntroLayer;

public class GamePlayScreen implements Screen {

    private final Stage stage;
    private final PauseOverlay pauseOverlay;
    private final GameContext gameContext;

    private final Texture leftTexture;
    private final Texture centerTexture;
    private final Texture rightTexture;

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
    private static final float INTRO_WAIT = 3;
    private static final float INTRO_DURATION = 1.4f;

    private float introTime;
    private boolean introFinished;

    public GamePlayScreen(
        GameContext gameContext,
        Runnable exitAction
    ) {
        this.exitAction = exitAction;
        this.gameContext = gameContext;

        Season season = gameContext.getSeason();

        Skin skin = PvzSkin.get();

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

            zombieIntroLayer.addZombie(
                introZombies[i],
                rightX + rightWidth * point[0],
                height * point[1]
            );
        }

        stage.addActor(zombieIntroLayer);

        PlantAnimationLayer plantAnimationLayer =
            new PlantAnimationLayer(
                gameContext,
                getGridX(),
                getGridY(),
                getGridWidth(),
                getGridHeight()
            );

        stage.addActor(plantAnimationLayer);

        pauseOverlay = new PauseOverlay(
            stage,
            skin,
            gameContext,
            () -> {
                System.out.println("Restart clicked");
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


        Table topBar = new Table();
        topBar.setFillParent(true);
        topBar.top().right();
        topBar.padTop(20);
        topBar.padRight(20);

        topBar.add(pauseTestButton)
            .size(70, 70);

        stage.addActor(topBar);

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

    private float[][] getZombieIntroPoints(Season season) {
        switch (season.getName()) {

            case "Ancient Egypt":
                return new float[][] {
                    {0.28f, 0.38f},
                    {0.15f, 0.46f},
                    {0.12f, 0.65f},
                    {0.23f, 0.60f}
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
                    {0.38f, 0.34f},
                    {0.29f, 0.41f},
                    {0.20f, 0.48f},
                    {0.12f, 0.55f}
                };

            case "FrozenCave":
                return new float[][] {
                    {0.30f, 0.34f},
                    {0.5f, 0.42f},
                    {0.16f, 0.50f},
                    {0.6f, 0.65f}
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
                    "DODO",
                    "HUNTER",
                    "TROGLOBITE"
                };

            case "Big Wave Beach":
                return new String[] {
                    "FISHERMAN",
                    "SNORKEL",
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

        updateIntroCamera(delta);

        if (pauseOverlay.isVisible()) {
            stage.act(0);
        } else {
            stage.act(delta);
        }

        Textures.getInstance().update();

        stage.draw();

        if (!pauseOverlay.isVisible()) {
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

        leftTexture.dispose();
        centerTexture.dispose();
        rightTexture.dispose();
        shapeRenderer.dispose();
        pauseOverlay.dispose();
    }
}
