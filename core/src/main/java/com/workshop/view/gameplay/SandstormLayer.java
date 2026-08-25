package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;

import java.util.List;

/**
 * موج آخر Ancient Egypt زامبی‌ها را با شن‌باد می‌آورد.
 * این لایه PAMهای SANDSTORM_REAR / SANDSTORM_TOP را روی کل زمین پخش می‌کند.
 */
public final class SandstormLayer extends Group {

    private static final String REAR_PAM =
        "768/INITIAL/EFFECTS/SANDSTORM_REAR/SANDSTORM_REAR.PAM";
    private static final String TOP_PAM =
        "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";

    private static final float STORM_LIFETIME = 5.5f;

    private final GameContext gameContext;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public SandstormLayer(
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
    }

    @Override
    public void act(float delta) {
        if (gameContext.pollSandstorm()) {
            spawnStorm();
        }
        super.act(delta);
    }

    private void spawnStorm() {
        int rows = gameContext.getLevel().getRows();
        float cellHeight = gridHeight / rows;

        addActor(new StormActor(
            REAR_PAM,
            gridX,
            gridY + gridHeight / 2f,
            gridWidth,
            gridHeight
        ));

        for (int row = 0; row < rows; row++) {
            float rowCenterY = gridY
                + gridHeight
                - row * cellHeight
                - cellHeight / 2f;

            addActor(new StormActor(
                TOP_PAM,
                gridX,
                rowCenterY,
                gridWidth,
                cellHeight * 1.7f
            ));
        }
    }

    private static final class StormActor extends Actor {

        private final String pamPath;
        private final float areaX;
        private final float areaCenterY;
        private final float areaWidth;
        private final float areaHeight;

        private String resolvedClip;
        private boolean resolved;
        private float scaleX = 1f;
        private float scaleY = 1f;
        private float stateTime;

        StormActor(
            String pamPath,
            float areaX,
            float areaCenterY,
            float areaWidth,
            float areaHeight
        ) {
            this.pamPath = pamPath;
            this.areaX = areaX;
            this.areaCenterY = areaCenterY;
            this.areaWidth = areaWidth;
            this.areaHeight = areaHeight;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= STORM_LIFETIME) {
                remove();
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!resolved) {
                resolveClipAndScale();
                resolved = true;
            }

            if (resolvedClip == null) {
                return;
            }

            float centerX = areaX + areaWidth / 2f;

            batch.flush();

            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(centerX, areaCenterY, 0f)
                .scale(scaleX, scaleY, 1f)
                .translate(-centerX, -areaCenterY, 0f);

            batch.setTransformMatrix(scaled);
            batch.setColor(1f, 1f, 1f, parentAlpha);

            Textures.getPamPlayer().draw(
                batch,
                pamPath,
                resolvedClip,
                stateTime,
                centerX,
                areaCenterY,
                true
            );

            batch.flush();
            batch.setTransformMatrix(original);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void resolveClipAndScale() {
            List<String> clips = Textures.getPamPlayer().clips(pamPath);

            if (clips != null && clips.contains("animation")) {
                resolvedClip = "animation";
            } else if (clips != null && clips.contains("idle")) {
                resolvedClip = "idle";
            } else if (clips != null && !clips.isEmpty()) {
                resolvedClip = clips.get(0);
            } else {
                Gdx.app.error("SandstormLayer", "No clips found for PAM: " + pamPath);
                return;
            }

            Rectangle bounds =
                Textures.getPamPlayer().bounds(pamPath, resolvedClip);

            if (bounds != null && bounds.width > 0 && bounds.height > 0) {
                scaleX = areaWidth / bounds.width;
                scaleY = areaHeight / bounds.height;
            }
        }
    }
}
