package com.workshop.view.Screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
    import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


import com.workshop.model.GameContext;
import com.workshop.model.menus.allmenus.RegisterMenu;
import com.workshop.model.user.SecurityQuestions;
import pvz.skin.PvzSkin;

/**
 * Visual (Scene2D) version of the console "register" / "pick question" commands.
 * Talks directly to {@link RegisterMenu} — the same class the console commands
 * {@code NewUser} and {@code PickAQuestion} already use — so all validation
 * (username/password rules, matching passwords, security questions, ...) stays
 * in one place.
 */
public class RegisterScreen implements Screen {

    /** Hook this up to whatever screen-switching mechanism your Game class uses. */
    public interface Listener {
        void onRegistrationFinished();
        void onSwitchToLogin();
    }

    private final Stage stage;
    private final Skin skin;
    private final RegisterMenu registerMenu;
    private final Listener listener;

    private TextField usernameField, passwordField, passwordConfirmField, nicknameField, emailField;
    private SelectBox<String> genderBox;
    private Label statusLabel;

    private SelectBox<String> questionBox;
    private TextField answerField, answerConfirmField;

    private Table root;
    private Table step1Table;
    private Table step2Table;

    public RegisterScreen(Listener listener) {
        this.listener = listener;
        this.skin = PvzSkin.get();
        this.stage = new Stage(new ScreenViewport());
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
        panel.setBackground(skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10"));

        Label title = new Label("Create your account", skin, "big");
        panel.add(title).colspan(2).padBottom(16).row();

        statusLabel = new Label("", skin, "secondary");
        statusLabel.setWrap(true);
        statusLabel.setAlignment(Align.center);

        buildStep1(panel);
        buildStep2(panel);
        step2Table.setVisible(false);

        panel.add(statusLabel).colspan(2).width(360).padTop(12).row();

        root.add(panel);
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

        step1Table.add(new Label("Username", skin)).right();
        step1Table.add(usernameField).width(260).row();
        step1Table.add(new Label("Password", skin)).right();
        step1Table.add(passwordField).width(260).row();
        step1Table.add(new Label("Confirm password", skin)).right();
        step1Table.add(passwordConfirmField).width(260).row();
        step1Table.add(new Label("Nickname", skin)).right();
        step1Table.add(nicknameField).width(260).row();
        step1Table.add(new Label("Email", skin)).right();
        step1Table.add(emailField).width(260).row();
        step1Table.add(new Label("Gender", skin)).right();
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

        step1Table.add(registerButton).colspan(2).padTop(12).width(200).row();
        step1Table.add(loginLink).colspan(2).row();

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

        step2Table.add(new Label("Security question", skin)).colspan(2).row();
        step2Table.add(questionBox).colspan(2).width(320).row();
        step2Table.add(new Label("Answer", skin)).right();
        step2Table.add(answerField).width(260).row();
        step2Table.add(new Label("Confirm answer", skin)).right();
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
            setStatus(result, true);
        }
    }

    private void submitStep2() {
        int questionId = questionBox.getSelectedIndex() + 1;
        String result = registerMenu.pickQuestion(
            questionId,
            answerField.getText(),
            answerConfirmField.getText()
        );

        boolean success = "Registered successfully.".equals(result);
        setStatus(result, !success);

        if (success && listener != null) {
            listener.onRegistrationFinished();
        }
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setColor(isError ? Color.SCARLET : Color.WHITE);
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
