package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.workshop.controller.repository.Textures;
import com.workshop.model.zombie.Zombie;

import pvz.libpvz.pam.PamPlayer;
import pvz.skin.PvzSkin;

public class ZombieDetailsScreen implements Screen {

    public interface BackListener {
        void onBack();
    }

    private final Stage stage;
    private final Skin skin;
    private final Zombie zombie;
    private final BackListener backListener;
    private final PamPlayer pamPlayer;

    private Texture menuBgTexture;
    private Image bg;
    private float stateTime = 0f;

    private static final float BASE_WIDTH = 1280f;
    private static final float BASE_HEIGHT = 720f;
    private static final Color BG_COLOR = Color.valueOf("0d1b3e");

    public ZombieDetailsScreen(Zombie zombie, PamPlayer pamPlayer, BackListener backListener) {
        this.zombie = zombie;
        this.pamPlayer = pamPlayer;
        this.backListener = backListener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new FitViewport(BASE_WIDTH, BASE_HEIGHT));

        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(BG_COLOR);
        pixmap.fill();
        menuBgTexture = new Texture(pixmap);
        pixmap.dispose();

        bg = new Image(new TextureRegionDrawable(new TextureRegion(menuBgTexture)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        stage.addActor(root);

        float scale = getScaleFactor();

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
        root.add(topBar).fillX().row();

        Table contentArea = new Table();

        Table leftCol = new Table();
        Actor zombiePamActor = createZombiePamActor(zombie.getName(), scale);
        leftCol.add(zombiePamActor).size(220 * scale, 220 * scale).padBottom(15 * scale).row();

        contentArea.add(leftCol).padRight(40 * scale).top();

        Table rightCol = new Table();
        rightCol.defaults().left().pad(8 * scale);

        rightCol.add(createSafeLabel("Toughness (HP):", "big")).right();
        rightCol.add(createSafeLabel(" " + zombie.getHp(), "big")).row();

        rightCol.add(createSafeLabel("Speed:", "big")).right();
        rightCol.add(createSafeLabel(" " + zombie.getSpeed(), "big")).row();

        rightCol.add(createSafeLabel("Damage:", "big")).right();

        contentArea.add(rightCol).top().row();

        root.add(contentArea).expand().center().row();
    }

    private Actor createZombiePamActor(String zombieName, float scale) {
        String formattedName = zombieName.toUpperCase().replace(" ", "_");
        String pamPath = "768/INITIAL/ZOMBIES/" + formattedName + "/" + formattedName + ".PAM";

        return new Table() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                super.draw(batch, parentAlpha);
                if (pamPlayer == null) {
                    drawFallbackTexture(batch);
                    return;
                }

                Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
                float drawX = getX() + getWidth() / 2f;
                float drawY = getY() + getHeight() / 2f - (30 * scale);

                Matrix4 scaleMatrix = oldMatrix.cpy();
                scaleMatrix.translate(drawX, drawY, 0);
                scaleMatrix.scale(0.55f * scale, 0.55f * scale, 1f);
                scaleMatrix.translate(-drawX, -drawY, 0);
                batch.setTransformMatrix(scaleMatrix);

                boolean drawn = false;
                String[] possibleClips = {"idle", "anim_idle", "walk", "anim_walk"};
                for (String clip : possibleClips) {
                    try {
                        pamPlayer.draw(batch, pamPath, clip, stateTime, drawX, drawY, true);
                        drawn = true;
                        break;
                    } catch (Exception ignored) {}
                }

                if (!drawn) {
                    drawFallbackTexture(batch);
                }

                batch.setTransformMatrix(oldMatrix);
            }

            private void drawFallbackTexture(Batch batch) {
                TextureRegion reg = Textures.regionOrNull("ZOMBIE_" + formattedName);
                if (reg == null) reg = Textures.regionOrNull(formattedName);
                if (reg != null) {
                    batch.draw(reg, getX(), getY(), getWidth(), getHeight());
                }
            }
        };
    }

    private float getScaleFactor() {
        float scaleX = stage.getWidth() / BASE_WIDTH;
        float scaleY = stage.getHeight() / BASE_HEIGHT;
        return Math.max(0.8f, Math.min(scaleX, scaleY));
    }

    private Label createSafeLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        }
        return new Label(text, skin);
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
            bg.setSize(stage.getWidth(), stage.getHeight());
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (menuBgTexture != null) menuBgTexture.dispose();
    }
}
