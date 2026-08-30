package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.MainMenu;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;
import com.workshop.view.components.CurrencyHeader;

import pvz.skin.PvzSkin;

import java.time.LocalTime;

public class MainMenuScreen implements Screen {

    public interface Listener {
        void onPlay();
        void onSettings();
        void onNews();
        void onProfile();
        void onLogout();
        void onCollection();
        void onGreenHouse();
        void onShop();
    }

    private final Stage stage;
    private final Skin skin;
    private final Listener listener;
    private Texture dotTexture;
    private Texture backgroundTexture;
    private CurrencyHeader currencyHeader;

    public MainMenuScreen(Listener listener) {
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());

        build();
    }

    private void build() {
        Table root = new Table();
        root.setFillParent(true);
        root.top();

        backgroundTexture = new Texture(
            Gdx.files.internal("IMAGES/mainmenu_background.png")
        );

        Image background = new Image(backgroundTexture);
        background.setFillParent(true);
        background.setScaling(Scaling.fill);

        stage.addActor(background);

        if (background != null) stage.addActor(background);
        stage.addActor(root);

        User currentUser = UserManager.getInstance().getCurrentUser();

        Table topBar = new Table();

        String usernameText = (currentUser != null) ? "User: " + currentUser.getUsername() : "User: Guest";
        Label userLabel = skin.has("big", Label.LabelStyle.class)
            ? new Label(usernameText, skin, "big")
            : new Label(usernameText, skin);

        userLabel.setFontScale(0.6f);
        userLabel.setColor(Color.GOLD);

        topBar.add(userLabel).left().padLeft(20).expandX();

        currencyHeader = new CurrencyHeader();
        topBar.add(currencyHeader).right().padRight(10);

        root.add(topBar).fillX().height(45).pad(10, 0, 0, 0).row();

        Table panel = new Table();
        panel.padTop(100);

        String username = (currentUser != null) ? currentUser.getUsername() : "Player";
        String greetingText = getGreetingMessage(username);

        Label greetingLabel = skin.has("big", Label.LabelStyle.class)
            ? new Label(greetingText, skin, "big")
            : new Label(greetingText, skin);

        greetingLabel.setFontScale(0.85f);
        greetingLabel.setColor(Color.WHITE);
        greetingLabel.setAlignment(Align.center);

        panel.add(greetingLabel).center().padBottom(15).row();

        TextButton playButton = new TextButton("Play", skin, "purple");
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onPlay();
            }
        });
        panel.add(playButton).width(260).height(70).center().padBottom(15).row();

        ImageButton settingsButton = new ImageButton(skin, "settings");
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onSettings();
            }
        });

        Actor profileButton = buildProfileButton();
        Actor collectionButton = buildCollectionButton();
        Actor shopButton = buildShopButton();

        boolean hasUnreadNews = currentUser != null
            && new MainMenu((GameContext) null).shouldShowRedDot(currentUser);
        Actor newsButton = buildNewsButton(hasUnreadNews);

        Table iconRow = new Table();
        iconRow.defaults().pad(6);
        iconRow.add(settingsButton).size(75, 72);
        iconRow.add(profileButton).size(75, 72);
        iconRow.add(collectionButton).size(75, 72);
        iconRow.add(newsButton);
        iconRow.add(shopButton).size(75, 72);
        panel.add(iconRow).padBottom(10).row();

        TextButton exitButton = new TextButton("Exit", skin, "brown");
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Toast.showError(stage, skin, "You must use 'Logout' to leave the main menu.");
            }
        });
        panel.add(exitButton).width(200).padBottom(8).row();

        TextButton logoutButton = new TextButton("Logout", skin, "green_small");
        logoutButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onLogout();
            }
        });
        panel.add(logoutButton).width(130).height(45).padBottom(10).row();

        root.add(panel).grow();

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom();
        Actor greenHouseButton = buildGreenHouseButton();
        bottomTable.add(greenHouseButton).size(260, 220).padBottom(-20).row();
        stage.addActor(bottomTable);
    }

    private String getGreetingMessage(String username) {
        int hour = LocalTime.now().getHour();
        String greeting;

        if (hour >= 5 && hour < 12) {
            greeting = "Good morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good afternoon";
        } else if (hour >= 17 && hour < 23) {
            greeting = "Good evening";
        } else {
            greeting = "Hello Night Owl";
        }

        return greeting + ", " + username + "!";
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

    private Actor buildGreenHouseButton() {
        Texture icon = new Texture(Gdx.files.internal("IMAGES/Menus/GreenHouse/IMAGE_UI_MAINMENU_MM_GREENHOUSE.png"));
        TextureRegion iconRegion = new TextureRegion(icon);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(iconRegion);
        style.pressedOffsetY = -4;

        ImageButton iconButton = new ImageButton(style);
        iconButton.getImageCell().grow();
        iconButton.getImage().setScaling(Scaling.fit);

        iconButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onGreenHouse();
            }
        });
        return iconButton;
    }

    private Actor buildCollectionButton() {
        TextureRegion iconRegion = Textures.regionOrNull("IMAGE_UI_HUD_ALMANACBUTTON_BUTTONS_HUD_ALMANAC_NORMAL");

        if (iconRegion == null) {
            iconRegion = Textures.regionOrNull("IMAGE_UI_MAINMENU_MM_ALMANAC");
        }

        if (iconRegion != null) {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.imageUp = new TextureRegionDrawable(iconRegion);
            style.pressedOffsetY = -4;

            ImageButton iconButton = new ImageButton(style);
            iconButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (listener != null) listener.onCollection();
                }
            });
            return iconButton;
        }

        TextButton collectionButton = new TextButton("Collection", skin, "green_small");
        collectionButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onCollection();
            }
        });
        return collectionButton;
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

    private Actor buildShopButton() {
        TextureRegion iconRegion = Textures.regionOrNull("IMAGE_UI_MAINMENU_MM_SHOP");

        if (iconRegion != null) {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.imageUp = new TextureRegionDrawable(iconRegion);
            style.pressedOffsetY = -4;
            ImageButton iconButton = new ImageButton(style);
            iconButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (listener != null) listener.onShop();
                }
            });
            return iconButton;
        }

        TextButton shopButton = new TextButton("Shop", skin, "green_small");
        shopButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onShop();
            }
        });
        return shopButton;
    }

    private Actor buildNewsIconOrFallback() {
        TextureRegion iconRegion = Textures.regionOrNull("IMAGE_UI_MAINMENU_MM_NEWSICON");

        if (iconRegion != null) {
            ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
            style.imageUp = new TextureRegionDrawable(iconRegion);
            style.pressedOffsetY = -4;
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
        if (dotTexture == null) {
            int size = 16;
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.valueOf("E85D5D"));
            pixmap.fillCircle(size / 2, size / 2, size / 2);
            dotTexture = new Texture(pixmap);
            pixmap.dispose();
        }
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

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (dotTexture != null) {
            dotTexture.dispose();
        }
    }
}
