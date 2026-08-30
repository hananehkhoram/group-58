package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.workshop.controller.repository.Textures;

final class IceDamageSprite {

    static final double MAX_ICE_HP = 600.0;

    // 0 تا 5 = مراحل آسیب
    // 6 = تکه‌های نهایی بعد از شکستن
    private static final int DAMAGE_STAGE_COUNT = 6;
    private static final int TOTAL_FRAME_COUNT = 7;
    private static final int BREAK_FRAME = 6;

    static final float BREAK_DURATION = 0.18f;

    private static final String SHEET_PATH =
        "IMAGES/768/INITIAL/EFFECTS/IceBreak/ice_break_strip.png";

    private static Texture sheet;
    private static TextureRegion[] frames;

    private IceDamageSprite() {
    }

    static void drawDamage(
        Batch batch,
        double iceHp,
        float centerX,
        float centerY,
        float cellHeight,
        float heightRatio,
        float xOffset,
        float yOffset,
        float parentAlpha,
        float flash
    ) {
        int frame = resolveDamageFrame(iceHp);

        drawFrame(
            batch,
            frame,
            centerX,
            centerY,
            cellHeight,
            heightRatio,
            xOffset,
            yOffset,
            parentAlpha,
            flash
        );
    }

    static void drawBreak(
        Batch batch,
        float centerX,
        float centerY,
        float cellHeight,
        float heightRatio,
        float xOffset,
        float yOffset,
        float parentAlpha
    ) {
        drawFrame(
            batch,
            BREAK_FRAME,
            centerX,
            centerY,
            cellHeight,
            heightRatio,
            xOffset,
            yOffset,
            parentAlpha,
            0f
        );
    }

    private static int resolveDamageFrame(double iceHp) {
        float hpFraction = MathUtils.clamp(
            (float) (iceHp / MAX_ICE_HP),
            0f,
            1f
        );

        float damageFraction = 1f - hpFraction;

        int frame = (int) Math.floor(
            damageFraction * DAMAGE_STAGE_COUNT
        );

        if (frame < 0) {
            return 0;
        }

        if (frame >= DAMAGE_STAGE_COUNT) {
            return DAMAGE_STAGE_COUNT - 1;
        }

        return frame;
    }

    private static void drawFrame(
        Batch batch,
        int frameIndex,
        float centerX,
        float centerY,
        float cellHeight,
        float heightRatio,
        float xOffset,
        float yOffset,
        float parentAlpha,
        float flash
    ) {
        TextureRegion[] loadedFrames = getFrames();

        if (loadedFrames == null) {
            return;
        }

        TextureRegion region = loadedFrames[frameIndex];

        float drawHeight = cellHeight * heightRatio;

        float aspect =
            (float) region.getRegionWidth()
                / region.getRegionHeight();

        float drawWidth = drawHeight * aspect;

        float drawX =
            centerX - drawWidth / 2f + xOffset;

        float drawY =
            centerY - drawHeight / 2f + yOffset;

        batch.setColor(
            1f + flash,
            1f + flash,
            1f + flash,
            parentAlpha
        );

        batch.draw(
            region,
            drawX,
            drawY,
            drawWidth,
            drawHeight
        );

        batch.setColor(
            1f,
            1f,
            1f,
            parentAlpha
        );
    }

    private static TextureRegion[] getFrames() {
        if (frames != null) {
            return frames;
        }

        FileHandle file =
            Textures.assetsRoot().child(SHEET_PATH);

        if (!file.exists()) {
            Gdx.app.error(
                "IceDamageSprite",
                "Ice sprite sheet not found: "
                    + file.file().getAbsolutePath()
            );
            return null;
        }

        sheet = new Texture(file);

        sheet.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );

        int textureWidth = sheet.getWidth();
        int textureHeight = sheet.getHeight();

        // برای تصویر 2172 پیکسلی:
        // 2172 % 7 = 2
        // پس فقط 2 پیکسل اضافه را حذف می‌کنیم.
        int frameWidth = textureWidth / TOTAL_FRAME_COUNT;

        int usableWidth =
            frameWidth * TOTAL_FRAME_COUNT;

        int startX =
            (textureWidth - usableWidth) / 2;

        Gdx.app.log(
            "IceDamageSprite",
            "Sheet: "
                + textureWidth
                + "x"
                + textureHeight
                + ", frameWidth="
                + frameWidth
                + ", startX="
                + startX
        );

        frames =
            new TextureRegion[TOTAL_FRAME_COUNT];

        for (int i = 0; i < TOTAL_FRAME_COUNT; i++) {
            frames[i] = new TextureRegion(
                sheet,
                startX + i * frameWidth,
                0,
                frameWidth,
                textureHeight
            );
        }

        return frames;
    }
}
