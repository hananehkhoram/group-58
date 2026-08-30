package com.workshop.view.gameplay;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.ProjectileHitFx;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

public final class ProjectileHitFxLayer extends Group {

    private static final String[] PAM_CANDIDATES = {
        "768/INITIAL/EFFECTS/PEA_SPLATS/PEA_SPLATS.PAM",
        "768/FULL/EFFECTS/PEA_SPLATS/PEA_SPLATS.PAM",
        "768/INITIAL/EFFECTS/PEASPLATS/PEASPLATS.PAM",
        "768/INITIAL/EFFECTS/T_PEA_SPLATS/T_PEA_SPLATS.PAM",
        "768/INITIAL/EFFECTS/PEA_SPLAT/PEA_SPLAT.PAM",
        "768/INITIAL/EFFECTS/SPLATS/SPLATS.PAM",
        "768/INITIAL/EFFECTS/GENERIC_EXPLOSION_FRONT/GENERIC_EXPLOSION_FRONT.PAM"
    };

    private static final String[] SPRITE_CANDIDATES = {
        "IMAGE_EFFECTS_PEA_SPLATS",
        "IMAGE_PLANT_PEASHOOTER_PEASHOOTER_23X23"
    };

    private static final float HEIGHT_TO_CELL_RATIO = 0.55f;
    private static final float FALLBACK_LIFETIME = 0.28f;
    private static final float Y_LIFT_TO_CELL_RATIO = 0.18f;

    private static String resolvedPamPath;
    private static String resolvedClip;
    private static String resolvedSpriteId;
    private static boolean lookupDone;

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public ProjectileHitFxLayer(
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
    }

    @Override
    public void act(float delta) {
        ProjectileHitFx fx;
        while ((fx = gameContext.pollProjectileHit()) != null) {
            addActor(new HitActor(
                getHitX(fx.x),
                getHitY(fx.row)
            ));
        }
        super.act(delta);
    }

    private float getHitX(double column) {
        return gridX + (float) column * getCellWidth();
    }

    private float getHitY(int row) {
        return gridY
            + gridHeight
            - row * getCellHeight()
            - getCellHeight() / 2f
            + getCellHeight() * Y_LIFT_TO_CELL_RATIO;
    }

    private float getCellWidth() {
        return gridWidth / gameContext.getLevel().getColumns();
    }

    private float getCellHeight() {
        return gridHeight / gameContext.getLevel().getRows();
    }

    private final class HitActor extends Actor {
        private final float centerX;
        private final float centerY;
        private float scale = 1f;
        private float lifetime = FALLBACK_LIFETIME;
        private float stateTime;
        private boolean resolved;

        HitActor(float centerX, float centerY) {
            this.centerX = centerX;
            this.centerY = centerY;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= lifetime) {
                remove();
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!resolved) {
                resolveVisual();
                resolved = true;
            }

            float fade = Math.max(0f, 1f - stateTime / lifetime);
            batch.setColor(1f, 1f, 1f, parentAlpha * fade);

            if (resolvedPamPath != null && resolvedClip != null) {
                drawPam(batch);
            } else if (resolvedSpriteId != null) {
                drawSprite(batch);
            }

            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void drawPam(Batch batch) {
            batch.flush();
            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(centerX, centerY, 0f)
                .scale(scale, scale, 1f)
                .translate(-centerX, -centerY, 0f);
            batch.setTransformMatrix(scaled);
            try {
                pamPlayer.draw(
                    batch,
                    resolvedPamPath,
                    resolvedClip,
                    stateTime,
                    centerX,
                    centerY,
                    false
                );
            } catch (Throwable ignored) {
            }
            batch.flush();
            batch.setTransformMatrix(original);
        }

        private void drawSprite(Batch batch) {
            TextureRegion region = Textures.regionOrNull(resolvedSpriteId);
            if (region == null) {
                return;
            }

            float pop = 0.75f + 0.55f * Math.min(1f, stateTime / 0.12f);
            float size = getCellHeight() * HEIGHT_TO_CELL_RATIO * pop;
            batch.draw(
                region,
                centerX - size / 2f,
                centerY - size / 2f,
                size,
                size
            );
        }

        private void resolveVisual() {
            ensureHitAsset();
            if (resolvedPamPath == null || resolvedClip == null) {
                return;
            }

            try {
                float duration = pamPlayer.clipDurationSeconds(
                    resolvedPamPath,
                    resolvedClip
                );
                if (duration > 0.05f) {
                    lifetime = Math.min(0.45f, duration);
                }
                Rectangle bounds = pamPlayer.bounds(resolvedPamPath, resolvedClip);
                if (bounds != null && bounds.height > 0f) {
                    scale = (getCellHeight() * HEIGHT_TO_CELL_RATIO) / bounds.height;
                }
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static synchronized void ensureHitAsset() {
        if (lookupDone) {
            return;
        }
        lookupDone = true;

        for (String path : PAM_CANDIDATES) {
            if (bindPam(path)) {
                return;
            }
        }

        for (String imageId : SPRITE_CANDIDATES) {
            if (Textures.regionOrNull(imageId) != null) {
                resolvedSpriteId = imageId;
                return;
            }
        }
    }

    private static boolean bindPam(String path) {
        if (path == null || !pamExists(path)) {
            return false;
        }

        try {
            List<String> clips = Textures.getPamPlayer().clips(path);
            if (clips == null || clips.isEmpty()) {
                return false;
            }

            String clip = pickClip(clips);
            if (clip == null) {
                return false;
            }

            resolvedPamPath = path;
            resolvedClip = clip;
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static String pickClip(List<String> clips) {
        for (String preferred : new String[]{"animation", "idle", "splat", "hit"}) {
            for (String clip : clips) {
                if (clip != null && preferred.equalsIgnoreCase(clip.trim())) {
                    return clip;
                }
            }
        }
        return clips.get(0);
    }

    private static boolean pamExists(String relativeToImages) {
        FileHandle file = Textures.assetsRoot()
            .child("IMAGES")
            .child(relativeToImages.replace('\\', '/'));
        return file.exists() && !file.isDirectory();
    }
}
