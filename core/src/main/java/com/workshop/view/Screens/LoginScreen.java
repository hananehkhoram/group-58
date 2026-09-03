package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.LoginMenu;

import com.workshop.view.Toast;
import pvz.skin.PvzSkin;


public class LoginScreen implements Screen {

    public interface Listener {
        void onLoginSuccess();
        void onSwitchToRegister();
        void onExit();
    }

    private enum Step { LOGIN, FORGOT_EMAIL, FORGOT_ANSWER, FORGOT_NEW_PASSWORD }

    private final Stage stage;
    private final Skin skin;
    private final LoginMenu loginMenu;
    private final Listener listener;
    private Texture backgroundTexture;

    private Table root;
    private Table loginTable, forgotEmailTable, forgotAnswerTable, forgotNewPasswordTable;
    private Label statusLabel;


    private Cell<Actor> contentCell;

    private TextField usernameField, passwordField;
    private CheckBox stayLoggedInBox;

    private TextField forgotUsernameField, forgotEmailField;
    private Label securityQuestionLabel;
    private TextField answerField;
    private TextField newPasswordField;

    public LoginScreen(Listener listener) {
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());
        this.loginMenu = new LoginMenu((GameContext) null);

        build();
    }

    private void build() {
        buildBackground();
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Table panel = new Table();
        panel.pad(16);
        panel.defaults().pad(3);

        Label title = new Label("Login", skin, "big");
        title.setColor(Color.valueOf("5B3A29"));
        panel.add(title).colspan(2).padBottom(8).row();

        statusLabel = new Label("", skin, "secondary");
        statusLabel.setWrap(true);
        statusLabel.setAlignment(Align.center);

        loginTable = buildLoginStep();
        forgotEmailTable = buildForgotEmailStep();
        forgotAnswerTable = buildForgotAnswerStep();
        forgotNewPasswordTable = buildForgotNewPasswordStep();

        contentCell = panel.add((Actor) loginTable).colspan(2);
        contentCell.row();

        panel.add(statusLabel).colspan(2).width(360).padTop(6).row();

        ScrollPane scrollPane = new ScrollPane(panel, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // vertical only, kicks in only if it truly doesn't fit
        stage.setScrollFocus(scrollPane);

        root.add(scrollPane).grow().pad(10);

        showStep(Step.LOGIN);
    }

    private Table buildLoginStep() {
        Table loginTable = new Table();
        loginTable.defaults().pad(3);

        usernameField = new TextField("", skin);
        usernameField.setMessageText("username");

        passwordField = new TextField("", skin);
        passwordField.setMessageText("password");
        passwordField.setPasswordCharacter('*');
        passwordField.setPasswordMode(true);

        stayLoggedInBox = new CheckBox(" Stay logged in", skin);

        loginTable.add(new Label("Username", skin, "secondary")).right();
        loginTable.add(usernameField).width(260).row();
        loginTable.add(new Label("Password", skin, "secondary")).right();
        loginTable.add(passwordField).width(260).row();
        loginTable.add(stayLoggedInBox).colspan(2).left().row();

        TextButton loginButton = new TextButton("Login", skin, "green");
        loginButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitLogin();
            }
        });

        TextButton forgotLink = new TextButton("Forgot password?", skin, "default");
        forgotLink.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clearStatus();
                showStep(Step.FORGOT_EMAIL);
            }
        });

        TextButton registerLink = new TextButton("Need an account? Register", skin, "default");
        registerLink.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (listener != null) listener.onSwitchToRegister();
            }
        });

        TextButton exitButton = new TextButton("Exit", skin, "brown");
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Matches console MenuExit: EXIT_TARGET(LOGIN) = REGISTER, not app quit.
                if (listener != null) listener.onExit();
            }
        });

        loginTable.add(loginButton).colspan(2).padTop(6).width(200).row();
        loginTable.add(forgotLink).colspan(2).row();
        loginTable.add(registerLink).colspan(2).row();
        loginTable.add(exitButton).colspan(2).padTop(4).width(200).row();

        return loginTable;
    }

    private Table buildForgotEmailStep() {
        Table forgotEmailTable = new Table();
        forgotEmailTable.defaults().pad(3);

        forgotUsernameField = new TextField("", skin);
        forgotUsernameField.setMessageText("username");

        forgotEmailField = new TextField("", skin);
        forgotEmailField.setMessageText("email");

        forgotEmailTable.add(new Label("Username", skin, "secondary")).right();
        forgotEmailTable.add(forgotUsernameField).width(260).row();
        forgotEmailTable.add(new Label("Email", skin, "secondary")).right();
        forgotEmailTable.add(forgotEmailField).width(260).row();

        TextButton submit = new TextButton("Continue", skin, "green");
        submit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitForgotEmail();
            }
        });
        TextButton back = new TextButton("Back to login", skin, "default");
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                clearStatus();
                showStep(Step.LOGIN);
            }
        });

        forgotEmailTable.add(submit).colspan(2).padTop(6).width(200).row();
        forgotEmailTable.add(back).colspan(2).row();

        return forgotEmailTable;
    }

    private Table buildForgotAnswerStep() {
        Table forgotAnswerTable = new Table();
        forgotAnswerTable.defaults().pad(3);

        securityQuestionLabel = new Label("", skin, "secondary");
        securityQuestionLabel.setWrap(true);

        answerField = new TextField("", skin);
        answerField.setMessageText("answer");

        forgotAnswerTable.add(securityQuestionLabel).colspan(2).width(320).padBottom(4).row();
        forgotAnswerTable.add(new Label("Answer", skin, "secondary")).right();
        forgotAnswerTable.add(answerField).width(260).row();

        TextButton submit = new TextButton("Continue", skin, "green");
        submit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitForgotAnswer();
            }
        });
        forgotAnswerTable.add(submit).colspan(2).padTop(6).width(200).row();

        return forgotAnswerTable;
    }

    private Table buildForgotNewPasswordStep() {
        Table forgotNewPasswordTable = new Table();
        forgotNewPasswordTable.defaults().pad(3);

        newPasswordField = new TextField("", skin);
        newPasswordField.setMessageText("new password");
        newPasswordField.setPasswordCharacter('*');
        newPasswordField.setPasswordMode(true);

        forgotNewPasswordTable.add(new Label("New password", skin, "secondary")).right();
        forgotNewPasswordTable.add(newPasswordField).width(260).row();

        TextButton submit = new TextButton("Set new password", skin, "green");
        submit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitNewPassword();
            }
        });
        forgotNewPasswordTable.add(submit).colspan(2).padTop(6).width(200).row();

        return forgotNewPasswordTable;
    }

    private void showStep(Step step) {
        Table table;
        switch (step) {
            case FORGOT_EMAIL: table = forgotEmailTable; break;
            case FORGOT_ANSWER: table = forgotAnswerTable; break;
            case FORGOT_NEW_PASSWORD: table = forgotNewPasswordTable; break;
            default: table = loginTable;
        }
        contentCell.setActor(table);
    }

    private void submitLogin() {
        String stayLoggedIn = stayLoggedInBox.isChecked() ? "-stay-logged-in" : null;
        String result = loginMenu.login(usernameField.getText(), passwordField.getText(), stayLoggedIn);

        boolean success = result != null && result.startsWith("Logged in");
        if (success) {
            if (listener != null) listener.onLoginSuccess();
        } else {
            Toast.showError(stage, skin, result);
        }
    }

    private void submitForgotEmail() {
        String result = loginMenu.startForgetPasswordProcess(
            forgotUsernameField.getText(), forgotEmailField.getText());

        boolean success = result != null && result.startsWith("Security Question:");
        if (!success) {
            Toast.showError(stage, skin, result);
        }

        if (success) {
            String question = result.substring("Security Question: ".length()).split("\n")[0];
            securityQuestionLabel.setText(question);
            showStep(Step.FORGOT_ANSWER);
        }
    }
    private void buildBackground() {
        FileHandle bgFile = Textures.assetsRoot().child("IMAGES/Menus/login/img.png");
        if (!bgFile.exists()) {
            Gdx.app.error("loginScreen", "Background not found at " + bgFile.file().getAbsolutePath());
            return;
        }

        backgroundTexture = new Texture(bgFile);
        Image background = new Image(backgroundTexture);
        background.setScaling(Scaling.fill); // cover the whole screen, cropping overflow instead of distorting
        background.setFillParent(true);
        stage.addActor(background);
    }

    private void submitForgotAnswer() {
        String result = loginMenu.answerSecurityQuestion(answerField.getText());

        boolean success = result != null && result.startsWith("Answer is correct!");
        if (success) {
            showStep(Step.FORGOT_NEW_PASSWORD);
        } else {
            Toast.showError(stage, skin, result);
        }
    }

    private void submitNewPassword() {
        String result = loginMenu.updatePassword(newPasswordField.getText());

        boolean success = "Password changed successfully.".equals(result);
        if (success) {
            Toast.showSuccess(stage, skin, result);
            newPasswordField.setText("");
            showStep(Step.LOGIN);
        } else {
            Toast.showError(stage, skin, result);
        }
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message == null ? "" : message);
        statusLabel.setColor(isError ? Color.SCARLET : Color.valueOf("5B3A29"));
    }

    private void clearStatus() {
        setStatus("", false);
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
