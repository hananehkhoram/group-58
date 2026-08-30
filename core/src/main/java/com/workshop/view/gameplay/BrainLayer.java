package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import pvz.libpvz.pam.PamPlayer;

import java.util.List;

public final class BrainLayer extends Group {

    /*
     * این مقدار را با مسیر واقعی PAM مغز خودت جایگزین کن.
     *
     * مثال ساختار مسیر:
     * 768/.../.../SOMETHING.PAM
     *
     * اسم واقعی را حدس نزن.
     */
    private static final String BRAIN_PAM =
        "768/FULL/ZOMBIE/POWER_BRAIN_PROJECTILE/POWER_BRAIN_PROJECTILE.PAM";

    private static final float TARGET_HEIGHT_TO_CELL_RATIO = 0.55f;

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final BrainActor[] brainActors;

    public BrainLayer(
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

        int rows = gameContext.getLevel().getRows();

        brainActors = new BrainActor[rows];

        createBrains();
    }

    private void createBrains() {
        for (int row = 0; row < brainActors.length; row++) {

            BrainActor brain =
                new BrainActor(
                    BRAIN_PAM,
                    getCellHeight()
                );

            brain.setPosition(
                getBrainX(),
                getBrainY(row)
            );

            brainActors[row] = brain;

            addActor(brain);
        }
    }

    @Override
    public void act(float delta) {
        syncBrains();

        super.act(delta);
    }

    private void syncBrains() {
        if (!(gameContext.getLevelManager()
            instanceof IZombieManager manager)) {

            setVisible(false);
            return;
        }

        setVisible(true);

        for (int row = 0; row < brainActors.length; row++) {

            BrainActor brain =
                brainActors[row];

            if (brain == null) {
                continue;
            }

            brain.setVisible(
                !manager.isBrainEaten(row)
            );
        }
    }

    private float getCellWidth() {
        return gridWidth
            / gameContext.getLevel().getColumns();
    }

    private float getCellHeight() {
        return gridHeight
            / gameContext.getLevel().getRows();
    }

    private float getBrainX() {
        return gridX
            - getCellWidth() * 0.45f;
    }

    private float getBrainY(int row) {
        float cellHeight = getCellHeight();

        return gridY
            + gridHeight
            - row * cellHeight
            - cellHeight / 2f;
    }

    private final class BrainActor extends Actor {

        private final String pamPath;
        private final float cellHeight;

        private String animationClip;

        private float stateTime;

        private Float resolvedScale;

        BrainActor(
            String pamPath,
            float cellHeight
        ) {
            this.pamPath = pamPath;
            this.cellHeight = cellHeight;

            resolveAnimationClip();
        }

        private void resolveAnimationClip() {
            try {
                pamPlayer.loadSync(pamPath);

                List<String> clips =
                    pamPlayer.clips(pamPath);

                if (clips == null || clips.isEmpty()) {
                    throw new IllegalStateException(
                        "Brain PAM has no animation clips: "
                            + pamPath
                    );
                }

                /*
                 * به جای حدس زدن اسم clip:
                 * اگر idle وجود داشته باشد از آن استفاده می‌کنیم.
                 * در غیر این صورت اگر فقط یک clip باشد همان واقعی را می‌گیریم.
                 * اگر چند clip باشد خطا می‌دهیم تا خودت clip درست را مشخص کنی.
                 */
                if (clips.contains("idle")) {
                    animationClip = "idle";
                    return;
                }

                if (clips.size() == 1) {
                    animationClip = clips.get(0);
                    return;
                }

                throw new IllegalStateException(
                    "Brain PAM has multiple clips. "
                        + "Choose the correct one explicitly. Clips: "
                        + clips
                );

            } catch (Exception e) {
                throw new IllegalStateException(
                    "Could not load brain animation from: "
                        + pamPath,
                    e
                );
            }
        }

        private float getScale() {
            if (resolvedScale != null) {
                return resolvedScale;
            }

            Rectangle bounds =
                pamPlayer.bounds(
                    pamPath,
                    animationClip
                );

            if (bounds == null
                || bounds.height <= 0f) {

                resolvedScale = 1f;
                return resolvedScale;
            }

            resolvedScale =
                cellHeight
                    * TARGET_HEIGHT_TO_CELL_RATIO
                    / bounds.height;

            return resolvedScale;
        }

        @Override
        public void act(float delta) {
            super.act(delta);

            stateTime += delta;
        }

        @Override
        public void draw(
            Batch batch,
            float parentAlpha
        ) {
            if (animationClip == null) {
                return;
            }

            float scale = getScale();

            Matrix4 oldTransform =
                batch.getTransformMatrix().cpy();

            Matrix4 transform =
                new Matrix4(oldTransform);

            transform.translate(
                getX(),
                getY(),
                0f
            );

            transform.scale(
                scale,
                scale,
                1f
            );

            transform.translate(
                -getX(),
                -getY(),
                0f
            );

            batch.setTransformMatrix(
                transform
            );

            batch.setColor(
                1f,
                1f,
                1f,
                parentAlpha
            );

            try {
                pamPlayer.draw(
                    batch,
                    pamPath,
                    animationClip,
                    stateTime,
                    getX(),
                    getY(),
                    true
                );
            } finally {
                batch.setTransformMatrix(
                    oldTransform
                );
            }
        }
    }
}
