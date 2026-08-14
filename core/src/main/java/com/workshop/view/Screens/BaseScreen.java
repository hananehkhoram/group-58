package com.workshop.view.Screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public abstract class BaseScreen implements Screen {

    protected final Skin skin;
    protected Texture generatedBgTexture;

    protected BaseScreen(Skin skin) {
        this.skin = skin;
    }
    protected Label createSafeLabel(String text, String styleName) {
        if (styleName != null && skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        }
        return new Label(text, skin);
    }

    protected Texture createSolidColorTexture(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    protected void safeDisposeTexture(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        safeDisposeTexture(generatedBgTexture);
    }
}
