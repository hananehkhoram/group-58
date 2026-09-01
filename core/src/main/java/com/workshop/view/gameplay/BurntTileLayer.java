package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;

import java.util.List;

/**
 * خانه‌های سوختهٔ زامباس اژدها: PAM رسمی Scorched_Earth_Tile روی هر خانه‌ای
 * که ctx.isBurnedCell برقرار است (۴ ثانیه غیرقابل‌کاشت).
 */
public final class BurntTileLayer extends Actor {

    private static final String TILE_PAM =
        "768/FULL/EFFECTS/SCORCHED_EARTH_TILE/SCORCHED_EARTH_TILE.PAM";

    private static Texture fallbackTile;

    private final GameContext gameContext;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private String tileClip;
    private boolean clipResolved;
    private float stateTime;

    public BurntTileLayer(
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
        setBounds(gridX, gridY, gridWidth, gridHeight);
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        int rows = gameContext.getLevel().getRows();
        int cols = gameContext.getLevel().getColumns();
        if (rows <= 0 || cols <= 0) {
            return;
        }

        if (!clipResolved) {
            resolveClip();
            clipResolved = true;
        }

        float cellWidth = gridWidth / cols;
        float cellHeight = gridHeight / rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (!gameContext.isBurnedCell(row, col)) {
                    continue;
                }

                float x = gridX + col * cellWidth;
                float y = gridY + gridHeight - (row + 1) * cellHeight;

                if (tileClip != null) {
                    drawPamTile(batch, parentAlpha, x, y, cellWidth, cellHeight);
                } else {
                    drawFallbackTile(batch, parentAlpha, x, y, cellWidth, cellHeight);
                }
            }
        }

        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private void drawPamTile(
        Batch batch,
        float parentAlpha,
        float x,
        float y,
        float cellWidth,
        float cellHeight
    ) {
        Rectangle bounds = Textures.getPamPlayer().bounds(TILE_PAM, tileClip);
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
            drawFallbackTile(batch, parentAlpha, x, y, cellWidth, cellHeight);
            return;
        }

        float scaleX = cellWidth / bounds.width;
        float scaleY = cellHeight / bounds.height;
        float centerX = x + cellWidth / 2f;
        float centerY = y + cellHeight / 2f;

        batch.flush();
        Matrix4 original = batch.getTransformMatrix().cpy();
        Matrix4 scaled = original.cpy()
            .translate(centerX, centerY, 0f)
            .scale(scaleX, scaleY, 1f)
            .translate(-centerX, -centerY, 0f);
        batch.setTransformMatrix(scaled);
        batch.setColor(1f, 1f, 1f, parentAlpha);

        Textures.getPamPlayer().draw(
            batch,
            TILE_PAM,
            tileClip,
            stateTime,
            centerX,
            centerY,
            true
        );

        batch.flush();
        batch.setTransformMatrix(original);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private void drawFallbackTile(
        Batch batch,
        float parentAlpha,
        float x,
        float y,
        float cellWidth,
        float cellHeight
    ) {
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(fallbackTile(), x, y, cellWidth, cellHeight);
    }

    private void resolveClip() {
        List<String> clips = Textures.getPamPlayer().clips(TILE_PAM);
        if (clips == null || clips.isEmpty()) {
            Gdx.app.error(
                "BurntTileLayer",
                "No clips for " + TILE_PAM + ", using fallback texture"
            );
            return;
        }
        if (clips.contains("idle")) {
            tileClip = "idle";
        } else if (clips.contains("animation")) {
            tileClip = "animation";
        } else {
            tileClip = clips.get(0);
        }
        Gdx.app.log(
            "BurntTileLayer",
            TILE_PAM + " clip=" + tileClip + " available=" + clips
        );
    }

    private static Texture fallbackTile() {
        if (fallbackTile == null) {
            Pixmap pixmap = paintScorchedTile();
            fallbackTile = new Texture(pixmap);
            pixmap.dispose();
            fallbackTile.setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );
        }
        return fallbackTile;
    }

    private static Pixmap paintScorchedTile() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.18f, 0.20f, 0.28f, 1f);
        pixmap.fill();

        pixmap.setColor(0.12f, 0.13f, 0.18f, 1f);
        for (int y = 0; y < size; y += 16) {
            for (int x = 0; x < size; x += 16) {
                pixmap.drawRectangle(x, y, 16, 16);
            }
        }

        int[][] cracks = {
            {32, 8, 28, 22}, {28, 22, 18, 30}, {28, 22, 40, 34},
            {18, 30, 10, 44}, {18, 30, 24, 48}, {40, 34, 52, 28},
            {40, 34, 48, 50}, {32, 8, 42, 14}, {24, 48, 16, 58},
            {48, 50, 58, 56}, {10, 44, 4, 52}
        };
        for (int[] crack : cracks) {
            pixmap.setColor(0.95f, 0.22f, 0.04f, 1f);
            pixmap.drawLine(crack[0], crack[1], crack[2], crack[3]);
            pixmap.setColor(1f, 0.72f, 0.12f, 1f);
            pixmap.drawLine(
                crack[0],
                crack[1] + 1,
                crack[2],
                crack[3] + 1
            );
        }

        pixmap.setColor(1f, 0.85f, 0.25f, 1f);
        pixmap.fillCircle(32, 24, 2);
        pixmap.fillCircle(28, 22, 1);
        pixmap.fillCircle(40, 34, 1);
        return pixmap;
    }
}
