package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.zombie.Zombie;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

/**
 * کوسه‌های کوچک زامباس بیچ و نوار باد توربین؛ جدا از انفجارهای معمولی
 * تا scale/clip اشتباه BurstActor آن‌ها را نامرئی نکند.
 */
public final class BeachZombossFxLayer extends Group {

    private static final String SHARK_PAM =
        "768/FULL/EFFECTS/ZOMBOSS_SHARK_PROJECTILE/ZOMBOSS_SHARK_PROJECTILE.PAM";
    private static final String WIND_PAM =
        "768/FULL/EFFECTS/ZOMBOSS_TURBINE_WIND/ZOMBOSS_TURBINE_WIND.PAM";

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;
    private final boolean drawWind;
    private final boolean spawnSharks;
    private float windTime;

    public BeachZombossFxLayer(
        GameContext gameContext,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this(gameContext, gridX, gridY, gridWidth, gridHeight, true, true);
    }

    public BeachZombossFxLayer(
        GameContext gameContext,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight,
        boolean drawWind,
        boolean spawnSharks
    ) {
        this.gameContext = gameContext;
        this.pamPlayer = Textures.getPamPlayer();
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.drawWind = drawWind;
        this.spawnSharks = spawnSharks;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        if (spawnSharks) {
            GameContext.BeachSharkSpawn spawn;
            while ((spawn = gameContext.pollBeachShark()) != null) {
                addActor(new SharkActor(spawn.row, spawn.col));
            }
        }
        windTime += delta;
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (drawWind && gameContext.hasBeachVortex()) {
            int top = gameContext.getBeachVortexTopRow();
            int bottom = gameContext.getBeachVortexBottomRow();
            for (int row = top; row <= bottom; row++) {
                drawWindRow(batch, parentAlpha, row);
            }
        }
        super.draw(batch, parentAlpha);
    }

    private void drawWindRow(Batch batch, float parentAlpha, int row) {
        String clip = pickClip(WIND_PAM, "animation", "idle", "loop");
        if (clip == null) {
            return;
        }
        Rectangle bounds;
        try {
            bounds = pamPlayer.bounds(WIND_PAM, clip);
        } catch (RuntimeException exception) {
            Gdx.app.error("BeachZombossFxLayer", "bounds failed for " + WIND_PAM, exception);
            return;
        }
        if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            return;
        }

        float cellWidth = getCellWidth();
        float cellHeight = getCellHeight();
        float left = gridX;
        float right = windRightX(cellWidth);
        float width = Math.max(cellWidth * 2f, right - left);
        float centerX = left + width / 2f;
        float centerY = getCellCenterY(row);
        float scaleX = width / bounds.width;
        float scaleY = cellHeight / bounds.height;

        batch.flush();
        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        Matrix4 original = batch.getTransformMatrix().cpy();
        Matrix4 scaled = original.cpy()
            .translate(centerX, centerY, 0f)
            .scale(scaleX, scaleY, 1f)
            .translate(-centerX, -centerY, 0f);
        batch.setTransformMatrix(scaled);
        batch.setColor(1f, 1f, 1f, parentAlpha);
        try {
            pamPlayer.draw(batch, WIND_PAM, clip, windTime, centerX, centerY, true);
        } catch (RuntimeException exception) {
            Gdx.app.error("BeachZombossFxLayer", "draw failed for " + WIND_PAM, exception);
        }
        batch.flush();
        batch.setTransformMatrix(original);
        batch.setBlendFunction(src, dst);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private float windRightX(float cellWidth) {
        for (Zombie zombie : gameContext.getAliveZombies()) {
            if (zombie != null && zombie.isBoss() && !zombie.isDead()) {
                return gridX + (float) zombie.getX() * cellWidth - cellWidth * 0.45f;
            }
        }
        return gridX + gridWidth * 0.7f;
    }

    private float getCellWidth() {
        return gridWidth / gameContext.getLevel().getColumns();
    }

    private float getCellHeight() {
        return gridHeight / gameContext.getLevel().getRows();
    }

    private float getCellCenterX(int column) {
        return gridX + column * getCellWidth() + getCellWidth() / 2f;
    }

    private float getCellCenterY(int row) {
        return gridY + gridHeight - row * getCellHeight() - getCellHeight() / 2f;
    }

    private String pickClip(String pamPath, String... preferred) {
        try {
            List<String> clips = pamPlayer.clips(pamPath);
            if (clips == null || clips.isEmpty()) {
                return null;
            }
            for (String want : preferred) {
                for (String clip : clips) {
                    if (clip != null && clip.equalsIgnoreCase(want)) {
                        return clip;
                    }
                }
            }
            for (String want : preferred) {
                String needle = want.toLowerCase();
                for (String clip : clips) {
                    if (clip != null && clip.toLowerCase().contains(needle)) {
                        return clip;
                    }
                }
            }
            return clips.get(0);
        } catch (RuntimeException exception) {
            Gdx.app.error("BeachZombossFxLayer", "clips failed for " + pamPath, exception);
            return null;
        }
    }

    private void drawPam(
        Batch batch,
        float parentAlpha,
        String pamPath,
        String clip,
        float time,
        boolean loop,
        float centerX,
        float centerY,
        float targetWidth,
        float targetHeight
    ) {
        if (clip == null) {
            return;
        }
        Rectangle bounds;
        try {
            bounds = pamPlayer.bounds(pamPath, clip);
        } catch (RuntimeException ignored) {
            return;
        }
        if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
            return;
        }

        float scale = Math.min(targetWidth / bounds.width, targetHeight / bounds.height);

        batch.flush();
        Matrix4 original = batch.getTransformMatrix().cpy();
        Matrix4 scaled = original.cpy()
            .translate(centerX, centerY, 0f)
            .scale(scale, scale, 1f)
            .translate(-centerX, -centerY, 0f);
        batch.setTransformMatrix(scaled);
        batch.setColor(1f, 1f, 1f, parentAlpha);
        try {
            pamPlayer.draw(batch, pamPath, clip, time, centerX, centerY, loop);
        } catch (RuntimeException ignored) {
        }
        batch.flush();
        batch.setTransformMatrix(original);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private final class SharkActor extends Actor {
        private final int row;
        private final int col;
        private float stateTime;
        private String walkClip;
        private String attackClip;
        private boolean clipsResolved;

        SharkActor(int row, int col) {
            this.row = row;
            this.col = col;
            float cellWidth = getCellWidth();
            float cellHeight = getCellHeight();
            setBounds(
                getCellCenterX(col) - cellWidth,
                getCellCenterY(row) - cellHeight,
                cellWidth * 2f,
                cellHeight * 2f
            );
            setTouchable(Touchable.disabled);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= 1.45f) {
                remove();
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!clipsResolved) {
                walkClip = pickClip(SHARK_PAM, "walk", "idle2", "idle");
                attackClip = pickClip(SHARK_PAM, "attack", "walk", "idle");
                clipsResolved = true;
            }
            String clip = stateTime < 0.45f ? walkClip : attackClip;
            float emerge = Math.min(1f, stateTime / 0.35f);
            float centerX = getCellCenterX(col);
            float centerY = getCellCenterY(row) - getCellHeight() * 0.55f * (1f - emerge);
            drawPam(
                batch,
                parentAlpha,
                SHARK_PAM,
                clip,
                stateTime,
                false,
                centerX,
                centerY,
                getCellWidth() * 1.7f,
                getCellHeight() * 1.55f
            );
        }
    }
}
