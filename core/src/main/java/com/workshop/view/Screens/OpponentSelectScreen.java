package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.workshop.PvzGame;
import com.workshop.model.MiniGame.Izambi.multiplayer.MatchRole;
import com.workshop.net.GameClient;
import com.workshop.net.MatchListener;
import com.workshop.net.NetResponse;
import pvz.skin.PvzSkin;

public class OpponentSelectScreen implements Screen {

    private final PvzGame game;
    private final Stage stage;
    private final Skin skin;

    private final Label statusLabel;
    private final TextField opponentField;
    private MatchRole preferredRole = MatchRole.PLANT;
    private final TextButton plantRoleButton;
    private final TextButton zombieRoleButton;

    private Table incomingPopup;
    private boolean inQueue;
    private TextButton queueButton;

    public OpponentSelectScreen(PvzGame game) {
        this.game = game;
        skin = PvzSkin.get();
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label title = new Label("I, Zombie - Choose an Opponent", skin);
        root.add(title).padBottom(20).row();

        opponentField = new TextField("", skin);
        opponentField.setMessageText("Opponent username");
        root.add(opponentField).width(260).padBottom(8).row();

        Table roleRow = new Table();
        plantRoleButton = new TextButton("Play as Plant", skin, "purple");
        zombieRoleButton = new TextButton("Play as Zombie", skin, "purple");
        plantRoleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                preferredRole = MatchRole.PLANT;
                statusLabel.setText("Role: Plant");
            }
        });
        zombieRoleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                preferredRole = MatchRole.ZOMBIE;
                statusLabel.setText("Role: Zombie");
            }
        });
        roleRow.add(plantRoleButton).padRight(8);
        roleRow.add(zombieRoleButton);
        root.add(roleRow).padBottom(12).row();

        TextButton challengeButton = new TextButton("Challenge", skin, "purple");
        challengeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                challengeOpponent();
            }
        });
        root.add(challengeButton).width(220).padBottom(12).row();

        queueButton = new TextButton("Find Random Opponent", skin, "purple");
        queueButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleQueue();
            }
        });
        root.add(queueButton).width(220).padBottom(12).row();

        TextButton couchButton = new TextButton("Couch Play (this device)", skin, "purple");
        couchButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showCouchIzambi();
            }
        });
        root.add(couchButton).width(220).padBottom(20).row();

        TextButton backButton = new TextButton("Back", skin, "purple");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameClient.get().setMatchListener(null);
                game.showTravelMenu();
            }
        });
        root.add(backButton).width(150).padBottom(12).row();

        statusLabel = new Label("Role: Plant", skin);
        root.add(statusLabel);

        GameClient.get().setMatchListener(new MatchListener() {
            @Override
            public void onChallengeInvite(String fromUsername, String fromRole) {
                Gdx.app.postRunnable(() -> showIncomingChallenge(fromUsername, fromRole));
            }

            @Override
            public void onChallengeDeclined(String byUsername) {
                Gdx.app.postRunnable(() -> statusLabel.setText(byUsername + " declined your challenge."));
            }

            @Override
            public void onMatchFound(String matchId, String opponentUsername, String yourRole, boolean isHost) {
                Gdx.app.postRunnable(() -> {
                    GameClient.get().setMatchListener(null);
                    game.showNetworkIzambiMatch(matchId, opponentUsername, MatchRole.fromWire(yourRole), isHost);
                });
            }
        });
    }

    private void challengeOpponent() {
        String target = opponentField.getText().trim();
        if (target.isEmpty()) {
            statusLabel.setText("Enter a username first.");
            return;
        }
        statusLabel.setText("Challenging " + target + "...");
        String role = preferredRole.name();
        runAsync(() -> {
            NetResponse response = GameClient.get().challenge(target, role);
            Gdx.app.postRunnable(() -> statusLabel.setText(
                response.ok ? "Waiting for " + target + " to respond..." : response.message
            ));
        });
    }

    private void toggleQueue() {
        if (inQueue) {
            runAsync(() -> GameClient.get().cancelRandomQueue());
            inQueue = false;
            queueButton.setText("Find Random Opponent");
            statusLabel.setText("Search cancelled.");
            return;
        }
        inQueue = true;
        queueButton.setText("Cancel Search");
        statusLabel.setText("Searching for an opponent...");
        runAsync(() -> {
            NetResponse response = GameClient.get().joinRandomQueue();
            if (!response.ok) {
                Gdx.app.postRunnable(() -> {
                    inQueue = false;
                    queueButton.setText("Find Random Opponent");
                    statusLabel.setText(response.message);
                });
            }
        });
    }

    private void showIncomingChallenge(String fromUsername, String fromRole) {
        if (incomingPopup != null) {
            incomingPopup.remove();
        }
        incomingPopup = new Table();
        incomingPopup.setFillParent(true);
        Label label = new Label(
            fromUsername + " challenged you (they play " + fromRole + "). Accept?",
            skin
        );
        TextButton accept = new TextButton("Accept", skin, "purple");
        TextButton decline = new TextButton("Decline", skin, "purple");
        accept.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                incomingPopup.remove();
                incomingPopup = null;
                runAsync(() -> GameClient.get().respondToChallenge(fromUsername, true));
            }
        });
        decline.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                incomingPopup.remove();
                incomingPopup = null;
                runAsync(() -> GameClient.get().respondToChallenge(fromUsername, false));
            }
        });
        Table buttons = new Table();
        buttons.add(accept).padRight(10);
        buttons.add(decline);
        incomingPopup.add(label).padBottom(10).row();
        incomingPopup.add(buttons);
        stage.addActor(incomingPopup);
    }

    private void runAsync(Runnable task) {
        Thread thread = new Thread(task, "izambi-net-call");
        thread.setDaemon(true);
        thread.start();
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
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
