package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.PendingFxQueue;

import java.util.List;

import pvz.libpvz.pam.PamPlayer;

/**
 * جلوهٔ واضح ظاهر شدن زامبی جلوی زامباس مصر: نور آبی و سایهٔ سفید زامبی.
 */
public final class EgyptSummonFxLayer extends Group {

    private static final String[] GLOW_PAMS = {
        "768/INITIAL/EFFECTS/ZOMBOSS_TELEPORTATION_BALL/ZOMBOSS_TELEPORTATION_BALL.PAM",
        "768/INITIAL/EFFECTS/ZOMBOSS_TELEPORT_BALL_EXIT/ZOMBOSS_TELEPORT_BALL_EXIT.PAM",
        "768/FULL/EFFECTS/ZOMBOSS_TRANSFORM_EFFECT/ZOMBOSS_TRANSFORM_EFFECT.PAM"
    };
    private static final String[] SILHOUETTE_PAMS = {
        "768/FULL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM",
        "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM",
        "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM"
    };
    private static final float LIFETIME = 1.25f;

    private static String glowPam;
    private static String glowClip;
    private static String silhouettePam;
    private static String silhouetteClip;
    private static boolean lookupDone;

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public EgyptSummonFxLayer(
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
        PendingFxQueue.EgyptSummonSpawn spawn;
        while ((spawn = gameContext.pollEgyptSummon()) != null) {
            addActor(new SummonActor(spawn.row, spawn.x));
        }
        super.act(delta);
    }

    private float getCellWidth() {
        return gridWidth / gameContext.getLevel().getColumns();
    }

    private float getCellHeight() {
        return gridHeight / gameContext.getLevel().getRows();
    }

    private float worldX(double column) {
        return gridX + (float) column * getCellWidth();
    }

    private float getCellCenterY(int row) {
        return gridY + gridHeight - row * getCellHeight() - getCellHeight() / 2f;
    }

    private final class SummonActor extends Actor {
        private final float centerX;
        private final float centerY;
        private float stateTime;
        private float glowScale = 1f;
        private float bodyScale = 1f;
        private boolean resolved;

        SummonActor(int row, double column) {
            this.centerX = worldX(column);
            this.centerY = getCellCenterY(row);
            float size = getCellHeight() * 3.2f;
            setBounds(centerX - size / 2f, centerY - size / 2f, size, size);
            setTouchable(Touchable.disabled);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= LIFETIME) {
                remove();
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!resolved) {
                resolve();
                resolved = true;
            }

            float t = Math.min(1f, stateTime / LIFETIME);
            float appear = Math.min(1f, stateTime / 0.28f);
            float fade = t < 0.7f ? 1f : Math.max(0f, 1f - (t - 0.7f) / 0.3f);
            float alpha = parentAlpha * appear * fade;

            int src = batch.getBlendSrcFunc();
            int dst = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

            if (glowPam != null && glowClip != null) {
                batch.setColor(0.35f, 0.85f, 1f, alpha);
                drawPam(batch, glowPam, glowClip, stateTime, centerX, centerY, glowScale * (0.85f + 0.35f * appear));
            }

            if (silhouettePam != null && silhouetteClip != null) {
                float pop = 0.55f + 0.55f * appear;
                batch.setColor(0.85f, 0.95f, 1f, alpha);
                drawPam(
                    batch,
                    silhouettePam,
                    silhouetteClip,
                    Math.min(stateTime, 0.35f),
                    centerX,
                    centerY,
                    bodyScale * pop
                );
            }

            batch.flush();
            batch.setBlendFunction(src, dst);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void drawPam(
            Batch batch,
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
            try {
                pamPlayer.draw(batch, pamPath, clip, time, x, y, true);
            } catch (RuntimeException ignored) {
            }
            batch.flush();
            batch.setTransformMatrix(original);
        }

        private void resolve() {
            ensureAssets();
            glowScale = scaleToHeight(glowPam, glowClip, 2.6f);
            bodyScale = scaleToHeight(silhouettePam, silhouetteClip, 2.15f);
        }

        private float scaleToHeight(String pamPath, String clip, float heightCells) {
            if (pamPath == null || clip == null) {
                return 1f;
            }
            try {
                Rectangle bounds = pamPlayer.bounds(pamPath, clip);
                if (bounds == null || bounds.height <= 0f) {
                    return 1f;
                }
                return (getCellHeight() * heightCells) / bounds.height;
            } catch (RuntimeException ignored) {
                return 1f;
            }
        }
    }

    private static synchronized void ensureAssets() {
        if (lookupDone) {
            return;
        }
        lookupDone = true;
        PamPlayer player = Textures.getPamPlayer();
        glowPam = bindPam(player, GLOW_PAMS);
        glowClip = clipOf(player, glowPam);
        silhouettePam = bindPam(player, SILHOUETTE_PAMS);
        silhouetteClip = clipOf(player, silhouettePam);
        if (glowPam == null && silhouettePam == null) {
            Gdx.app.error("EgyptSummonFxLayer", "No summon PAM found");
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
            } catch (RuntimeException ignored) {
            }
        }
        return null;
    }

    private static String clipOf(PamPlayer player, String path) {
        if (path == null) {
            return null;
        }
        try {
            List<String> clips = player.clips(path);
            if (clips == null || clips.isEmpty()) {
                return null;
            }
            for (String preferred : new String[]{"idle", "animation", "walk", "loop"}) {
                for (String clip : clips) {
                    if (clip != null && preferred.equalsIgnoreCase(clip.trim())) {
                        return clip;
                    }
                }
            }
            return clips.get(0);
        } catch (RuntimeException ignored) {
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
