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
 * جلوهٔ ظاهر شدن زامبی یخی: نور آبی، بلوک یخ در حال رشد، سایهٔ زامبی غار یخی.
 */
public final class IceSummonFxLayer extends Group {

    private static final String[] GLOW_PAMS = {
        "768/INITIAL/EFFECTS/ZOMBOSS_TELEPORTATION_BALL/ZOMBOSS_TELEPORTATION_BALL.PAM",
        "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM",
        "768/FULL/EFFECTS/ZOMBOSS_TRANSFORM_EFFECT/ZOMBOSS_TRANSFORM_EFFECT.PAM"
    };
    private static final String[] ICE_BLOCK_PAMS = {
        "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM",
        "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM"
    };
    private static final String[] SILHOUETTE_PAMS = {
        "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_BASIC/ZOMBIE_ICEAGE_BASIC.PAM",
        "768/INITIAL/ZOMBIE/ZOMBIE_ICEAGE_BASIC/ZOMBIE_ICEAGE_BASIC.PAM",
        "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM"
    };
    private static final float LIFETIME = 1.25f;

    private static String glowPam;
    private static String glowClip;
    private static String iceBlockPam;
    private static String iceBlockClip;
    private static String silhouettePam;
    private static String silhouetteClip;
    private static boolean lookupDone;

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public IceSummonFxLayer(
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
        PendingFxQueue.IceSummonSpawn spawn;
        while ((spawn = gameContext.pollIceSummon()) != null) {
            addActor(new SummonActor(spawn.row, spawn.col));
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

    private final class SummonActor extends Actor {
        private final float centerX;
        private final float centerY;
        private float stateTime;
        private float glowScale = 1f;
        private float iceScale = 1f;
        private float bodyScale = 1f;
        private boolean resolved;

        SummonActor(int row, int col) {
            this.centerX = getCellCenterX(col);
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
            float appear = Math.min(1f, stateTime / 0.32f);
            float fade = t < 0.72f ? 1f : Math.max(0f, 1f - (t - 0.72f) / 0.28f);
            float alpha = parentAlpha * appear * fade;
            float pop = 0.35f + 0.75f * appear;

            int src = batch.getBlendSrcFunc();
            int dst = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

            if (glowPam != null && glowClip != null) {
                batch.setColor(0.4f, 0.85f, 1f, alpha);
                drawPam(batch, glowPam, glowClip, stateTime, centerX, centerY, glowScale * (0.8f + 0.4f * appear));
            }

            batch.flush();
            batch.setBlendFunction(src, dst);

            if (silhouettePam != null && silhouetteClip != null) {
                batch.setColor(0.75f, 0.95f, 1f, alpha * 0.9f);
                drawPam(
                    batch,
                    silhouettePam,
                    silhouetteClip,
                    Math.min(stateTime, 0.4f),
                    centerX,
                    centerY,
                    bodyScale * pop
                );
            }

            if (iceBlockPam != null && iceBlockClip != null) {
                batch.setColor(0.85f, 0.97f, 1f, alpha);
                drawPam(
                    batch,
                    iceBlockPam,
                    iceBlockClip,
                    stateTime,
                    centerX,
                    centerY,
                    iceScale * pop
                );
            }

            batch.flush();
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
            glowScale = scaleToHeight(glowPam, glowClip, 2.4f);
            iceScale = scaleToHeight(iceBlockPam, iceBlockClip, 1.85f);
            bodyScale = scaleToHeight(silhouettePam, silhouetteClip, 1.7f);
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
        iceBlockPam = bindPam(player, ICE_BLOCK_PAMS);
        iceBlockClip = clipOf(player, iceBlockPam);
        silhouettePam = bindPam(player, SILHOUETTE_PAMS);
        silhouetteClip = clipOf(player, silhouettePam);
        if (glowPam == null && iceBlockPam == null && silhouettePam == null) {
            Gdx.app.error("IceSummonFxLayer", "No ice summon PAM found");
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
