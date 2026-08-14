package com.workshop.view.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Screens.CollectionScreen;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

public class CurrencyHeader extends Table {

    private final Skin skin;
    private final TextureBank textureBank;
    private final PamPlayer pamPlayer;
    private Label coinLbl;
    private Label gemLbl;
    private Texture fallbackBg;
    private float stateTime = 0f;

    public CurrencyHeader() {
        FileHandle assetsFolder = Gdx.files.internal("assets");
        textureBank = new TextureBank("768", assetsFolder);
        pamPlayer = new PamPlayer(textureBank, assetsFolder);
        this.skin = PvzSkin.get();
        buildWidget();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    private void buildWidget() {
        User currentUser = UserManager.getInstance().getCurrentUser();
        long gems = currentUser != null ? currentUser.getGems() : 0;
        long coins = currentUser != null ? currentUser.getCoins() : 0;
        boolean debugMode = currentUser != null && currentUser.isDebugMode();

        final String diamondPamPath = "768/INITIAL/EFFECTS/COIN_DIAMOND/COIN_DIAMOND.PAM";
        final String coinPamPath = "768/INITIAL/EFFECTS/COIN_STACK/COIN_STACK.PAM";
        final float iconScale = 0.3f;

        // بخش الماس
        Table gemBg = new Table();
        if (skin.has("AlmanacCurrencyBg", Drawable.class)) {
            gemBg.setBackground(skin.getDrawable("AlmanacCurrencyBg"));
        } else {
            gemBg.setBackground(getFallbackBackground());
        }

        gemLbl = createSafeLabel(String.valueOf(gems), "big");
        gemLbl.setFontScale(0.55f);
        gemBg.add(gemLbl).padLeft(40).padRight(40).expandY();

        Table diamondPamContainer = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (textureBank != null) textureBank.update();

                Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + getHeight() / 2f;

                Matrix4 scaleMatrix = oldMatrix.cpy();
                scaleMatrix.translate(drawX, drawY, 0);
                scaleMatrix.scale(iconScale, iconScale, 1f);
                scaleMatrix.translate(-drawX, -drawY, 0);
                batch.setTransformMatrix(scaleMatrix);

                try {
                    if (pamPlayer != null) {
                        pamPlayer.draw(batch, diamondPamPath, "idle", stateTime, drawX, drawY, true);
                    }
                } catch (Exception ignored) {
                }

                batch.setTransformMatrix(oldMatrix);
            }
        };

        Table gemWrapper = new Table();
        gemWrapper.add(gemBg).height(32);
        gemWrapper.add(diamondPamContainer).size(35, 35).right().padRight(debugMode ? 5 : 0).padTop(5);

        if (debugMode) {
            ImageButton gemDebugBtn = new ImageButton(skin, "generic_close_circle");
            gemDebugBtn.setTransform(true);
            gemDebugBtn.setRotation(45f);
            gemDebugBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    User user = UserManager.getInstance().getCurrentUser();
                    if (user != null) {
                        user.setGems(user.getGems() + 5);
                        updateValues();
                    }
                }
            });
            gemWrapper.add(gemDebugBtn).size(25, 25).padLeft(5);
        }

        this.add(gemWrapper).padRight(20);

        // بخش سکه
        Table coinBg = new Table();
        if (skin.has("AlmanacCurrencyBg", Drawable.class)) {
            coinBg.setBackground(skin.getDrawable("AlmanacCurrencyBg"));
        } else {
            coinBg.setBackground(getFallbackBackground());
        }

        coinLbl = createSafeLabel(String.valueOf(coins), "big");
        coinLbl.setFontScale(0.55f);
        coinBg.add(coinLbl).padLeft(40).padRight(40).expandY();

        Table coinPamContainer = new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (textureBank != null) textureBank.update();

                Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + getHeight() / 2f;

                Matrix4 scaleMatrix = oldMatrix.cpy();
                scaleMatrix.translate(drawX, drawY, 0);
                scaleMatrix.scale(iconScale, iconScale, 1f);
                scaleMatrix.translate(-drawX, -drawY, 0);
                batch.setTransformMatrix(scaleMatrix);

                try {
                    if (pamPlayer != null) {
                        pamPlayer.draw(batch, coinPamPath, "idle", stateTime, drawX, drawY, true);
                    }
                } catch (Exception ignored) {
                }

                batch.setTransformMatrix(oldMatrix);
            }
        };

        Table coinWrapper = new Table();
        coinWrapper.add(coinBg).height(32);
        coinWrapper.add(coinPamContainer).size(35, 35).right().padRight(debugMode ? 5 : 35).padTop(0);

        if (debugMode) {
            ImageButton coinDebugBtn = new ImageButton(skin, "generic_close_circle");
            coinDebugBtn.setTransform(true);
            coinDebugBtn.setRotation(45f);
            coinDebugBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    User user = UserManager.getInstance().getCurrentUser();
                    if (user != null) {
                        user.setCoins(user.getCoins() + 100);
                        updateValues();
                    }
                }
            });
            coinWrapper.add(coinDebugBtn).size(25, 25).padLeft(5);
        }

        this.add(coinWrapper);
    }

    public void updateValues() {
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (coinLbl != null) coinLbl.setText(String.valueOf(currentUser.getCoins()));
            if (gemLbl != null) gemLbl.setText(String.valueOf(currentUser.getGems()));
        }
    }

    private Drawable getFallbackBackground() {
        if (fallbackBg == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(0f, 0f, 0f, 0.65f));
            pixmap.fill();
            fallbackBg = new Texture(pixmap);
            pixmap.dispose();
        }
        return new TextureRegionDrawable(new TextureRegion(fallbackBg));
    }

    private Label createSafeLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        }
        return new Label(text, skin);
    }
}
