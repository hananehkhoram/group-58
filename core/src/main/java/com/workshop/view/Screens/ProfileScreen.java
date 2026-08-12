package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Scaling;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.ProfileMenu;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Toast;

import pvz.skin.PvzSkin;


public class ProfileScreen implements Screen {

    public interface Listener {
        void onBack();
    }

    private final Stage stage;
    private final Skin skin;
    private final ProfileMenu profileMenu;
    private final Listener listener;
    private Texture backgroundTexture;

    private Label infoLabel;

    private TextField usernameField;
    private TextField nicknameField;
    private TextField emailField;
    private TextField oldPasswordField, newPasswordField;

    public ProfileScreen(Listener listener) {
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());
        this.profileMenu = new ProfileMenu((GameContext) null);

        build();
    }

    private void build() {
        buildBackground();

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Table panel = new Table();
        panel.pad(30);
        panel.defaults().pad(6);
        //panel.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        Label title = new Label("Profile", skin, "big");
        title.setColor(Color.valueOf("5B3A29")); // "big"/"default" styles default to white, invisible on the cream panel
        panel.add(title).colspan(3).padBottom(16).row();

        buildInfoSection(panel);
        buildUsernameSection(panel);
        buildNicknameSection(panel);
        buildEmailSection(panel);
        buildPasswordSection(panel);

        TextButton backButton = new TextButton("Back", skin, "brown");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onBack();
            }
        });
        panel.add(backButton).colspan(3).padTop(16).width(200).row();

        ScrollPane scrollPane = new ScrollPane(panel, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        stage.setScrollFocus(scrollPane);

        root.add(scrollPane).grow().pad(20);
    }

    private void buildBackground() {
        FileHandle bgFile = Textures.assetsRoot().child("IMAGES/Menus/profile/img.png");
        if (!bgFile.exists()) {
            Gdx.app.error("ProfileScreen", "Background not found at " + bgFile.file().getAbsolutePath());
            return;
        }

        backgroundTexture = new Texture(bgFile);
        Image background = new Image(backgroundTexture);
        background.setScaling(Scaling.fill); // cover the whole screen, cropping overflow instead of distorting
        background.setFillParent(true);
        stage.addActor(background);
    }

    private void buildInfoSection(Table panel) {
        infoLabel = new Label("", skin, "secondary");
        infoLabel.setAlignment(Align.left);
        infoLabel.setWrap(true);
        refreshInfo();
        panel.add(infoLabel).colspan(3).width(360).padBottom(16).row();
    }

    private void refreshInfo() {
        infoLabel.setText(profileMenu.showInfo());
    }

    private void buildUsernameSection(Table panel) {
        User currentUser = UserManager.getInstance().getCurrentUser();
        usernameField = new TextField("", skin);
        usernameField.setMessageText(currentUser != null ? currentUser.getUsername() : "username");

        TextButton save = new TextButton("Save", skin, "green_small");
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String result = profileMenu.changeUsername(usernameField.getText());
                handleResult(result, "successfully changed");
            }
        });

        panel.add(new Label("Username", skin, "secondary")).right();
        panel.add(usernameField).width(220);
        panel.add(save).width(90).row();
    }

    private void buildNicknameSection(Table panel) {
        User currentUser = UserManager.getInstance().getCurrentUser();
        nicknameField = new TextField("", skin);
        nicknameField.setMessageText(currentUser != null ? currentUser.getNickName() : "nickname");

        TextButton save = new TextButton("Save", skin, "green_small");
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String result = profileMenu.changeNickname(nicknameField.getText());
                handleResult(result, "successfully changed");
            }
        });

        panel.add(new Label("Nickname", skin, "secondary")).right();
        panel.add(nicknameField).width(220);
        panel.add(save).width(90).row();
    }

    private void buildEmailSection(Table panel) {
        User currentUser = UserManager.getInstance().getCurrentUser();
        emailField = new TextField("", skin);
        emailField.setMessageText(currentUser != null ? currentUser.getEmail() : "email");

        TextButton save = new TextButton("Save", skin, "green_small");
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String result = profileMenu.changeEmail(emailField.getText());
                handleResult(result, "successfully changed");
            }
        });

        panel.add(new Label("Email", skin, "secondary")).right();
        panel.add(emailField).width(220);
        panel.add(save).width(90).row();
    }

    private void buildPasswordSection(Table panel) {
        oldPasswordField = new TextField("", skin);
        oldPasswordField.setMessageText("current password");
        oldPasswordField.setPasswordCharacter('*');
        oldPasswordField.setPasswordMode(true);

        newPasswordField = new TextField("", skin);
        newPasswordField.setMessageText("new password");
        newPasswordField.setPasswordCharacter('*');
        newPasswordField.setPasswordMode(true);

        TextButton save = new TextButton("Save", skin, "green_small");
        save.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String result = profileMenu.changePassword(oldPasswordField.getText(), newPasswordField.getText());
                boolean success = "Password successfully changed.".equals(result);
                if (success) {
                    Toast.showSuccess(stage, skin, result);
                    oldPasswordField.setText("");
                    newPasswordField.setText("");
                } else {
                    Toast.showError(stage, skin, result);
                }
            }
        });

        panel.add(new Label("Current password", skin, "secondary")).right();
        panel.add(oldPasswordField).width(220).row();
        panel.add(new Label("New password", skin, "secondary")).right();
        panel.add(newPasswordField).width(220);
        panel.add(save).width(90).row();
    }

    private void handleResult(String result, String successSubstring) {
        boolean success = result != null && result.contains(successSubstring);
        if (success) {
            Toast.showSuccess(stage, skin, result);
            refreshInfo();
        } else {
            Toast.showError(stage, skin, result);
        }
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
    }
}
