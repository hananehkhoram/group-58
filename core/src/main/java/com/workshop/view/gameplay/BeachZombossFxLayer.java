package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.PendingFxQueue;
import com.workshop.model.zombie.Zombie;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

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
        setTransform(false);
        setBounds(0f, 0f, 5000f, 4000f);
        setTouchable(Touchable.disabled);
    }

    public static void drawOnBoss(
        Batch batch,
        float parentAlpha,
        float x,
        float y,
        float size,
        float time
    ) {
        TextureRegion region = swirlRegion();
        if (region == null || size <= 1f) {
            return;
        }

        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        float origin = size / 2f;
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(
            region,
            x - origin,
            y - origin,
            origin,
            origin,
            size,
            size,
            1f,
            1f,
            -time * 220f
        );
        batch.draw(
            region,
            x - origin * 0.68f,
            y - origin * 0.68f,
            origin * 0.68f,
            origin * 0.68f,
            size * 0.68f,
            size * 0.68f,
            1f,
            1f,
            time * 310f
        );

        PamPlayer player = Textures.getPamPlayer();
        String clip = null;
        try {
            List<String> clips = player.clips(WIND_PAM);
            if (clips != null && !clips.isEmpty()) {
                clip = clips.contains("animation") ? "animation" : clips.get(0);
            }
        } catch (RuntimeException ignored) {
        }
        if (clip != null) {
            try {
                Rectangle bounds = player.bounds(WIND_PAM, clip);
                if (bounds != null && bounds.width > 0f && bounds.height > 0f) {
                    float scale = size / Math.max(bounds.width, bounds.height);
                    batch.flush();
                    Matrix4 original = batch.getTransformMatrix().cpy();
                    Matrix4 scaled = original.cpy()
                        .translate(x, y, 0f)
                        .scale(scale, scale, 1f)
                        .translate(-x, -y, 0f);
                    batch.setTransformMatrix(scaled);
                    player.draw(batch, WIND_PAM, clip, time, x, y, true);
                    batch.flush();
                    batch.setTransformMatrix(original);
                }
            } catch (RuntimeException ignored) {
            }
        }

        batch.flush();
        batch.setBlendFunction(src, dst);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    @Override
    public void act(float delta) {
        if (spawnSharks) {
            PendingFxQueue.BeachSharkSpawn spawn;
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
            drawMouthVortex(batch, parentAlpha);
        }
        super.draw(batch, parentAlpha);
    }

    private void drawMouthVortex(Batch batch, float parentAlpha) {
        int top = gameContext.getBeachVortexTopRow();
        int bottom = gameContext.getBeachVortexBottomRow();
        float cellHeight = getCellHeight();
        float mouthX = windMouthX(getCellWidth());
        float pulse = 0.75f + 0.25f * (float) Math.sin(windTime * 9f);

        int src = batch.getBlendSrcFunc();
        int dst = batch.getBlendDstFunc();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for (int row = top; row <= bottom; row++) {
            drawSuctionBand(batch, parentAlpha * pulse, row, mouthX, cellHeight);
        }

        batch.flush();
        batch.setBlendFunction(src, dst);
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private void drawSuctionBand(
        Batch batch,
        float parentAlpha,
        int row,
        float mouthX,
        float cellHeight
    ) {
        TextureRegion region = swirlRegion();
        if (region == null) {
            return;
        }
        float height = cellHeight * 0.55f;
        float width = Math.max(cellHeight, mouthX - gridX);
        batch.setColor(1f, 1f, 1f, parentAlpha * 0.28f);
        batch.draw(
            region,
            gridX,
            getCellCenterY(row) - height / 2f,
            width,
            height
        );
        batch.setColor(1f, 1f, 1f, parentAlpha);
    }

    private float windMouthX(float cellWidth) {
        for (Zombie zombie : gameContext.getAliveZombies()) {
            if (zombie != null && zombie.isBoss() && !zombie.isDead()) {
                return gridX + (float) zombie.getX() * cellWidth - cellWidth * 0.85f;
            }
        }
        return gridX + gridWidth * 0.72f;
    }

    private static Texture swirlTexture;
    private static TextureRegion swirlRegion;

    private static TextureRegion swirlRegion() {
        if (swirlRegion != null) {
            return swirlRegion;
        }
        int size = 128;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        int center = size / 2;
        for (int angle = 0; angle < 780; angle++) {
            double rad = Math.toRadians(angle);
            double radius = 10 + angle / 780.0 * 48;
            int x = center + (int) Math.round(Math.cos(rad) * radius);
            int y = center + (int) Math.round(Math.sin(rad) * radius);
            float alpha = Math.max(0.15f, 0.95f - angle / 780f * 0.8f);
            pixmap.setColor(1f, 1f, 1f, alpha);
            pixmap.fillCircle(x, y, 3);
        }
        swirlTexture = new Texture(pixmap);
        pixmap.dispose();
        swirlTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        swirlRegion = new TextureRegion(swirlTexture);
        return swirlRegion;
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

        private static final float SHARK_LIFETIME = 1.85f;

        SharkActor(int row, int col) {
            this.row = row;
            this.col = col;
            float cellWidth = getCellWidth();
            float cellHeight = getCellHeight();
            setBounds(
                getCellCenterX(col) - cellWidth * 1.6f,
                getCellCenterY(row) - cellHeight * 1.6f,
                cellWidth * 3.2f,
                cellHeight * 3.2f
            );
            setTouchable(Touchable.disabled);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= SHARK_LIFETIME) {
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
            String clip = stateTime < 0.55f ? walkClip : attackClip;
            float emerge = Math.min(1f, stateTime / 0.4f);
            float centerX = getCellCenterX(col);
            float centerY = getCellCenterY(row) - getCellHeight() * 0.7f * (1f - emerge);
            drawPam(
                batch,
                parentAlpha,
                SHARK_PAM,
                clip,
                stateTime,
                false,
                centerX,
                centerY,
                getCellWidth() * 2.5f,
                getCellHeight() * 2.2f
            );
        }
    }
}
