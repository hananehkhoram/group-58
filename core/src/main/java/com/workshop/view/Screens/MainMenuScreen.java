package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.MainMenu;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;

import pvz.skin.PvzSkin;

/**
 * The main menu: the hub every other menu (Play/Game, Settings, News, Profile) is
 * reached from. Matches {@code MainMenu}'s notion of "new news" (a red dot on the
 * News button) via {@link MainMenu#shouldShowRedDot(User)}.
 */
public class MainMenuScreen implements Screen {

    /** Hook these up to whatever screens/logic those destinations end up being. */
    public interface Listener {
        void onPlay();
        void onSettings();
        void onNews();
        void onProfile();
        void onLogout();
        void onTest();
    }

    private final Stage stage;
    private final Skin skin;
    private final Listener listener;
    private Texture dotTexture;
    private Texture backgroundTexture;

    public MainMenuScreen(Listener listener) {
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());

        build();
    }

    private void build() {
        Table root = new Table();
        root.setFillParent(true);

        Actor background = buildBackgroundOrNull();
        if (background != null) stage.addActor(background); // added first so it draws behind everything else
        stage.addActor(root);

        Table panel = new Table();
        panel.pad(250);
        panel.padBottom(150);

        User currentUser = UserManager.getInstance().getCurrentUser();
        String nickName = currentUser != null ? currentUser.getNickName() : "player";


        TextButton playButton = new TextButton("Play", skin, "purple");
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onPlay();
            }
        });
        panel.add(playButton).width(260).height(70).expand().center().row();

        ImageButton settingsButton = new ImageButton(skin, "settings");
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onSettings();
            }
        });

        Actor profileButton = buildProfileButton();

        boolean hasUnreadNews = currentUser != null
            && new MainMenu((GameContext) null).shouldShowRedDot(currentUser);
        Actor newsButton = buildNewsButton(hasUnreadNews);

        TextButton logoutButton = new TextButton("Logout", skin, "green_small");
        logoutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onLogout();
            }
        });


        Table iconRow = new Table();
        iconRow.defaults().pad(6);
        iconRow.add(settingsButton).size(75, 72);
        iconRow.add(profileButton).size(75, 72);
        iconRow.add(newsButton);
        panel.add(iconRow).padBottom(10).row();

        //===========================TEST====================================

        TextButton testButton = new TextButton("TEST", skin, "brown");

        testButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) {
                    listener.onTest();
                }
            }
        });

        panel.add(testButton)
            .width(200)
            .height(55)
            .padBottom(10)
            .row();

        //================================================================================


        TextButton exitButton = new TextButton("Exit", skin, "brown");
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Toast.showError(stage, skin, "You must use 'Logout' to leave the main menu.");
            }
        });
        panel.add(exitButton).width(200).padBottom(10).row();
        panel.add(logoutButton)
            .width(130)
            .height(50)
            .padBottom(5)
            .row();

        root.add(panel).grow();
    }


    private Actor buildBackgroundOrNull() {
        backgroundTexture = new Texture(
            Gdx.files.internal("IMAGES/mainmenu_background.png")
        );

        Image background = new Image(backgroundTexture);

        background.setScaling(Scaling.fill);
        background.setFillParent(true);

        return background;
    }

    private Actor buildLogoOrFallbackTitle(String nickName) {
        TextureRegion logoRegion = Textures.regionOrNull("IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL");
        if (logoRegion != null) {
            Image logo = new Image(logoRegion);
            logo.setScaling(Scaling.fit);
            return logo;
        }

        Label fallback = new Label("Welcome, " + nickName, skin, "big");
        fallback.setColor(Color.valueOf("5B3A29")); // "big"/"default" styles default to white, invisible on the cream panel
        return fallback;
    }

    private Actor buildProfileButton() {
        TextureRegion iconRegion = Textures.regionOrNull("IMAGE_UI_MAINMENU_MM_CAMERA");

        if (iconRegion != null) {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.imageUp = new TextureRegionDrawable(iconRegion);
            ImageButton iconButton = new ImageButton(style);
            iconButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (listener != null) listener.onProfile();
                }
            });
            return iconButton;
        }

        TextButton profileButton = new TextButton("Profile", skin, "green_small");
        profileButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onProfile();
            }
        });
        return profileButton;
    }

    private Actor buildNewsButton(boolean showDot) {
        Actor newsButton = buildNewsIconOrFallback();

        if (!showDot) return newsButton;

        Stack stack = new Stack();
        stack.add(newsButton);

        Table dotHolder = new Table();
        dotHolder.top().right();
        Image dot = new Image(getDotDrawable());
        dot.setSize(14, 14);
        dotHolder.add(dot).size(14).padTop(-6).padRight(-6);
        stack.add(dotHolder);

        return stack;
    }

    private Actor buildNewsIconOrFallback() {
        TextureRegion iconRegion = Textures.regionOrNull("IMAGE_UI_MAINMENU_MM_NEWSICON");

        if (iconRegion != null) {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.imageUp = new TextureRegionDrawable(iconRegion);
            ImageButton iconButton = new ImageButton(style);
            iconButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (listener != null) listener.onNews();
                }
            });
            return iconButton;
        }

        TextButton newsButton = new TextButton("News", skin, "default");
        newsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onNews();
            }
        });
        return newsButton;
    }

    private TextureRegionDrawable getDotDrawable() {
        int size = 16;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("E85D5D"));
        pixmap.fillCircle(size / 2, size / 2, size / 2);
        dotTexture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(dotTexture));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();

        if (dotTexture != null) {
            dotTexture.dispose();
        }

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
