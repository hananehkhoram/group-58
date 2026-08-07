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

import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.LoginMenu;

import com.workshop.view.Toast;
import pvz.skin.PvzSkin;


public class LoginScreen implements Screen {

    public interface Listener {
        void onLoginSuccess();
        void onSwitchToRegister();
    }

    private enum Step { LOGIN, FORGOT_EMAIL, FORGOT_ANSWER, FORGOT_NEW_PASSWORD }

    private final Stage stage;
    private final Skin skin;
    private final LoginMenu loginMenu;
    private final Listener listener;

    private Table root;
    private Table loginTable, forgotEmailTable, forgotAnswerTable, forgotNewPasswordTable;
    private Label statusLabel;

    // login step
    private TextField usernameField, passwordField;
    private CheckBox stayLoggedInBox;

    // forgot-password steps
    private TextField forgotUsernameField, forgotEmailField;
    private Label securityQuestionLabel;
    private TextField answerField;
    private TextField newPasswordField;

    private Step currentStep = Step.LOGIN;

    public LoginScreen(Listener listener) {
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());
        this.loginMenu = new LoginMenu((GameContext) null);

        build();
    }

    private void build() {
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Table panel = new Table();
        panel.pad(30);
        panel.defaults().pad(6);
        panel.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        Label title = new Label("Login", skin, "big");
        panel.add(title).colspan(2).padBottom(16).row();

        statusLabel = new Label("", skin, "secondary");
        statusLabel.setWrap(true);
        statusLabel.setAlignment(Align.center);

        buildLoginStep(panel);
        buildForgotEmailStep(panel);
        buildForgotAnswerStep(panel);
        buildForgotNewPasswordStep(panel);

        panel.add(statusLabel).colspan(2).width(360).padTop(12).row();

        root.add(panel);

        showStep(Step.LOGIN);
    }

    private void buildLoginStep(Table panel) {
        loginTable = new Table();
        loginTable.defaults().pad(4);

        usernameField = new TextField("", skin);
        usernameField.setMessageText("username");

        passwordField = new TextField("", skin);
        passwordField.setMessageText("password");
        passwordField.setPasswordCharacter('*');
        passwordField.setPasswordMode(true);

        stayLoggedInBox = new CheckBox(" Stay logged in", skin);

        loginTable.add(new Label("Username", skin)).right();
        loginTable.add(usernameField).width(260).row();
        loginTable.add(new Label("Password", skin)).right();
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

        loginTable.add(loginButton).colspan(2).padTop(12).width(200).row();
        loginTable.add(forgotLink).colspan(2).row();
        loginTable.add(registerLink).colspan(2).row();

        panel.add(loginTable).row();
    }

    private void buildForgotEmailStep(Table panel) {
        forgotEmailTable = new Table();
        forgotEmailTable.defaults().pad(4);

        forgotUsernameField = new TextField("", skin);
        forgotUsernameField.setMessageText("username");

        forgotEmailField = new TextField("", skin);
        forgotEmailField.setMessageText("email");

        forgotEmailTable.add(new Label("Username", skin)).right();
        forgotEmailTable.add(forgotUsernameField).width(260).row();
        forgotEmailTable.add(new Label("Email", skin)).right();
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

        forgotEmailTable.add(submit).colspan(2).padTop(12).width(200).row();
        forgotEmailTable.add(back).colspan(2).row();

        panel.add(forgotEmailTable).row();
    }

    private void buildForgotAnswerStep(Table panel) {
        forgotAnswerTable = new Table();
        forgotAnswerTable.defaults().pad(4);

        securityQuestionLabel = new Label("", skin, "secondary");
        securityQuestionLabel.setWrap(true);

        answerField = new TextField("", skin);
        answerField.setMessageText("answer");

        forgotAnswerTable.add(securityQuestionLabel).colspan(2).width(320).padBottom(8).row();
        forgotAnswerTable.add(new Label("Answer", skin)).right();
        forgotAnswerTable.add(answerField).width(260).row();

        TextButton submit = new TextButton("Continue", skin, "green");
        submit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitForgotAnswer();
            }
        });
        forgotAnswerTable.add(submit).colspan(2).padTop(12).width(200).row();

        panel.add(forgotAnswerTable).row();
    }

    private void buildForgotNewPasswordStep(Table panel) {
        forgotNewPasswordTable = new Table();
        forgotNewPasswordTable.defaults().pad(4);

        newPasswordField = new TextField("", skin);
        newPasswordField.setMessageText("new password");
        newPasswordField.setPasswordCharacter('*');
        newPasswordField.setPasswordMode(true);

        forgotNewPasswordTable.add(new Label("New password", skin)).right();
        forgotNewPasswordTable.add(newPasswordField).width(260).row();

        TextButton submit = new TextButton("Set new password", skin, "green");
        submit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                submitNewPassword();
            }
        });
        forgotNewPasswordTable.add(submit).colspan(2).padTop(12).width(200).row();

        panel.add(forgotNewPasswordTable).row();
    }

    private void showStep(Step step) {
        currentStep = step;
        loginTable.setVisible(step == Step.LOGIN);
        forgotEmailTable.setVisible(step == Step.FORGOT_EMAIL);
        forgotAnswerTable.setVisible(step == Step.FORGOT_ANSWER);
        forgotNewPasswordTable.setVisible(step == Step.FORGOT_NEW_PASSWORD);
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
        statusLabel.setColor(isError ? Color.SCARLET : Color.WHITE);
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
    }
}
