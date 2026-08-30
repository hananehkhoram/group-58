package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.model.zombie.Zombie;
import com.workshop.view.gameplay.ZombieAnimationResolver;
import com.workshop.view.gameplay.ZombieAnimationSpec;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

public class ZombieDetailsScreen extends BaseScreen {

    public interface BackListener {
        void onBack();
    }

    private static final float BASE_WIDTH = 1280f;
    private static final float BASE_HEIGHT = 720f;
    private static final Color BG_COLOR = Color.valueOf("0d1b3e");
    private static final Color STAT_BOX_BG_COLOR = Color.valueOf("2f6c2f");

    private static ZombieAnimationResolver animationResolver;

    private final Stage stage;
    private final Zombie zombie;
    private final BackListener backListener;
    private final PamPlayer pamPlayer;
    private final TextureBank textureBank;
    private final ZombieAnimationSpec animationSpec;

    private float stateTime = 0f;
    private Image bg;
    private Texture generatedBgTexture;
    private Drawable cardBgDrawable;

    public ZombieDetailsScreen(Zombie zombie, PamPlayer pamPlayer, TextureBank textureBank, BackListener backListener) {
        this(zombie, pamPlayer, textureBank, backListener, null);
    }

    public ZombieDetailsScreen(Zombie zombie, PamPlayer pamPlayer, TextureBank textureBank, BackListener backListener, String seasonName) {
        super(PvzSkin.get());

        this.zombie = zombie;
        this.pamPlayer = pamPlayer;
        this.textureBank = textureBank;
        this.backListener = backListener;
        this.stage = new Stage(new ScreenViewport());

        if (animationResolver == null) {
            animationResolver = new ZombieAnimationResolver();
        }
        this.animationSpec = animationResolver.resolve(zombie, seasonName);

        this.cardBgDrawable = createSolidColorDrawable(STAT_BOX_BG_COLOR);

        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);

        String bgTexturePath = "IMAGES/Menus/Collection/plantDetailBG.png";

        if (Gdx.files.internal(bgTexturePath).exists()) {
            Texture bgTexture = new Texture(Gdx.files.internal(bgTexturePath));
            bgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTexture)));
        } else {
            generatedBgTexture = createSolidColorTexture(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a);
            generatedBgTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            bg = new Image(new TextureRegionDrawable(new TextureRegion(generatedBgTexture)));
        }
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        stage.addActor(root);

        float scale = getScaleFactor();

        Table topBar = buildTopBar(scale);
        root.add(topBar).fillX().row();

        Table contentArea = new Table();
        Table leftCol = buildLeftColumn(scale);
        Table rightCol = buildRightColumn(scale);

        contentArea.add(leftCol).padRight(30 * scale).top();
        contentArea.add(rightCol).top().expandX().fillX().row();

        root.add(contentArea).expand().center().padTop(10 * scale).row();
    }

    private Table buildTopBar(float scale) {
        Table topBar = new Table();

        TextButton backBtn = new TextButton("Back", skin, "brown");
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (backListener != null) backListener.onBack();
            }
        });
        topBar.add(backBtn).width(110 * scale).height(45 * scale).left().pad(15);

        Label nameTitle = createSafeLabel(zombie.getName(), "big");
        nameTitle.setFontScale(1.3f * scale);
        topBar.add(nameTitle).expandX().center();

        return topBar;
    }

    private Table buildLeftColumn(float scale) {
        Table leftCol = new Table();

        Stack zombieStack = new Stack();

        Actor zombiePamActor = createZombiePamActor(scale);
        Table animWrapper = new Table();
        animWrapper.add(zombiePamActor).expand().fill();
        zombieStack.add(animWrapper);

        leftCol.add(zombieStack).size(320 * scale, 380 * scale).padBottom(15 * scale).row();

        return leftCol;
    }

    private Drawable getStatIconDrawable(String fileNameWithoutExt) {
        String fullPath = "IMAGES/Menus/Collection/" + fileNameWithoutExt + ".png";
        try {
            if (Gdx.files.internal(fullPath).exists()) {
                Texture tex = new Texture(Gdx.files.internal(fullPath));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return new TextureRegionDrawable(new TextureRegion(tex));
            }
        } catch (Exception ignored) {}
        return null;
    }

    public Texture createSolidColorTexture(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Drawable createSolidColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private Table createStatBox(Drawable icon, String labelText, String valueText, float scale) {
        Table box = new Table();
        box.setBackground(cardBgDrawable);

        if (icon != null) {
            box.add(new Image(icon)).size(32 * scale, 32 * scale).pad(6 * scale);
        } else {
            box.add().size(32 * scale, 32 * scale).pad(6 * scale);
        }

        Table textTable = new Table();
        textTable.defaults().left();
        Label lbl = createSafeLabel(labelText, "default");
        lbl.setFontScale(0.75f * scale);
        lbl.setColor(Color.LIGHT_GRAY);
        textTable.add(lbl).row();

        Label val = createSafeLabel(valueText, "big");
        val.setFontScale(0.9f * scale);
        textTable.add(val);

        box.add(textTable).expandX().fillX().padRight(10 * scale);
        return box;
    }

    private Table buildRightColumn(float scale) {
        Table rightCol = new Table();
        rightCol.defaults().left().padBottom(10 * scale);

        Table statsGrid = new Table();
        statsGrid.defaults().space(10 * scale);

        Drawable toughnessIcon = getStatIconDrawable("toughness");
        Drawable rechargeIcon = getStatIconDrawable("recharge");
        Drawable damageIcon = getStatIconDrawable("damage");
        Drawable familyIcon = getStatIconDrawable("family");

        Table col1 = new Table();
        col1.defaults().padBottom(8 * scale).fillX();
        col1.add(createStatBox(toughnessIcon, "TOUGHNESS (HP)", String.valueOf(zombie.getHp()), scale)).width(220 * scale).row();
        col1.add(createStatBox(damageIcon, "EATING DAMAGE/SEC", String.format("%.1f", zombie.getEatDps()), scale)).width(220 * scale).row();

        Table col2 = new Table();
        col2.defaults().padBottom(8 * scale).fillX();
        col2.add(createStatBox(rechargeIcon, "SPEED", String.format("%.2f", zombie.getSpeed()), scale)).width(220 * scale).row();
        col2.add(createStatBox(familyIcon, "WEIGHT", String.valueOf(zombie.getWeight()), scale)).width(220 * scale).row();

        statsGrid.add(col1).padRight(15 * scale);
        statsGrid.add(col2);
        rightCol.add(statsGrid).row();

        return rightCol;
    }

    private Actor createZombiePamActor(float scale) {
        return new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                if (animationSpec == null) return;

                String pamPath = animationSpec.getPamPath();
                String idleClip = animationSpec.getIdleClip();
                if (pamPath == null || idleClip == null) return;

                if (textureBank != null) textureBank.update();

                float drawX = getX() + (getWidth() / 2f) + (10f * scale);
                // ضریب ارتفاع رو از 0.2f به 0.35f تغییر دادیم تا زامبی بالاتر بیاد
                float drawY = getY() + (getHeight() * 0.35f);

                try {
                    pamPlayer.draw(batch, pamPath, idleClip, stateTime, drawX, drawY, true);
                } catch (Exception ignored) {
                }
            }
        };
    }

    private float getScaleFactor() {
        float scaleX = (float) Gdx.graphics.getWidth() / BASE_WIDTH;
        float scaleY = (float) Gdx.graphics.getHeight() / BASE_HEIGHT;
        return Math.max(0.8f, Math.min(scaleX, scaleY));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (bg != null) {
            bg.setSize(width, height);
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (generatedBgTexture != null) {
            generatedBgTexture.dispose();
        }
        if (bg != null && bg.getDrawable() instanceof TextureRegionDrawable) {
            ((TextureRegionDrawable) bg.getDrawable()).getRegion().getTexture().dispose();
        }
        super.dispose();
    }
}
