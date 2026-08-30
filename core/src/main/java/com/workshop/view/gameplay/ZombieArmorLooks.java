package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.workshop.controller.repository.Textures;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.Armor;
import com.workshop.model.zombie.behavior.ArmorType;

/**
 * ظاهر زره روی زامبی: اسپرایت واقعی مخروط/سطل/آجر از اطلس،
 * با سه مرحله سالم / فرورفته / داغان.
 */
final class ZombieArmorLooks {

    private static final int TEX = 64;
    private static final String IMG = "IMAGE_ZOMBIE_ZOMBIE_TUTORIAL_ZOMBIE_TUTORIAL_";
    private static TextureRegion[][] frames;
    private static boolean loaded;

    private ZombieArmorLooks() {}

    static void draw(
        Batch batch,
        Zombie zombie,
        float originX,
        float originY,
        float cellHeight,
        float parentAlpha
    ) {
        if (zombie == null || zombie.isDead()) {
            return;
        }
        ensureFrames();

        Color old = batch.getColor();
        batch.setColor(1f, 1f, 1f, parentAlpha);

        drawPiece(batch, zombie.getArmor(), originX, originY, cellHeight, true);
        drawPiece(batch, zombie.getSecondaryArmor(), originX, originY, cellHeight, false);

        batch.setColor(old);
    }

    static TextureRegion region(ArmorType type, int stage) {
        if (type == null) {
            return null;
        }
        ensureFrames();
        int clamped = Math.max(0, Math.min(2, stage));
        return frames[type.ordinal()][clamped];
    }

    static int stageOf(Armor armor) {
        if (armor == null || armor.isDestroyed()) {
            return -1;
        }
        float fraction = armor.getArmorHP()
            / (float) Math.max(1, armor.getArmorType().baseHealth);
        if (fraction > 0.66f) {
            return 0;
        }
        if (fraction > 0.33f) {
            return 1;
        }
        return 2;
    }

    private static void drawPiece(
        Batch batch,
        Armor armor,
        float originX,
        float originY,
        float cellHeight,
        boolean primary
    ) {
        int stage = stageOf(armor);
        if (stage < 0) {
            return;
        }

        ArmorType type = armor.getArmorType();
        if (type == ArmorType.NEWSPAPER && stage == 0) {
            return;
        }
        TextureRegion region = frames[type.ordinal()][stage];
        if (region == null) {
            return;
        }

        float width;
        float height;
        float x;
        float y;
        float rotation;
        float originXLocal;
        float originYLocal;

        float aspect = region.getRegionHeight()
            / (float) Math.max(1, region.getRegionWidth());

        switch (type) {
            case CONE -> {
                width = cellHeight * (stage == 0 ? 0.58f : stage == 1 ? 0.54f : 0.50f);
                height = width * aspect;
                rotation = 0f;
                x = originX - width * 0.88f;
                y = originY + cellHeight * 0.70f;
                originXLocal = width * 0.5f;
                originYLocal = 0f;
            }
            case BUCKET -> {
                width = cellHeight * 0.62f;
                height = width * aspect;
                rotation = 0f;
                x = originX - width * 0.88f;
                y = originY + cellHeight * 0.72f;
                originXLocal = width * 0.5f;
                originYLocal = 0f;
            }
            case BRICK -> {
                width = cellHeight * 0.62f;
                height = cellHeight * (stage == 0 ? 0.56f : stage == 1 ? 0.50f : 0.42f);
                rotation = 0f;
                x = originX - width * 0.88f;
                y = originY + cellHeight * 0.70f;
                originXLocal = width * 0.5f;
                originYLocal = 0f;
            }
            case SHOULDER_CROWN -> {
                width = cellHeight * 0.46f;
                height = cellHeight * (stage == 0 ? 0.34f : stage == 1 ? 0.26f : 0.16f);
                rotation = stage == 0 ? 0f : stage == 1 ? 8f : 20f;
                x = originX - width * 0.42f;
                y = originY + cellHeight * 0.96f;
                originXLocal = width * 0.5f;
                originYLocal = 0f;
            }
            case SHOULDER_ARMOR -> {
                width = cellHeight * 0.58f;
                height = cellHeight * (stage == 0 ? 0.28f : stage == 1 ? 0.22f : 0.14f);
                rotation = stage == 0 ? 0f : stage == 1 ? -6f : 16f;
                x = originX - width * 0.48f;
                y = originY + cellHeight * 0.52f;
                originXLocal = width * 0.5f;
                originYLocal = height * 0.5f;
            }
            case NEWSPAPER -> {
                width = cellHeight * (stage == 2 ? 0.36f : 0.46f);
                height = cellHeight * (stage == 0 ? 0.42f : stage == 1 ? 0.34f : 0.22f);
                rotation = stage == 0 ? -12f : stage == 1 ? 8f : 24f;
                x = originX - width * 0.15f;
                y = originY + cellHeight * 0.38f;
                originXLocal = width * 0.5f;
                originYLocal = height * 0.5f;
            }
            default -> {
                return;
            }
        }

        if (!primary && type == ArmorType.SHOULDER_ARMOR) {
            x += cellHeight * 0.04f;
        }

        batch.draw(
            region,
            x,
            y,
            originXLocal,
            originYLocal,
            width,
            height,
            1f,
            1f,
            rotation
        );
    }

    private static void ensureFrames() {
        if (loaded) {
            return;
        }
        loaded = true;
        ArmorType[] types = ArmorType.values();
        frames = new TextureRegion[types.length][3];
        for (ArmorType type : types) {
            for (int stage = 0; stage < 3; stage++) {
                TextureRegion atlas = atlasFrame(type, stage);
                if (atlas != null) {
                    frames[type.ordinal()][stage] = atlas;
                } else {
                    TextureRegion region = new TextureRegion(buildTexture(type, stage));
                    region.flip(false, true);
                    frames[type.ordinal()][stage] = region;
                }
            }
        }
    }

    private static TextureRegion atlasFrame(ArmorType type, int stage) {
        String[] ids = atlasIds(type, stage);
        if (ids == null) {
            return null;
        }
        for (String id : ids) {
            TextureRegion region = Textures.regionOrNull(id);
            if (region != null) {
                return region;
            }
        }
        return null;
    }

    private static final String WEST =
        "IMAGE_ZOMBIE_ZOMBIE_WEST_BASIC_ZOMBIE_WEST_BASIC_";
    private static final String WEST_BRICK =
        "IMAGE_ZOMBIE_ZOMBIE_WEST_BASIC_BRICK_ZOMBIE_WEST_BASIC_BRICK_";

    private static String[] atlasIds(ArmorType type, int stage) {
        int s = Math.max(0, Math.min(2, stage));
        return switch (type) {
            case CONE -> switch (s) {
                case 0 -> new String[] {
                    WEST + "80X83_2",
                    WEST_BRICK + "80X83_2",
                    IMG + "76X98"
                };
                case 1 -> new String[] {
                    WEST + "80X83",
                    WEST_BRICK + "80X83",
                    IMG + "60X86"
                };
                default -> new String[] {
                    WEST + "82X82",
                    WEST_BRICK + "82X82",
                    IMG + "80X83_2"
                };
            };
            case BUCKET -> switch (s) {
                case 0 -> new String[] {
                    WEST + "99X92",
                    WEST_BRICK + "99X92",
                    IMG + "81X64"
                };
                case 1 -> new String[] {
                    WEST + "99X87",
                    WEST_BRICK + "99X87",
                    IMG + "81X64_2"
                };
                default -> new String[] {
                    WEST + "100X75",
                    WEST_BRICK + "100X75",
                    IMG + "80X72"
                };
            };
            case BRICK -> new String[] {
                IMG + (s == 0 ? "91X97" : s == 1 ? "96X97" : "82X82")
            };
            default -> null;
        };
    }

    private static Texture buildTexture(ArmorType type, int stage) {
        Pixmap pixmap = new Pixmap(TEX, TEX, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();

        switch (type) {
            case CONE -> paintCone(pixmap, stage);
            case BUCKET -> paintBucket(pixmap, stage);
            case BRICK -> paintBrick(pixmap, stage);
            case SHOULDER_CROWN -> paintCrown(pixmap, stage);
            case SHOULDER_ARMOR -> paintShoulder(pixmap, stage);
            case NEWSPAPER -> paintNewspaper(pixmap, stage);
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return texture;
    }

    private static void paintCone(Pixmap pixmap, int stage) {
        int top = stage == 0 ? 4 : stage == 1 ? 16 : 30;
        int left = stage == 2 ? 14 : 10;
        int right = TEX - 1 - left;
        pixmap.setColor(0.93f, 0.42f, 0.10f, 1f);
        fillTriangle(pixmap, TEX / 2, top, left, 60, right, 60);
        pixmap.setColor(1f, 1f, 1f, 0.95f);
        if (stage == 0) {
            pixmap.fillRectangle(18, 28, 28, 5);
            pixmap.fillRectangle(16, 44, 32, 5);
        } else if (stage == 1) {
            pixmap.fillRectangle(22, 34, 18, 4);
        }
        punch(pixmap, stage == 1 ? 46 : 48, 42, stage == 0 ? 0 : stage == 1 ? 9 : 12);
        if (stage == 2) {
            punch(pixmap, 18, 46, 8);
            pixmap.setColor(0.45f, 0.22f, 0.08f, 1f);
            pixmap.fillRectangle(20, 56, 24, 5);
        }
    }

    private static void paintBucket(Pixmap pixmap, int stage) {
        int top = stage == 0 ? 10 : stage == 1 ? 18 : 32;
        int inset = stage == 2 ? 8 : 6;
        pixmap.setColor(0.62f, 0.66f, 0.70f, 1f);
        pixmap.fillRectangle(12 + inset, top, 40 - inset * 2, 50 - top);
        pixmap.setColor(0.78f, 0.82f, 0.86f, 1f);
        pixmap.fillRectangle(8, top, 48, 8);
        if (stage == 0) {
            pixmap.setColor(0.45f, 0.48f, 0.52f, 1f);
            pixmap.fillRectangle(16, 28, 32, 3);
            pixmap.fillRectangle(16, 40, 32, 3);
        }
        if (stage >= 1) {
            pixmap.setColor(0.38f, 0.32f, 0.28f, 1f);
            fillTriangle(pixmap, 12, top + 8, 28, 38, 10, 52);
        }
        punch(pixmap, 44, 36, stage == 0 ? 0 : stage == 1 ? 8 : 13);
        if (stage == 2) {
            punch(pixmap, 22, 40, 7);
            pixmap.setColor(0.35f, 0.30f, 0.28f, 1f);
            pixmap.fillRectangle(14, 50, 36, 8);
        }
    }

    private static void paintBrick(Pixmap pixmap, int stage) {
        int top = stage == 0 ? 14 : stage == 1 ? 22 : 36;
        pixmap.setColor(0.72f, 0.28f, 0.18f, 1f);
        pixmap.fillRectangle(10, top, 44, 56 - top);
        pixmap.setColor(0.88f, 0.78f, 0.62f, 1f);
        pixmap.fillRectangle(10, 32, 44, 3);
        pixmap.fillRectangle(30, top, 3, 56 - top);
        if (stage >= 1) {
            punch(pixmap, 48, 24, 10);
            pixmap.setColor(0.35f, 0.12f, 0.08f, 1f);
            pixmap.fillRectangle(18, 26, 3, 22);
        }
        if (stage == 2) {
            punch(pixmap, 16, 48, 9);
            pixmap.setColor(0.40f, 0.16f, 0.10f, 1f);
            pixmap.fillRectangle(12, 50, 40, 8);
        }
    }

    private static void paintCrown(Pixmap pixmap, int stage) {
        int top = stage == 0 ? 12 : stage == 1 ? 20 : 34;
        pixmap.setColor(0.90f, 0.72f, 0.18f, 1f);
        pixmap.fillRectangle(12, 28, 40, 24);
        if (stage < 2) {
            pixmap.fillRectangle(12, top, 10, 28 - top);
            pixmap.fillRectangle(27, top - 4, 10, 32 - top);
            pixmap.fillRectangle(42, top, 10, 28 - top);
        }
        if (stage >= 1) {
            punch(pixmap, 48, 22, 8);
            pixmap.setColor(0.45f, 0.32f, 0.10f, 1f);
            pixmap.fillRectangle(20, 40, 24, 4);
        }
        if (stage == 2) {
            punch(pixmap, 20, 36, 7);
            pixmap.fillRectangle(14, 46, 36, 8);
        }
    }

    private static void paintShoulder(Pixmap pixmap, int stage) {
        int top = stage == 0 ? 16 : stage == 1 ? 24 : 34;
        pixmap.setColor(0.55f, 0.58f, 0.62f, 1f);
        pixmap.fillRectangle(4, top, 22, 40 - top);
        pixmap.fillRectangle(38, top, 22, 40 - top);
        pixmap.setColor(0.72f, 0.74f, 0.78f, 1f);
        pixmap.fillRectangle(4, top, 22, 6);
        pixmap.fillRectangle(38, top, 22, 6);
        if (stage >= 1) {
            punch(pixmap, 18, 28, 8);
        }
        if (stage == 2) {
            punch(pixmap, 50, 30, 9);
            pixmap.setColor(0.32f, 0.28f, 0.26f, 1f);
            pixmap.fillRectangle(6, 42, 18, 8);
            pixmap.fillRectangle(40, 42, 18, 8);
        }
    }

    private static void paintNewspaper(Pixmap pixmap, int stage) {
        int top = stage == 0 ? 8 : stage == 1 ? 14 : 26;
        pixmap.setColor(0.93f, 0.91f, 0.82f, 1f);
        pixmap.fillRectangle(10, top, 44, 54 - top);
        pixmap.setColor(0.25f, 0.25f, 0.28f, 1f);
        pixmap.fillRectangle(16, top + 8, 32, 3);
        pixmap.fillRectangle(16, top + 16, 28, 2);
        pixmap.fillRectangle(16, top + 22, 30, 2);
        if (stage >= 1) {
            punch(pixmap, 50, top + 4, 12);
        }
        if (stage == 2) {
            punch(pixmap, 18, 36, 10);
            pixmap.setColor(0.93f, 0.91f, 0.82f, 1f);
            pixmap.fillRectangle(8, 40, 14, 18);
            pixmap.fillRectangle(40, 38, 16, 20);
        }
    }

    private static void punch(Pixmap pixmap, int x, int y, int radius) {
        if (radius <= 0) {
            return;
        }
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fillCircle(x, y, radius);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
    }

    private static void fillTriangle(Pixmap pixmap, int x1, int y1, int x2, int y2, int x3, int y3) {
        pixmap.fillTriangle(x1, y1, x2, y2, x3, y3);
    }
}
