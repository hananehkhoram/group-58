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
 * وقتی «باد یخ» (FrozenCaveChapter.applyIcyWindToRow) روی یه سطر می‌وزه،
 * این لایه انیمیشن باد سرد (FROSTBITE_CHILL_WIND) رو موقتاً روی همون
 * سطر پخش می‌کنه.
 */
public final class ChillWindLayer extends Group {

    private static final String WIND_PAM =
        "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";

    private static final float GUST_LIFETIME = 2.2f;

    private final GameContext gameContext;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public ChillWindLayer(
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
        Integer row;

        while ((row = gameContext.pollWindRow()) != null) {
            spawnGust(row);
        }

        super.act(delta);
    }

    private void spawnGust(int row) {
        int rows = gameContext.getLevel().getRows();

        if (row < 0 || row >= rows) {
            return;
        }

        float cellHeight = gridHeight / rows;

        float rowCenterY = gridY
            + gridHeight
            - row * cellHeight
            - cellHeight / 2f;

        addActor(new WindGustActor(gridX, rowCenterY, gridWidth, cellHeight));
    }

    private static final class WindGustActor extends Actor {

        private final float rowX;
        private final float rowCenterY;
        private final float rowWidth;
        private final float rowHeight;

        private String resolvedClip;
        private boolean resolved;
        private float scaleX = 1f;
        private float scaleY = 1f;
        private float stateTime;

        WindGustActor(
            float rowX,
            float rowCenterY,
            float rowWidth,
            float rowHeight
        ) {
            this.rowX = rowX;
            this.rowCenterY = rowCenterY;
            this.rowWidth = rowWidth;
            this.rowHeight = rowHeight;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;

            if (stateTime >= GUST_LIFETIME) {
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

            float centerX = rowX + rowWidth / 2f;

            // این PAM دورِ یه بومِ کوچیک ساخته شده؛ برای اینکه کل عرضِ
            // سطر رو بگیره، مثل DialogueOverlay از تبدیلِ ماتریسِ batch
            // برای اسکیل (غیریکنواخت روی X/Y) استفاده می‌کنیم.
            batch.flush();

            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(centerX, rowCenterY, 0f)
                .scale(scaleX, scaleY, 1f)
                .translate(-centerX, -rowCenterY, 0f);

            batch.setTransformMatrix(scaled);

            // اگه اکتورِ قبلی رنگِ batch رو نیمه‌شفاف گذاشته باشه، اینجا
            // صریحاً به حالتِ عادی برمی‌گردونیمش.
            batch.setColor(1f, 1f, 1f, parentAlpha);

            Textures.getPamPlayer().draw(
                batch,
                WIND_PAM,
                resolvedClip,
                stateTime,
                centerX,
                rowCenterY,
                false
            );

            batch.flush();
            batch.setTransformMatrix(original);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void resolveClipAndScale() {
            List<String> clips = Textures.getPamPlayer().clips(WIND_PAM);

            String preferred = "animation";

            if (clips != null && clips.contains(preferred)) {
                resolvedClip = preferred;
            } else if (clips != null && !clips.isEmpty()) {
                Gdx.app.log(
                    "ChillWindLayer",
                    "Clip \"" + preferred + "\" not found in " + WIND_PAM
                        + ", falling back to \"" + clips.get(0) + "\". Available: " + clips
                );
                resolvedClip = clips.get(0);
            } else {
                Gdx.app.error(
                    "ChillWindLayer",
                    "No clips found for PAM: " + WIND_PAM
                );
                return;
            }

            Rectangle bounds =
                Textures.getPamPlayer().bounds(WIND_PAM, resolvedClip);

            if (bounds != null && bounds.width > 0 && bounds.height > 0) {
                scaleX = rowWidth / bounds.width;
                scaleY = rowHeight / bounds.height;
            }
        }
    }
}
