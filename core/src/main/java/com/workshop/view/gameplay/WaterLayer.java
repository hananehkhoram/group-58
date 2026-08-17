package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.GridCell;

import java.util.List;
import java.util.Set;

/**
 * دریا و اثرات مرتبطِ کشندِ ساحل (Big Wave Beach):
 * - پر شدنِ آب روی ستون‌هایی که ctx.getSeason().getWaterCells(ctx) پوشش می‌ده
 *   (هر شروعِ موج ممکنه یهو عوض بشه، طبق منطق فاز یک؛ نیازی به تغییر
 *   پیوسته نیست).
 * - خطی که حداکثر پیشروی دریا رو نشون می‌ده (بر اساس Level.getMaxWaterColumn،
 *   ثابته و در طول مرحله تغییر نمی‌کنه).
 * - خانه‌های «ساحل پست» (season.getLowTideCells) که با یه پوششِ رنگیِ
 *   نیمه‌شفاف مشخص می‌شن (SHALLOW_PUDDLE_TILE.PAM یه تایل‌ستِ اتوتایلِ
 *   چندقطعه‌ایه که با این کتابخونه به‌صورت یه کلیپِ ساده قابل‌پخش نیست،
 *   پس به‌جاش از یه رنگِ صاف استفاده می‌کنیم).
 */
public final class WaterLayer extends Group {

    private static final String SEA_PAM =
        "768/FULL/BACKGROUNDS/WATER_UNDERLAYER/WATER_UNDERLAYER.PAM";
    private static final String SEA_CLIP = "Water";

    private static final String TIDE_LINE_PAM =
        "768/FULL/BACKGROUNDS/WATER_TIDE_LINE/WATER_TIDE_LINE.PAM";
    private static final String TIDE_LINE_CLIP = "idle";

    private static final Color LOW_TIDE_COLOR =
        new Color(0.55f, 0.85f, 1f, 0.35f);

    private static final float TIDE_LINE_WIDTH = 14f;

    private static Texture whitePixel;

    private final GameContext gameContext;
    private final boolean active;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final PamStretchActor seaFill;
    private final PamStretchActor tideLine;

    public WaterLayer(
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

        // maxWaterColumn پیش‌فرضش ۵ هست حتی برای فصل‌های دیگه، پس این
        // لایه رو صراحتاً فقط برای Big Wave Beach فعال می‌کنیم.
        this.active = "Big Wave Beach".equals(
            gameContext.getSeason().getName()
        );

        seaFill = new PamStretchActor(SEA_PAM, SEA_CLIP, true);
        tideLine = new PamStretchActor(TIDE_LINE_PAM, TIDE_LINE_CLIP, true);

        if (!active) {
            return;
        }

        addActor(seaFill);
        buildLowTidePuddles();
        addActor(tideLine);
        layoutTideLine();
    }

    @Override
    public void act(float delta) {
        if (active) {
            layoutSeaFill();
        }

        super.act(delta);
    }

    private float cellWidth() {
        return gridWidth / gameContext.getLevel().getColumns();
    }

    private float cellHeight() {
        return gridHeight / gameContext.getLevel().getRows();
    }

    private void layoutSeaFill() {
        Set<GridCell> waterCells =
            gameContext.getSeason().getWaterCells(gameContext);

        if (waterCells.isEmpty()) {
            seaFill.setVisible(false);
            return;
        }

        int minCol = Integer.MAX_VALUE;

        for (GridCell cell : waterCells) {
            if (cell.getCol() < minCol) {
                minCol = cell.getCol();
            }
        }

        float cw = cellWidth();
        float x = gridX + minCol * cw;
        float width = (gridX + gridWidth) - x;

        seaFill.setVisible(true);
        seaFill.setBounds(x, gridY, Math.max(width, 0f), gridHeight);
    }

    /**
     * خط ثابتی که حداکثر پیشروی ممکنِ دریا (Level.getMaxWaterColumn) رو
     * نشون می‌ده؛ فقط یک‌بار در ابتدای مرحله محاسبه می‌شه چون در طول
     * مرحله عوض نمی‌شه.
     */
    private void layoutTideLine() {
        int totalColumns = gameContext.getLevel().getColumns();
        int maxWaterColumn = gameContext.getLevel().getMaxWaterColumn();

        if (maxWaterColumn <= 0) {
            tideLine.setVisible(false);
            return;
        }

        int boundaryColumn = totalColumns - maxWaterColumn;
        boundaryColumn = Math.max(0, Math.min(totalColumns, boundaryColumn));

        float cw = cellWidth();
        float x = gridX + boundaryColumn * cw;

        tideLine.setBounds(
            x - TIDE_LINE_WIDTH / 2f,
            gridY,
            TIDE_LINE_WIDTH,
            gridHeight
        );
    }

    private void buildLowTidePuddles() {
        Set<GridCell> lowTideCells =
            gameContext.getSeason().getLowTideCells(gameContext);

        if (lowTideCells.isEmpty()) {
            return;
        }

        float cw = cellWidth();
        float ch = cellHeight();

        for (GridCell cell : lowTideCells) {
            Image overlay = new Image(getWhitePixelDrawable());
            overlay.setColor(LOW_TIDE_COLOR);

            float x = gridX + cell.getCol() * cw;
            float y = gridY + gridHeight - (cell.getRow() + 1) * ch;

            overlay.setBounds(x, y, cw, ch);
            addActor(overlay);
        }
    }

    private static com.badlogic.gdx.scenes.scene2d.utils.Drawable getWhitePixelDrawable() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }

        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(whitePixel);
    }

    /**
     * یک PAM رو با تغییرِ موقتِ ماتریسِ تبدیلِ batch، روی مستطیل دلخواهِ
     * getX/getY/getWidth/getHeight کش می‌ده (چون این نسخه از libPVZ پارامتر
     * scale توی draw() نداره).
     */
    private static final class PamStretchActor extends Actor {

        private final String pamPath;
        private final String preferredClip;
        private final boolean loop;

        private String resolvedClip;
        private boolean resolved;
        private float stateTime;

        PamStretchActor(String pamPath, String preferredClip, boolean loop) {
            this.pamPath = pamPath;
            this.preferredClip = preferredClip;
            this.loop = loop;
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

            // اگه اکتورِ قبلی رنگِ batch رو نیمه‌شفاف گذاشته باشه (مثلاً
            // پوششِ ساحل پست)، اینجا صریحاً به حالتِ عادی برمی‌گردونیمش.
            batch.setColor(1f, 1f, 1f, parentAlpha);

            Textures.getPamPlayer().draw(
                batch,
                pamPath,
                resolvedClip,
                stateTime,
                centerX,
                centerY,
                loop
            );

            batch.flush();
            batch.setTransformMatrix(original);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void resolveClip() {
            List<String> clips = Textures.getPamPlayer().clips(pamPath);

            if (clips == null || clips.isEmpty()) {
                Gdx.app.error("WaterLayer", "No clips found for PAM: " + pamPath);
                return;
            }

            if (clips.contains(preferredClip)) {
                resolvedClip = preferredClip;
            } else {
                Gdx.app.log(
                    "WaterLayer",
                    "Clip \"" + preferredClip + "\" not found in " + pamPath
                        + ", falling back to \"" + clips.get(0) + "\". Available: " + clips
                );
                resolvedClip = clips.get(0);
            }
        }
    }
}
