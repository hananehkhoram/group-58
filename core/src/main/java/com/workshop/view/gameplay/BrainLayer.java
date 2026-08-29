package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;

public final class BrainLayer extends Group implements Disposable {

    private static final float BRAIN_HEIGHT_TO_CELL_RATIO = 0.55f;

    private final GameContext gameContext;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Image[] brainActors;

    private final Texture brainTexture;

    public BrainLayer(
        GameContext gameContext,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;

        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        int rows =
            gameContext.getLevel().getRows();

        brainActors =
            new Image[rows];

        brainTexture =
            createBrainTexture();

        createBrains();
    }

    private void createBrains() {
        TextureRegion region =
            new TextureRegion(brainTexture);

        TextureRegionDrawable drawable =
            new TextureRegionDrawable(region);

        for (int row = 0; row < brainActors.length; row++) {

            Image brain =
                new Image(drawable);

            brain.setTouchable(
                com.badlogic.gdx.scenes.scene2d.Touchable.disabled
            );

            float height =
                getCellHeight()
                    * BRAIN_HEIGHT_TO_CELL_RATIO;

            float aspect =
                (float) brainTexture.getWidth()
                    / brainTexture.getHeight();

            float width =
                height * aspect;

            brain.setSize(
                width,
                height
            );

            brain.setPosition(
                getBrainX(width),
                getBrainY(row, height)
            );

            brainActors[row] = brain;

            addActor(brain);
        }
    }

    @Override
    public void act(float delta) {
        updateBrains();

        super.act(delta);
    }

    private void updateBrains() {
        if (!(gameContext.getLevelManager()
            instanceof IZombieManager manager)) {

            setVisible(false);
            return;
        }

        for (int row = 0; row < brainActors.length; row++) {

            Image brain =
                brainActors[row];

            if (brain == null) {
                continue;
            }

            brain.setVisible(
                !manager.isBrainEaten(row)
            );
        }
    }

    private float getBrainX(float brainWidth) {
        return gridX
            - getCellWidth() * 0.55f
            - brainWidth / 2f;
    }

    private float getBrainY(
        int row,
        float brainHeight
    ) {
        float centerY =
            gridY
                + gridHeight
                - row * getCellHeight()
                - getCellHeight() / 2f;

        return centerY
            - brainHeight / 2f;
    }

    private float getCellWidth() {
        return gridWidth
            / gameContext.getLevel().getColumns();
    }

    private float getCellHeight() {
        return gridHeight
            / gameContext.getLevel().getRows();
    }

    private Texture createBrainTexture() {
        int width = 110;
        int height = 75;

        Pixmap pixmap =
            new Pixmap(
                width,
                height,
                Pixmap.Format.RGBA8888
            );

        pixmap.setColor(
            0f,
            0f,
            0f,
            0f
        );

        pixmap.fill();

        Color brainColor =
            new Color(
                0.92f,
                0.42f,
                0.55f,
                1f
            );

        pixmap.setColor(brainColor);

        pixmap.fillCircle(
            38,
            36,
            27
        );

        pixmap.fillCircle(
            70,
            36,
            27
        );

        pixmap.fillCircle(
            53,
            25,
            25
        );

        pixmap.setColor(
            0.55f,
            0.16f,
            0.28f,
            1f
        );

        pixmap.drawLine(
            53,
            12,
            53,
            59
        );

        pixmap.drawLine(
            25,
            31,
            43,
            38
        );

        pixmap.drawLine(
            28,
            48,
            45,
            43
        );

        pixmap.drawLine(
            80,
            27,
            63,
            36
        );

        pixmap.drawLine(
            81,
            49,
            64,
            43
        );

        Texture texture =
            new Texture(pixmap);

        pixmap.dispose();

        return texture;
    }

    @Override
    public void dispose() {
        brainTexture.dispose();
    }
}
