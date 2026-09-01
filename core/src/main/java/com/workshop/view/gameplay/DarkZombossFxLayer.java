package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
 * گلوله‌های آتشین در حال پرواز و آتش دو سطر زامباس اژدها.
 */
public final class DarkZombossFxLayer extends Group {

    private static final String[] FIREBALL_PAMS = {
        "768/INITIAL/EFFECTS/T_MISSILE_TOE_PROJECTILE/T_MISSILE_TOE_PROJECTILE.PAM",
        "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM",
        "768/INITIAL/EFFECTS/ZOMBOSS_TELEPORTATION_BALL/ZOMBOSS_TELEPORTATION_BALL.PAM"
    };
    private static final String[] BREATH_PAMS = {
        "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM",
        "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_DARK/ZOMBOSS_MISSILE_EXPLOSION_DARK.PAM"
    };
    private static final String[] FALLBACK_SPRITES = {
        "IMAGE_EFFECTS_T_MISSILE_TOE_PROJECTILE_T_MISSILE_TOE_PROJECTILE_64X64",
        "IMAGE_PLANT_PEASHOOTER_PEASHOOTER_23X23"
    };

    private static String fireballPam;
    private static String fireballClip;
    private static String breathPam;
    private static String breathClip;
    private static String fallbackSpriteId;
    private static boolean lookupDone;

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public DarkZombossFxLayer(
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
        GameContext.DarkFireballSpawn fireball;
        while ((fireball = gameContext.pollDarkFireball()) != null) {
            addActor(new FireballActor(fireball.row, fireball.col, fireball.flightSeconds));
        }
        GameContext.DarkFireBreathSpawn breath;
        while ((breath = gameContext.pollDarkFireBreath()) != null) {
            addActor(new BreathActor(breath.topRow, breath.bottomRow, breath.durationSeconds));
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

    private float bossMouthX() {
        for (Zombie zombie : gameContext.getAliveZombies()) {
            if (zombie != null && zombie.isBoss() && !zombie.isDead()) {
                return gridX + (float) zombie.getX() * getCellWidth();
            }
        }
        return gridX + gridWidth * 0.88f;
    }

    private float bossMouthY() {
        for (Zombie zombie : gameContext.getAliveZombies()) {
            if (zombie != null && zombie.isBoss() && !zombie.isDead()) {
                int top = (int) zombie.getY();
                return (getCellCenterY(top) + getCellCenterY(top + 1)) / 2f;
            }
        }
        return gridY + gridHeight / 2f;
    }

    private final class FireballActor extends Actor {
        private final float targetX;
        private final float targetY;
        private final float startX;
        private final float startY;
        private final float flightSeconds;
        private float stateTime;
        private float ballScale = 1f;
        private boolean resolved;

        FireballActor(int row, int col, float flightSeconds) {
            this.targetX = getCellCenterX(col);
            this.targetY = getCellCenterY(row);
            this.startX = bossMouthX();
            this.startY = bossMouthY();
            this.flightSeconds = Math.max(0.35f, flightSeconds);
            float size = getCellHeight() * 2.4f;
            setBounds(startX - size / 2f, startY - size / 2f, size, size);
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
                ensureAssets();
                ballScale = scaleToCells(fireballPam, fireballClip, 1.6f, 1.6f);
                resolved = true;
            }
            float t = Math.min(1f, stateTime / flightSeconds);
            float x = startX + (targetX - startX) * t;
            float y = startY + (targetY - startY) * t + getCellHeight() * 1.4f * 4f * t * (1f - t);

            int src = batch.getBlendSrcFunc();
            int dst = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            if (!drawFireballPam(batch, parentAlpha, x, y)) {
                drawFallback(batch, parentAlpha, x, y);
            }
            batch.flush();
            batch.setBlendFunction(src, dst);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private boolean drawFireballPam(Batch batch, float parentAlpha, float x, float y) {
            if (fireballPam == null || fireballClip == null) {
                return false;
            }
            drawPam(batch, fireballPam, fireballClip, stateTime, x, y, ballScale, 1f, 0.45f, 0.12f, parentAlpha);
            return true;
        }

        private void drawFallback(Batch batch, float parentAlpha, float x, float y) {
            if (fallbackSpriteId == null) {
                return;
            }
            TextureRegion region = Textures.regionOrNull(fallbackSpriteId);
            if (region == null) {
                return;
            }
            float width = getCellWidth() * 1.4f;
            float height = getCellHeight() * 1.4f;
            batch.setColor(1f, 0.4f, 0.08f, parentAlpha);
            batch.draw(region, x - width / 2f, y - height / 2f, width, height);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }
    }

    private final class BreathActor extends Actor {
        private final int topRow;
        private final int bottomRow;
        private final float lifetime;
        private float stateTime;
        private float breathScaleX = 1f;
        private float breathScaleY = 1f;
        private boolean resolved;

        BreathActor(int topRow, int bottomRow, float durationSeconds) {
            this.topRow = topRow;
            this.bottomRow = bottomRow;
            this.lifetime = Math.max(0.6f, durationSeconds);
            setBounds(gridX, gridY, gridWidth, gridHeight);
            setTouchable(Touchable.disabled);
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
                ensureAssets();
                breathScaleX = scaleToWidth(breathPam, breathClip, gameContext.getLevel().getColumns() * 0.95f);
                breathScaleY = scaleToHeight(breathPam, breathClip, 1.55f);
                resolved = true;
            }
            if (breathPam == null || breathClip == null) {
                return;
            }
            float appear = Math.min(1f, stateTime / 0.22f);
            float fade = stateTime < lifetime * 0.7f
                ? 1f
                : Math.max(0f, 1f - (stateTime - lifetime * 0.7f) / (lifetime * 0.3f));
            float alpha = parentAlpha * appear * fade;
            float sweep = Math.min(1f, stateTime / 0.55f);
            float width = gridWidth * (0.35f + 0.65f * sweep);
            float centerX = bossMouthX() - width / 2f;

            int src = batch.getBlendSrcFunc();
            int dst = batch.getBlendDstFunc();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            for (int row = topRow; row <= bottomRow; row++) {
                drawPam(
                    batch,
                    breathPam,
                    breathClip,
                    stateTime,
                    centerX,
                    getCellCenterY(row),
                    breathScaleX * (0.55f + 0.45f * sweep),
                    breathScaleY,
                    1f,
                    0.4f,
                    0.08f,
                    alpha
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
            float scaleX,
            float scaleY,
            float red,
            float green,
            float blue,
            float alpha
        ) {
            batch.flush();
            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(x, y, 0f)
                .scale(scaleX, scaleY, 1f)
                .translate(-x, -y, 0f);
            batch.setTransformMatrix(scaled);
            batch.setColor(red, green, blue, alpha);
            try {
                pamPlayer.draw(batch, pamPath, clip, time, x, y, true);
            } catch (RuntimeException ignored) {
            }
            batch.flush();
            batch.setTransformMatrix(original);
        }
    }

    private void drawPam(
        Batch batch,
        String pamPath,
        String clip,
        float time,
        float x,
        float y,
        float scale,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        batch.flush();
        Matrix4 original = batch.getTransformMatrix().cpy();
        Matrix4 scaled = original.cpy()
            .translate(x, y, 0f)
            .scale(scale, scale, 1f)
            .translate(-x, -y, 0f);
        batch.setTransformMatrix(scaled);
        batch.setColor(red, green, blue, alpha);
        try {
            pamPlayer.draw(batch, pamPath, clip, time, x, y, true);
        } catch (RuntimeException ignored) {
        }
        batch.flush();
        batch.setTransformMatrix(original);
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

    private float scaleToWidth(String pamPath, String clip, float widthCells) {
        if (pamPath == null || clip == null) {
            return 1f;
        }
        try {
            Rectangle bounds = pamPlayer.bounds(pamPath, clip);
            if (bounds == null || bounds.width <= 0f) {
                return 1f;
            }
            return (getCellWidth() * widthCells) / bounds.width;
        } catch (RuntimeException ignored) {
            return 1f;
        }
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

    private static synchronized void ensureAssets() {
        if (lookupDone) {
            return;
        }
        lookupDone = true;
        PamPlayer player = Textures.getPamPlayer();
        fireballPam = bindPam(player, FIREBALL_PAMS);
        fireballClip = clipOf(player, fireballPam);
        breathPam = bindPam(player, BREATH_PAMS);
        breathClip = clipOf(player, breathPam);
        for (String imageId : FALLBACK_SPRITES) {
            if (Textures.regionOrNull(imageId) != null) {
                fallbackSpriteId = imageId;
                break;
            }
        }
        if (fireballPam == null && breathPam == null) {
            Gdx.app.error("DarkZombossFxLayer", "No fire PAM found");
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
            for (String preferred : new String[]{"animation", "idle", "attack", "loop"}) {
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
