package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.RegisterMenu;
import com.workshop.model.user.SecurityQuestions;
import com.workshop.view.Toast;

import pvz.skin.PvzSkin;

public class RegisterScreen implements Screen {

    public interface Listener {
        void onRegistrationFinished();
        void onSwitchToLogin();
        void onExit();
    }

    private final Stage stage;
    private final Skin skin;
    private final RegisterMenu registerMenu;
    private final Listener listener;

    // step 1 widgets
    private TextField usernameField, passwordField, passwordConfirmField, nicknameField, emailField;
    private SelectBox<String> genderBox;

    // step 2 widgets
    private SelectBox<String> questionBox;
    private TextField answerField, answerConfirmField;

    private Table root;
    private Table step1Table;
    private Table step2Table;

    // persistent inline message (e.g. "account created, pick a question below") —
    // deliberately NOT a Toast, since it needs to stay on screen while step 2 is filled in
    private Label statusLabel;

    public RegisterScreen(Listener listener) {
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());
        // ctx isn't used by RegisterMenu's register()/pickQuestion() logic, null is fine here
        this.registerMenu = new RegisterMenu((GameContext) null);

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
        // TenPatch background from the skin (see pvz-skin README > "TenPatch Drawables For Backgrounds")
        panel.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        // "big"/"default" Label styles in this skin have no fontColor set, which
        // defaults to white -> invisible on the cream panel background. Use
        // "secondary" (explicit DarkBrown fontColor) for anything meant to be read.
        Label title = new Label("Create your account", skin, "big");
        title.setColor(Color.valueOf("5B3A29"));
        panel.add(title).colspan(2).padBottom(16).row();

        statusLabel = new Label("", skin, "secondary");
        statusLabel.setWrap(true);
        statusLabel.setAlignment(Align.center);

        buildStep1(panel);
        buildStep2(panel);
        step2Table.setVisible(false);

        panel.add(statusLabel).colspan(2).width(360).padTop(12).row();

        ScrollPane scrollPane = new ScrollPane(panel, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // vertical only
        stage.setScrollFocus(scrollPane);

        root.add(scrollPane).grow().pad(20);
    }

    private void buildStep1(Table panel) {
        step1Table = new Table();
        step1Table.defaults().pad(4);

        usernameField = new TextField("", skin);
        usernameField.setMessageText("username");

        passwordField = new TextField("", skin);
        passwordField.setMessageText("password");
        passwordField.setPasswordCharacter('*');
        passwordField.setPasswordMode(true);

        passwordConfirmField = new TextField("", skin);
        passwordConfirmField.setMessageText("confirm password");
        passwordConfirmField.setPasswordCharacter('*');
        passwordConfirmField.setPasswordMode(true);

        nicknameField = new TextField("", skin);
        nicknameField.setMessageText("nickname");

        emailField = new TextField("", skin);
        emailField.setMessageText("email");

        genderBox = new SelectBox<>(skin);
        genderBox.setItems("male", "female");

        step1Table.add(new Label("Username", skin, "secondary")).right();
        step1Table.add(usernameField).width(260).row();
        step1Table.add(new Label("Password", skin, "secondary")).right();
        step1Table.add(passwordField).width(260).row();
        step1Table.add(new Label("Confirm password", skin, "secondary")).right();
        step1Table.add(passwordConfirmField).width(260).row();
        step1Table.add(new Label("Nickname", skin, "secondary")).right();
        step1Table.add(nicknameField).width(260).row();
        step1Table.add(new Label("Email", skin, "secondary")).right();
        step1Table.add(emailField).width(260).row();
        step1Table.add(new Label("Gender", skin, "secondary")).right();
        step1Table.add(genderBox).width(260).row();

        TextButton registerButton = new TextButton("Register", skin, "green");
        registerButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                submitStep1();
            }
        });

        TextButton loginLink = new TextButton("Already have an account? Login", skin, "default");
        loginLink.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (listener != null) listener.onSwitchToLogin();
            }
        });

        TextButton exitButton = new TextButton("Exit", skin, "brown");
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (listener != null) listener.onExit();
            }
        });

        step1Table.add(registerButton).colspan(2).padTop(12).width(200).row();
        step1Table.add(loginLink).colspan(2).row();
        step1Table.add(exitButton).colspan(2).padTop(8).width(200).row();

        panel.add(step1Table).row();
    }

    private void buildStep2(Table panel) {
        step2Table = new Table();
        step2Table.defaults().pad(4);

        questionBox = new SelectBox<>(skin);
        String[] questionTexts = new String[SecurityQuestions.values().length];
        for (SecurityQuestions q : SecurityQuestions.values()) {
            questionTexts[q.getId() - 1] = q.getQuestionText();
        }
        questionBox.setItems(questionTexts);

        answerField = new TextField("", skin);
        answerField.setMessageText("answer");

        answerConfirmField = new TextField("", skin);
        answerConfirmField.setMessageText("confirm answer");

        step2Table.add(new Label("Security question", skin, "secondary")).colspan(2).row();
        step2Table.add(questionBox).colspan(2).width(320).row();
        step2Table.add(new Label("Answer", skin, "secondary")).right();
        step2Table.add(answerField).width(260).row();
        step2Table.add(new Label("Confirm answer", skin, "secondary")).right();
        step2Table.add(answerConfirmField).width(260).row();

        TextButton finishButton = new TextButton("Finish", skin, "green");
        finishButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                submitStep2();
            }
        });
        step2Table.add(finishButton).colspan(2).padTop(12).width(200).row();

        panel.add(step2Table).row();
    }

    private void submitStep1() {
        String result = registerMenu.register(
            usernameField.getText(),
            passwordField.getText(),
            passwordConfirmField.getText(),
            nicknameField.getText(),
            emailField.getText(),
            genderBox.getSelected()
        );

        boolean success = result != null && result.startsWith("Please pick a security question");

        if (success) {
            setStatus("Account created. Pick a security question below.", false);
            step1Table.setVisible(false);
            step2Table.setVisible(true);
        } else {
            Toast.showError(stage, skin, result);
        }
    }

    private void submitStep2() {
        int questionId = questionBox.getSelectedIndex() + 1; // ids are 1-based
        String result = registerMenu.pickQuestion(
            questionId,
            answerField.getText(),
            answerConfirmField.getText()
        );

        boolean success = "Registered successfully.".equals(result);
        if (success) {
            Toast.showSuccess(stage, skin, result);
            if (listener != null) listener.onRegistrationFinished();
        } else {
            Toast.showError(stage, skin, result);
        }
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setColor(isError ? Color.SCARLET : Color.valueOf("5B3A29"));
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
