package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.ZombiePartFx;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.ArmorType;
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
            ArmorType popped;
            while ((popped = zombie.pollArmorPop()) != null) {
                gameContext.dropZombiePart(
                    zombie.getRow(),
                    zombie.getX(),
                    ZombiePartFx.Kind.ARMOR,
                    popped
                );
            }
            if (zombie.consumeArmDrop()) {
                gameContext.dropZombiePart(
                    zombie.getRow(),
                    zombie.getX(),
                    ZombiePartFx.Kind.ARM,
                    null,
                    zombie
                );
            }
            if (zombie.consumeHeadDrop()) {
                gameContext.dropZombiePart(
                    zombie.getRow(),
                    zombie.getX(),
                    ZombiePartFx.Kind.HEAD,
                    null,
                    zombie
                );
            }
        }
    }

    private void spawn(ZombiePartFx fx) {
        float x = gridX + (float) fx.x * (gridWidth / gameContext.getLevel().getColumns());
        float y = rowCenterY(fx.row);
        float cell = gridHeight / gameContext.getLevel().getRows();

        switch (fx.kind) {
            case ARMOR -> {
                addActor(new BurstActor(x, y + cell * 0.35f, cell * 0.9f, ARMOR_BREAK_PAM, 0.55f));
                TextureRegion armor = ZombieArmorLooks.region(
                    fx.armorType != null ? fx.armorType : ArmorType.CONE,
                    2
                );
                float armorH = armorHeight(fx.armorType, cell);
                addActor(new FallingSpriteActor(
                    armor,
                    x - cell * 0.18f,
                    y + cell * 0.95f,
                    armorH * 0.92f,
                    armorH,
                    220f,
                    18f
                ));
            }
            case ARM -> {
                FallingArmActor arm = createFallingArm(fx, x, y, cell);
                if (arm != null) {
                    addActor(arm);
                } else {
                    addActor(new FallingSpriteActor(
                        ZombieLimbLooks.arm(),
                        x - cell * 0.16f,
                        y + cell * 0.28f,
                        cell * 0.32f,
                        cell * 0.50f,
                        160f,
                        24f
                    ));
                }
            }
            case HEAD -> {
                FallingHeadActor head = createFallingHead(fx, x, y, cell);
                if (head != null) {
                    addActor(head);
                } else {
                    addActor(new FallingSpriteActor(
                        ZombieLimbLooks.head(),
                        x + 8f,
                        y + cell * 0.95f,
                        cell * 0.72f,
                        cell * 0.78f,
                        190f,
                        8f
                    ));
                }
            }
        }
    }

    private FallingHeadActor createFallingHead(ZombiePartFx fx, float x, float y, float cell) {
        if (fx.zombie == null) {
            return null;
        }
        String seasonName = gameContext.getSeason() != null
            ? gameContext.getSeason().getName()
            : null;
        ZombieAnimationSpec spec =
            ZombieAnimationResolver.shared().resolve(fx.zombie, seasonName);
        if (spec == null) {
            return null;
        }
        String clip = spec.getIdleClip();
        if (clip == null) {
            return null;
        }
        String pamPath = spec.getPamPath();
        Rectangle bounds = pamPlayer.bounds(pamPath, clip);
        if (bounds == null || bounds.height <= 0f) {
            return null;
        }
        float scale = (cell * 1.6f) / bounds.height;
        float feetY = y + 10f;
        float headX = x + cell * 0.12f;
        float headY = feetY + cell * 1.05f;
        return new FallingHeadActor(pamPath, clip, scale, headX, headY, cell);
    }

    private FallingArmActor createFallingArm(ZombiePartFx fx, float x, float y, float cell) {
        if (fx.zombie == null) {
            return null;
        }
        String seasonName = gameContext.getSeason() != null
            ? gameContext.getSeason().getName()
            : null;
        ZombieAnimationSpec spec =
            ZombieAnimationResolver.shared().resolve(fx.zombie, seasonName);
        if (spec == null) {
            return null;
        }
        String clip = spec.getIdleClip();
        if (clip == null) {
            return null;
        }
        String pamPath = spec.getPamPath();
        List<String> parts = ZombieArmVisibility.outerArmRoots(pamPlayer, pamPath);
        if (parts.isEmpty()) {
            return null;
        }
        Rectangle bounds = pamPlayer.bounds(pamPath, clip);
        if (bounds == null || bounds.height <= 0f) {
            return null;
        }
        float scale = (cell * 1.6f) / bounds.height;
        float feetX = x;
        float feetY = y + 10f;
        return new FallingArmActor(pamPath, clip, scale, parts, feetX, feetY, cell);
    }

    private static float armorHeight(ArmorType type, float cell) {
        if (type == ArmorType.BUCKET || type == ArmorType.BRICK) {
            return cell * 0.38f;
        }
        if (type == ArmorType.NEWSPAPER) {
            return cell * 0.40f;
        }
        if (type == ArmorType.SHOULDER_ARMOR) {
            return cell * 0.30f;
        }
        return cell * 0.46f;
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

    private final class FallingSpriteActor extends Actor {
        private final TextureRegion region;
        private float x;
        private float y;
        private float vx;
        private float vy;
        private float rotation;
        private final float spin;
        private final float width;
        private final float height;
        private float stateTime;
        private float groundedTime;
        private boolean grounded;
        private final float floorY;

        FallingSpriteActor(
            TextureRegion region,
            float x,
            float y,
            float width,
            float height,
            float popSpeed,
            float spin
        ) {
            this.region = region;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.floorY = y - 36f;
            this.vx = MathUtils.random(50f, 130f) * (MathUtils.randomBoolean() ? 1f : -1f);
            this.vy = popSpeed;
            this.spin = spin * (MathUtils.randomBoolean() ? 1f : -1f);
            this.rotation = MathUtils.random(-20f, 20f);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (!grounded) {
                rotation += spin * delta * 60f;
                x += vx * delta;
                y += vy * delta;
                vy -= 820f * delta;
                vx *= 0.985f;
                if (vy < 0f && y <= floorY) {
                    grounded = true;
                    y = floorY;
                    vy = 0f;
                    vx *= 0.25f;
                }
                if (stateTime > 1.25f) {
                    grounded = true;
                }
            } else {
                groundedTime += delta;
                if (groundedTime >= 1.6f) {
                    remove();
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (region == null) {
                return;
            }
            float alpha = parentAlpha;
            if (grounded) {
                alpha *= MathUtils.clamp(1f - groundedTime / 1.6f, 0f, 1f);
            }
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(
                region,
                x,
                y,
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                rotation
            );
            batch.setColor(1f, 1f, 1f, 1f);
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

    private final class FallingHeadActor extends Actor {
        private final String pamPath;
        private final String clip;
        private final float pamScale;
        private final float cell;
        private final float boxW;
        private final float boxH;
        private float headX;
        private float headY;
        private float vx;
        private float vy;
        private float rotation;
        private final float spin;
        private float stateTime;
        private float groundedTime;
        private boolean grounded;
        private final float floorY;

        FallingHeadActor(
            String pamPath,
            String clip,
            float pamScale,
            float headX,
            float headY,
            float cell
        ) {
            this.pamPath = pamPath;
            this.clip = clip;
            this.pamScale = pamScale;
            this.cell = cell;
            this.headX = headX;
            this.headY = headY;
            this.boxW = cell * 1.7f;
            this.boxH = cell * 1.35f;
            this.floorY = headY - cell * 0.85f;
            this.vx = MathUtils.random(45f, 110f) * (MathUtils.randomBoolean() ? 1f : -1f);
            this.vy = MathUtils.random(160f, 210f);
            this.spin = MathUtils.random(16f, 28f) * (MathUtils.randomBoolean() ? 1f : -1f);
            this.rotation = MathUtils.random(-8f, 8f);
            setBounds(headX - boxW / 2f, headY - boxH / 2f, boxW, boxH);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (!grounded) {
                rotation += spin * delta;
                rotation = MathUtils.clamp(rotation, -22f, 22f);
                headX += vx * delta;
                headY += vy * delta;
                vy -= 780f * delta;
                vx *= 0.987f;
                if (vy < 0f && headY <= floorY) {
                    grounded = true;
                    headY = floorY;
                    vy = 0f;
                    vx *= 0.22f;
                }
                if (stateTime > 1.35f) {
                    grounded = true;
                }
            } else {
                groundedTime += delta;
                if (groundedTime >= 1.7f) {
                    remove();
                }
            }
            setPosition(headX - boxW / 2f, headY - boxH / 2f);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float alpha = parentAlpha;
            if (grounded) {
                alpha *= MathUtils.clamp(1f - groundedTime / 1.7f, 0f, 1f);
            }

            batch.flush();
            if (!clipBegin()) {
                return;
            }

            float cx = headX;
            float cy = headY;
            float feetX = cx - cell * 0.12f;
            float feetY = cy - cell * 1.05f;

            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 transform = original.cpy()
                .translate(cx, cy, 0f)
                .rotate(0f, 0f, 1f, rotation)
                .translate(-cx, -cy, 0f)
                .translate(feetX, feetY, 0f)
                .scale(pamScale, pamScale, 1f)
                .translate(-feetX, -feetY, 0f);
            batch.setTransformMatrix(transform);
            batch.setColor(1f, 1f, 1f, alpha);
            try {
                pamPlayer.draw(batch, pamPath, clip, 0.15f, feetX, feetY, false);
            } catch (Throwable ignored) {
            }
            batch.flush();
            batch.setTransformMatrix(original);
            clipEnd();
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private final class FallingArmActor extends Actor {
        private final String pamPath;
        private final String clip;
        private final float pamScale;
        private final List<String> parts;
        private final float cell;
        private float originX;
        private float originY;
        private float vx;
        private float vy;
        private float rotation;
        private final float spin;
        private float stateTime;
        private float groundedTime;
        private boolean grounded;
        private final float floorY;

        FallingArmActor(
            String pamPath,
            String clip,
            float pamScale,
            List<String> parts,
            float feetX,
            float feetY,
            float cell
        ) {
            this.pamPath = pamPath;
            this.clip = clip;
            this.pamScale = pamScale;
            this.parts = parts;
            this.cell = cell;
            this.originX = feetX;
            this.originY = feetY;
            this.floorY = feetY - cell * 0.45f;
            this.vx = MathUtils.random(35f, 90f) * (MathUtils.randomBoolean() ? 1f : -1f);
            this.vy = MathUtils.random(140f, 190f);
            this.spin = MathUtils.random(50f, 90f) * (MathUtils.randomBoolean() ? 1f : -1f);
            this.rotation = MathUtils.random(-12f, 12f);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (!grounded) {
                rotation += spin * delta;
                originX += vx * delta;
                originY += vy * delta;
                vy -= 800f * delta;
                vx *= 0.987f;
                if (vy < 0f && originY <= floorY) {
                    grounded = true;
                    originY = floorY;
                    vy = 0f;
                    vx *= 0.22f;
                }
                if (stateTime > 1.35f) {
                    grounded = true;
                }
            } else {
                groundedTime += delta;
                if (groundedTime >= 1.7f) {
                    remove();
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float alpha = parentAlpha;
            if (grounded) {
                alpha *= MathUtils.clamp(1f - groundedTime / 1.7f, 0f, 1f);
            }

            float pivotX = originX - cell * 0.18f;
            float pivotY = originY + cell * 0.48f;

            batch.flush();
            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 transform = original.cpy()
                .translate(pivotX, pivotY, 0f)
                .rotate(0f, 0f, 1f, rotation)
                .translate(-pivotX, -pivotY, 0f)
                .translate(originX, originY, 0f)
                .scale(pamScale, pamScale, 1f)
                .translate(-originX, -originY, 0f);
            batch.setTransformMatrix(transform);
            batch.setColor(1f, 1f, 1f, alpha);
            try {
                for (String part : parts) {
                    pamPlayer.drawPart(batch, pamPath, clip, 0.12f, originX, originY, part);
                }
            } catch (Throwable ignored) {
            }
            batch.flush();
            batch.setTransformMatrix(original);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

}
