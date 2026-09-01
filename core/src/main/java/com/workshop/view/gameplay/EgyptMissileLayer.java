package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

public final class EgyptMissileLayer extends Group {

    private static final String[] MISSILE_PAMS = {
        "768/INITIAL/EFFECTS/T_MISSILE_TOE_PROJECTILE/T_MISSILE_TOE_PROJECTILE.PAM",
        "768/INITIAL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_EGYPT/ZOMBOSS_MISSILE_EXPLOSION_EGYPT.PAM",
        "768/INITIAL/EFFECTS/ZOMBOSS_TELEPORTATION_BALL/ZOMBOSS_TELEPORTATION_BALL.PAM"
    };
    private static final String[] RETICLE_PAMS = {
        "768/INITIAL/EFFECTS/MISSILE_TOE_RETICLE/MISSILE_TOE_RETICLE.PAM"
    };
    private static final String[] FALLBACK_SPRITES = {
        "IMAGE_EFFECTS_T_MISSILE_TOE_PROJECTILE_T_MISSILE_TOE_PROJECTILE_64X64",
        "IMAGE_PLANT_PEASHOOTER_PEASHOOTER_23X23"
    };

    private static final float MISSILE_WIDTH_CELLS = 1.8f;
    private static final float MISSILE_HEIGHT_CELLS = 3.2f;
    private static final float RETICLE_CELLS = 1.35f;

    private static String missilePamPath;
    private static String missileClip;
    private static String reticlePamPath;
    private static String reticleClip;
    private static String fallbackSpriteId;
    private static boolean lookupDone;

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public EgyptMissileLayer(
        GameContext gameContext,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;
        this.pamPlayer = Textures.getPamPlayer();
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        setTouchable(Touchable.disabled);
    }

    @Override
    public void act(float delta) {
        GameContext.EgyptMissileSpawn spawn;
        while ((spawn = gameContext.pollEgyptMissile()) != null) {
            addActor(new MissileActor(spawn.row, spawn.col, spawn.flightSeconds));
        }
        super.act(delta);
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

    private final class MissileActor extends Actor {
        private final float targetX;
        private final float targetY;
        private final float startY;
        private final float flightSeconds;
        private float stateTime;
        private float missileScale = 1f;
        private float reticleScale = 1f;
        private boolean resolved;

        MissileActor(int row, int col, float flightSeconds) {
            this.targetX = getCellCenterX(col);
            this.targetY = getCellCenterY(row);
            this.startY = gridY + gridHeight + getCellHeight() * 2.4f;
            this.flightSeconds = Math.max(0.35f, flightSeconds);
            float size = getCellHeight() * MISSILE_HEIGHT_CELLS;
            setBounds(targetX - size / 2f, startY - size / 2f, size, size * 2f);
            setTouchable(Touchable.disabled);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= flightSeconds) {
                remove();
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!resolved) {
                resolveVisual();
                resolved = true;
            }

            float t = Math.min(1f, stateTime / flightSeconds);
            float y = startY + (targetY - startY) * t * t;

            drawReticle(batch, parentAlpha);
            if (!drawMissilePam(batch, parentAlpha, y)) {
                drawFallbackSprite(batch, parentAlpha, y);
            }
        }

        private void drawReticle(Batch batch, float parentAlpha) {
            if (reticlePamPath == null || reticleClip == null) {
                return;
            }
            float pulse = 0.85f + 0.2f * (float) Math.sin(stateTime * 14f);
            drawPam(
                batch,
                parentAlpha,
                reticlePamPath,
                reticleClip,
                stateTime,
                targetX,
                targetY,
                reticleScale * pulse
            );
        }

        private boolean drawMissilePam(Batch batch, float parentAlpha, float y) {
            if (missilePamPath == null || missileClip == null) {
                return false;
            }
            drawPam(
                batch,
                parentAlpha,
                missilePamPath,
                missileClip,
                stateTime,
                targetX,
                y,
                missileScale
            );
            return true;
        }

        private void drawFallbackSprite(Batch batch, float parentAlpha, float y) {
            if (fallbackSpriteId == null) {
                return;
            }
            TextureRegion region = Textures.regionOrNull(fallbackSpriteId);
            if (region == null) {
                return;
            }
            float width = getCellWidth() * MISSILE_WIDTH_CELLS;
            float height = getCellHeight() * MISSILE_HEIGHT_CELLS;
            batch.setColor(1f, 0.55f, 0.15f, parentAlpha);
            batch.draw(region, targetX - width / 2f, y - height / 2f, width, height);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void drawPam(
            Batch batch,
            float parentAlpha,
            String pamPath,
            String clip,
            float time,
            float x,
            float y,
            float scale
        ) {
            batch.flush();
            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(x, y, 0f)
                .scale(scale, scale, 1f)
                .translate(-x, -y, 0f);
            batch.setTransformMatrix(scaled);
            batch.setColor(1f, 1f, 1f, parentAlpha);
            try {
                pamPlayer.draw(batch, pamPath, clip, time, x, y, true);
            } catch (RuntimeException ignored) {
            }
            batch.flush();
            batch.setTransformMatrix(original);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void resolveVisual() {
            ensureMissileAsset();
            missileScale = scaleToCells(missilePamPath, missileClip, MISSILE_WIDTH_CELLS, MISSILE_HEIGHT_CELLS);
            reticleScale = scaleToCells(reticlePamPath, reticleClip, RETICLE_CELLS, RETICLE_CELLS);
        }

        private float scaleToCells(String pamPath, String clip, float widthCells, float heightCells) {
            if (pamPath == null || clip == null) {
                return 1f;
            }
            try {
                Rectangle bounds = pamPlayer.bounds(pamPath, clip);
                if (bounds == null || bounds.width <= 0f || bounds.height <= 0f) {
                    return 1f;
                }
                float scaleX = (getCellWidth() * widthCells) / bounds.width;
                float scaleY = (getCellHeight() * heightCells) / bounds.height;
                return Math.min(scaleX, scaleY);
            } catch (RuntimeException ignored) {
                return 1f;
            }
        }
    }

    private static synchronized void ensureMissileAsset() {
        if (lookupDone) {
            return;
        }
        lookupDone = true;

        PamPlayer player = Textures.getPamPlayer();
        missilePamPath = bindPam(player, MISSILE_PAMS);
        missileClip = missilePamPath == null ? null : pickClip(player, missilePamPath);
        reticlePamPath = bindPam(player, RETICLE_PAMS);
        reticleClip = reticlePamPath == null ? null : pickClip(player, reticlePamPath);

        for (String imageId : FALLBACK_SPRITES) {
            if (Textures.regionOrNull(imageId) != null) {
                fallbackSpriteId = imageId;
                break;
            }
        }
    }

    private static String bindPam(PamPlayer player, String[] paths) {
        for (String path : paths) {
            if (!pamExists(path)) {
                continue;
            }
            try {
                List<String> clips = player.clips(path);
                if (clips != null && !clips.isEmpty()) {
                    return path;
                }
            } catch (RuntimeException exception) {
                Gdx.app.error("EgyptMissileLayer", "Could not load " + path, exception);
            }
        }
        return null;
    }

    private static String pickClip(PamPlayer player, String path) {
        try {
            List<String> clips = player.clips(path);
            if (clips == null || clips.isEmpty()) {
                return null;
            }
            for (String preferred : new String[]{"idle", "animation", "loop", "attack"}) {
                for (String clip : clips) {
                    if (clip != null && preferred.equalsIgnoreCase(clip.trim())) {
                        return clip;
                    }
                }
            }
            return clips.get(0);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static boolean pamExists(String relativeToImages) {
        FileHandle file = Textures.assetsRoot()
            .child("IMAGES")
            .child(relativeToImages.replace('\\', '/'));
        return file.exists() && !file.isDirectory();
    }
}
