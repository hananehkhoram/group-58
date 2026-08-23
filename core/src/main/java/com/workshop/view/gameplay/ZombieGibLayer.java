package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.ZombiePartFx;
import com.workshop.model.zombie.Zombie;
import pvz.libpvz.pam.PamPlayer;

import java.util.List;

public final class ZombieGibLayer extends Group {

    private static final String ARMOR_BREAK_PAM =
        "768/FULL/EFFECTS/ARMOR_BREAK_EFFECT/ARMOR_BREAK_EFFECT.PAM";

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public ZombieGibLayer(
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
        collectFromZombies();
        ZombiePartFx fx;
        while ((fx = gameContext.pollZombiePart()) != null) {
            spawn(fx);
        }
        super.act(delta);
    }

    private void collectFromZombies() {
        for (Zombie zombie : gameContext.getAliveZombies()) {
            if (zombie == null) {
                continue;
            }
            if (zombie.consumeArmorPop()) {
                gameContext.dropZombiePart(zombie.getRow(), zombie.getX(), ZombiePartFx.Kind.ARMOR);
            }
            if (zombie.consumeArmDrop()) {
                gameContext.dropZombiePart(zombie.getRow(), zombie.getX(), ZombiePartFx.Kind.ARM);
            }
            if (zombie.consumeHeadDrop()) {
                gameContext.dropZombiePart(zombie.getRow(), zombie.getX(), ZombiePartFx.Kind.HEAD);
            }
        }
    }

    private void spawn(ZombiePartFx fx) {
        float x = gridX + (float) fx.x * (gridWidth / gameContext.getLevel().getColumns());
        float y = rowCenterY(fx.row);
        float cell = gridHeight / gameContext.getLevel().getRows();

        switch (fx.kind) {
            case ARMOR -> {
                addActor(new BurstActor(x, y + cell * 0.35f, cell * 1.1f, ARMOR_BREAK_PAM, 0.7f));
                addActor(new FallingPartActor(x, y + cell * 0.45f, cell * 0.55f, ARMOR_BREAK_PAM, 0.55f));
            }
            case ARM -> addActor(new FallingPartActor(x - 8f, y + cell * 0.15f, cell * 0.45f, ARMOR_BREAK_PAM, 0.4f));
            case HEAD -> addActor(new FallingPartActor(x + 6f, y + cell * 0.5f, cell * 0.5f, ARMOR_BREAK_PAM, 0.5f));
        }
    }

    private float rowCenterY(int row) {
        float cellHeight = gridHeight / gameContext.getLevel().getRows();
        return gridY + gridHeight - row * cellHeight - cellHeight / 2f + cellHeight * 0.16f;
    }

    private final class BurstActor extends Actor {
        private final float centerX;
        private final float centerY;
        private final float targetHeight;
        private final String pamPath;
        private final float lifetime;
        private String clip;
        private boolean resolved;
        private float scale = 1f;
        private float stateTime;

        BurstActor(float centerX, float centerY, float targetHeight, String pamPath, float lifetime) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.targetHeight = targetHeight;
            this.pamPath = pamPath;
            this.lifetime = lifetime;
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
            if (!resolveClip()) {
                return;
            }
            drawPam(batch, parentAlpha, centerX, centerY, scale, pamPath, clip, stateTime, false);
        }

        private boolean resolveClip() {
            if (resolved) {
                return clip != null;
            }
            resolved = true;
            clip = pickClip(pamPath);
            Rectangle bounds = clip == null ? null : pamPlayer.bounds(pamPath, clip);
            if (bounds != null && bounds.height > 0f) {
                scale = targetHeight / bounds.height;
            }
            return clip != null;
        }
    }

    private final class FallingPartActor extends Actor {
        private float x;
        private float y;
        private float vx;
        private float vy;
        private final float targetHeight;
        private final String pamPath;
        private String clip;
        private boolean resolved;
        private float scale = 1f;
        private float stateTime;
        private float groundedTime;
        private boolean grounded;
        private final float floorY;

        FallingPartActor(float x, float y, float targetHeight, String pamPath, float sizeScale) {
            this.x = x;
            this.y = y;
            this.floorY = y - 28f;
            this.targetHeight = targetHeight * sizeScale;
            this.pamPath = pamPath;
            this.vx = MathUtils.random(40f, 110f) * (MathUtils.randomBoolean() ? 1f : -1f);
            this.vy = MathUtils.random(140f, 220f);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (!grounded) {
                x += vx * delta;
                y += vy * delta;
                vy -= 780f * delta;
                vx *= 0.98f;
                if (vy < 0f && y <= floorY) {
                    grounded = true;
                    vy = 0f;
                    vx *= 0.3f;
                }
                if (stateTime > 1.1f) {
                    grounded = true;
                }
            } else {
                groundedTime += delta;
                if (groundedTime >= 1.4f) {
                    remove();
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!resolved) {
                clip = pickClip(pamPath);
                Rectangle bounds = clip == null ? null : pamPlayer.bounds(pamPath, clip);
                if (bounds != null && bounds.height > 0f) {
                    scale = targetHeight / bounds.height;
                }
                resolved = true;
            }
            if (clip == null) {
                return;
            }
            float alpha = parentAlpha;
            if (grounded) {
                alpha *= MathUtils.clamp(1f - groundedTime / 1.4f, 0f, 1f);
            }
            drawPam(batch, alpha, x, y, scale, pamPath, clip, Math.min(stateTime, 0.35f), false);
        }
    }

    private String pickClip(String pamPath) {
        List<String> clips = pamPlayer.clips(pamPath);
        if (clips == null || clips.isEmpty()) {
            Gdx.app.error("ZombieGibLayer", "No clips for " + pamPath);
            return null;
        }
        if (clips.contains("animation")) {
            return "animation";
        }
        if (clips.contains("idle")) {
            return "idle";
        }
        return clips.get(0);
    }

    private void drawPam(
        Batch batch,
        float alpha,
        float x,
        float y,
        float scale,
        String pamPath,
        String clip,
        float time,
        boolean loop
    ) {
        batch.flush();
        Matrix4 original = batch.getTransformMatrix().cpy();
        Matrix4 scaled = original.cpy()
            .translate(x, y, 0f)
            .scale(scale, scale, 1f)
            .translate(-x, -y, 0f);
        batch.setTransformMatrix(scaled);
        batch.setColor(1f, 1f, 1f, alpha);
        pamPlayer.draw(batch, pamPath, clip, time, x, y, loop);
        batch.flush();
        batch.setTransformMatrix(original);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
