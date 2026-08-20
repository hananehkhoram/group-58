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
 * خانه‌های لغزنده/یخیِ غارهای یخی (FrozenCaveChapter.getSliderNextRow):
 * روی هر خانه‌ای که به سمتِ بالا یا پایین لیز می‌خوره، تصویرِ متناظرِ
 * TILESLIDER_ICEAGE_UP/DOWN کشیده می‌شه. چون این خانه‌ها در طولِ مرحله
 * ثابت هستن (فقط در onLevelStart تعیین می‌شن)، یک‌بار در سازنده ساخته
 * می‌شن.
 */
public final class SlipperyTileLayer extends Group {

    private static final String SLIDE_UP_PAM =
        "768/FULL/EFFECTS/TILESLIDER_ICEAGE_UP/TILESLIDER_ICEAGE_UP.PAM";
    private static final String SLIDE_DOWN_PAM =
        "768/FULL/EFFECTS/TILESLIDER_ICEAGE_DOWN/TILESLIDER_ICEAGE_DOWN.PAM";
    private static final String SLIDE_CLIP = "idle";

    private final GameContext gameContext;
    private final boolean active;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public SlipperyTileLayer(
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

        // فعلاً فقط غارهای یخی خانه‌ی لغزنده دارن؛ برای بقیه‌ی فصل‌ها این
        // لایه چیزی رسم نمی‌کنه.
        this.active = "FrozenCave".equals(
            gameContext.getSeason().getName()
        );

        if (!active) {
            return;
        }

        buildSlipperyTiles();
    }

    private void buildSlipperyTiles() {
        int rows = gameContext.getLevel().getRows();
        int cols = gameContext.getLevel().getColumns();

        float cellWidth = gridWidth / cols;
        float cellHeight = gridHeight / rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int slideTo =
                    gameContext.getSeason().getSliderNextRow(row, col);

                String pamPath;
                if (slideTo < row) {
                    pamPath = SLIDE_UP_PAM;
                } else if (slideTo > row) {
                    pamPath = SLIDE_DOWN_PAM;
                } else {
                    continue;
                }

                float x = gridX + col * cellWidth;
                float y = gridY + gridHeight - (row + 1) * cellHeight;

                SlipperyTileActor tile = new SlipperyTileActor(pamPath);
                tile.setBounds(x, y, cellWidth, cellHeight);
                addActor(tile);
            }
        }
    }

    /**
     * یک PAM رو با تغییرِ موقتِ ماتریسِ تبدیلِ batch، دقیقاً روی مستطیلِ
     * یک خانه (getX/getY/getWidth/getHeight) می‌کشه؛ همون تکنیکِ
     * WaterLayer.PamStretchActor، چون این نسخه از libPVZ پارامترِ scale
     * توی draw() نداره.
     */
    private static final class SlipperyTileActor extends Actor {

        private final String pamPath;

        private String resolvedClip;
        private boolean resolved;
        private float stateTime;

        SlipperyTileActor(String pamPath) {
            this.pamPath = pamPath;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (getWidth() <= 0 || getHeight() <= 0) {
                return;
            }

            if (!resolved) {
                resolveClip();
                resolved = true;
            }

            if (resolvedClip == null) {
                return;
            }

            Rectangle bounds =
                Textures.getPamPlayer().bounds(pamPath, resolvedClip);

            if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
                return;
            }

            float scaleX = getWidth() / bounds.width;
            float scaleY = getHeight() / bounds.height;

            float centerX = getX() + getWidth() / 2f;
            float centerY = getY() + getHeight() / 2f;

            batch.flush();

            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(centerX, centerY, 0f)
                .scale(scaleX, scaleY, 1f)
                .translate(-centerX, -centerY, 0f);

            batch.setTransformMatrix(scaled);

            // اگه اکتورِ قبلی رنگِ batch رو نیمه‌شفاف گذاشته باشه، اینجا
            // صریحاً به حالتِ عادی برمی‌گردونیمش.
            batch.setColor(1f, 1f, 1f, parentAlpha);

            Textures.getPamPlayer().draw(
                batch,
                pamPath,
                resolvedClip,
                stateTime,
                centerX,
                centerY,
                true
            );

            batch.flush();
            batch.setTransformMatrix(original);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void resolveClip() {
            List<String> clips = Textures.getPamPlayer().clips(pamPath);

            if (clips == null || clips.isEmpty()) {
                Gdx.app.error(
                    "SlipperyTileLayer",
                    "No clips found for PAM: " + pamPath
                );
                return;
            }

            if (clips.contains(SLIDE_CLIP)) {
                resolvedClip = SLIDE_CLIP;
            } else {
                Gdx.app.log(
                    "SlipperyTileLayer",
                    "Clip \"" + SLIDE_CLIP + "\" not found in " + pamPath
                        + ", falling back to \"" + clips.get(0)
                        + "\". Available: " + clips
                );
                resolvedClip = clips.get(0);
            }
        }
    }
}
