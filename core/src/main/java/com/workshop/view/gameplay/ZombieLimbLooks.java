package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** اسپرایت جدا برای سر و دست افتاده. */
final class ZombieLimbLooks {

    private static TextureRegion head;
    private static TextureRegion arm;

    private ZombieLimbLooks() {}

    static TextureRegion head() {
        ensure();
        return head;
    }

    static TextureRegion arm() {
        ensure();
        return arm;
    }

    private static void ensure() {
        if (head != null) {
            return;
        }
        head = make(ZombieLimbLooks::paintHead);
        arm = make(ZombieLimbLooks::paintArm);
    }

    private interface Painter {
        void paint(Pixmap pixmap);
    }

    private static TextureRegion make(Painter painter) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        painter.paint(pixmap);
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        TextureRegion region = new TextureRegion(texture);
        region.flip(false, true);
        return region;
    }

    private static void paintHead(Pixmap pixmap) {
        pixmap.setColor(0.42f, 0.62f, 0.32f, 1f);
        pixmap.fillCircle(32, 30, 20);
        pixmap.fillRectangle(24, 46, 16, 10);
        pixmap.setColor(0.28f, 0.42f, 0.20f, 1f);
        pixmap.fillCircle(32, 24, 18);
        pixmap.setColor(0.42f, 0.62f, 0.32f, 1f);
        pixmap.fillCircle(32, 32, 16);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fillCircle(24, 28, 5);
        pixmap.fillCircle(40, 28, 5);
        pixmap.setColor(0.08f, 0.08f, 0.08f, 1f);
        pixmap.fillCircle(25, 29, 2);
        pixmap.fillCircle(41, 29, 2);
        pixmap.setColor(0.55f, 0.12f, 0.12f, 1f);
        pixmap.fillRectangle(22, 40, 20, 6);
        pixmap.setColor(0.95f, 0.95f, 0.90f, 1f);
        pixmap.fillRectangle(24, 41, 3, 4);
        pixmap.fillRectangle(31, 41, 3, 4);
        pixmap.fillRectangle(38, 41, 3, 4);
        pixmap.setColor(0.35f, 0.22f, 0.12f, 1f);
        pixmap.fillCircle(18, 16, 4);
        pixmap.fillCircle(32, 12, 5);
        pixmap.fillCircle(46, 16, 4);
    }

    private static void paintArm(Pixmap pixmap) {
        pixmap.setColor(0.42f, 0.62f, 0.32f, 1f);
        pixmap.fillRectangle(26, 6, 12, 28);
        pixmap.fillCircle(32, 8, 7);
        pixmap.fillRectangle(22, 30, 20, 12);
        pixmap.fillCircle(18, 42, 7);
        pixmap.fillCircle(32, 50, 8);
        pixmap.fillCircle(46, 42, 7);
        pixmap.setColor(0.32f, 0.48f, 0.22f, 1f);
        pixmap.fillRectangle(28, 18, 8, 4);
        pixmap.setColor(0.38f, 0.55f, 0.28f, 1f);
        pixmap.fillRectangle(14, 38, 8, 14);
        pixmap.fillRectangle(28, 48, 8, 12);
        pixmap.fillRectangle(42, 38, 8, 14);
    }
}
