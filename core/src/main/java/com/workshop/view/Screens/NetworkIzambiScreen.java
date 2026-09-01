package com.workshop.view.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.workshop.PvzGame;
import com.workshop.controller.MenuManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.Textures;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.MiniGame.Izambi.Izambi;
import com.workshop.model.MiniGame.Izambi.multiplayer.GameClientMatchTransport;
import com.workshop.model.MiniGame.Izambi.multiplayer.GuestLawnMirror;
import com.workshop.model.MiniGame.Izambi.multiplayer.MatchRole;
import com.workshop.model.MiniGame.Izambi.multiplayer.NetworkIzambiMatch;
import com.workshop.model.plants.Plant;
import com.workshop.net.GameClient;
import com.workshop.net.MatchListener;
import com.workshop.view.Toast;
import com.workshop.view.gameplay.*;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Screen for a networked 2-player "I, Zombie" match, built from the same
 * layers the single-player {@code GamePlayScreen} uses (real backgrounds,
 * PAM plant/zombie animations, brains, red line) so it looks and feels like
 * the real game instead of a placeholder board.
 * <p>
 * The host renders its own live {@link GameContext} directly. The guest has
 * no simulation of its own, so a {@link GuestLawnMirror} turns incoming
 * snapshots into an equivalent (but purely visual) GameContext that the
 * exact same rendering layers can read from. Projectiles are only shown on
 * the host, since the guest's mirror doesn't track them.
 */
public class NetworkIzambiScreen implements Screen {

    private final PvzGame game;
    private final String matchId;
    private final String opponentUsername;
    private final MatchRole localRole;
    private final boolean isHost;

    private final Stage stage;
    private final Skin skin;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final Texture leftTexture;
    private final Texture centerTexture;
    private final Texture rightTexture;
    private final float worldHeight;

    private final NetworkIzambiMatch match;
    private GuestLawnMirror guestMirror;
    private final GameContext displayCtx;

    private final List<PlantCardActor> plantCards = new ArrayList<>();
    private final List<ZombieCardActor> zombieCards = new ArrayList<>();
    private Plant selectedPlant;
    private String selectedZombieType;

    private Label sunLabel;
    private Label timerLabel;

    private boolean endHandled;
    private static final String[] TEXT_REACTIONS = {
        "GG!",
        "Nice move!",
        "Oops!"
    };

    private static final String[] EMOJI_PATHS = {
        "UI/REACTIONS/emoji_0.png",
        "UI/REACTIONS/emoji_1.png",
        "UI/REACTIONS/emoji_2.png"
    };

    private static final String[][] STICKER_PATHS = {

        {
            "UI/REACTIONS/sticker_0/0.png",
            "UI/REACTIONS/sticker_0/1.png",
            "UI/REACTIONS/sticker_0/2.png"
        },

        {
            "UI/REACTIONS/sticker_1/0.png",
            "UI/REACTIONS/sticker_1/1.png",
            "UI/REACTIONS/sticker_1/2.png"
        },

        {
            "UI/REACTIONS/sticker_2/0.png",
            "UI/REACTIONS/sticker_2/1.png",
            "UI/REACTIONS/sticker_2/2.png"
        }
    };

    public NetworkIzambiScreen(
        PvzGame game,
        String matchId,
        String opponentUsername,
        MatchRole localRole,
        boolean isHost
    ) {
        this.game = game;
        this.matchId = matchId;
        this.opponentUsername = opponentUsername;
        this.localRole = localRole;
        this.isHost = isHost;

        skin = PvzSkin.get();

        leftTexture = new Texture(Gdx.files.internal("IMAGES/Menus/MiniGame/IzombieLeft.png"));
        centerTexture = new Texture(Gdx.files.internal("IMAGES/Menus/MiniGame/Izombie.png"));
        rightTexture = new Texture(Gdx.files.internal("IMAGES/Menus/MiniGame/IzombieRight.png"));

        float fullWorldWidth = leftTexture.getWidth() + centerTexture.getWidth() + rightTexture.getWidth();
        worldHeight = centerTexture.getHeight();
        float gameplayWorldWidth = leftTexture.getWidth() + centerTexture.getWidth();

        FitViewport worldViewport = new FitViewport(fullWorldWidth, worldHeight);
        stage = new Stage(worldViewport);

        OrthographicCamera camera = (OrthographicCamera) stage.getCamera();
        camera.position.set(gameplayWorldWidth / 2f, worldHeight / 2f, 0f);
        camera.update();

        buildBackground();

        match = new NetworkIzambiMatch(isHost, localRole, new GameClientMatchTransport(GameClient.get(), matchId));
        if (isHost) {
            match.startHost(new MenuManager(null), 1);
            displayCtx = match.getHostIzambi().getCtx();
        } else {
            guestMirror = new GuestLawnMirror(1);
            displayCtx = guestMirror.getCtx();
        }

        buildLawnLayers();
        buildHud();
        buildUnitBank();
        setupPlacementClick();

        GameClient.get().setMatchListener(new MatchListener() {
            @Override
            public void onMatchMessage(String eventMatchId, String kind, String payload) {
                if (!matchId.equals(eventMatchId)) {
                    return;
                }
                Gdx.app.postRunnable(() -> {
                    switch (kind) {
                        case "STATE" -> match.onRemoteState(payload);
                        case "ACTION" -> match.onRemoteAction(payload);
                        case "END" -> match.onRemoteEnd(payload);
                        case "REACTION" -> handleReaction(payload);
                        default -> {
                        }
                    }
                });
            }

            @Override
            public void onOpponentLeft(String eventMatchId) {
                if (!matchId.equals(eventMatchId)) {
                    return;
                }
                Gdx.app.postRunnable(match::onOpponentLeft);
            }
        });
    }

    private void buildBackground() {
        float leftWidth = leftTexture.getWidth();
        float centerWidth = centerTexture.getWidth();
        float rightWidth = rightTexture.getWidth();

        Image leftBackground = new Image(leftTexture);
        Image centerBackground = new Image(centerTexture);
        Image rightBackground = new Image(rightTexture);

        leftBackground.setBounds(0, 0, leftWidth, worldHeight);
        centerBackground.setBounds(leftWidth, 0, centerWidth, worldHeight);
        rightBackground.setBounds(leftWidth + centerWidth, 0, rightWidth, worldHeight);

        stage.addActor(leftBackground);
        stage.addActor(centerBackground);
        stage.addActor(rightBackground);
    }

    private void buildLawnLayers() {
        stage.addActor(new BowlingRedLineLayer(
            shapeRenderer,
            getGridX() + IZombieManager.RED_LINE_COLUMN * getCellWidth(),
            getGridY(),
            getGridHeight()
        ));

        stage.addActor(new PlantAnimationLayer(displayCtx, getGridX(), getGridY(), getGridWidth(), getGridHeight()));

        if (isHost) {
            // Projectiles only exist as real objects on the host's live
            // context; the guest's mirror doesn't track them.
            stage.addActor(new ProjectileAnimationLayer(
                displayCtx, getGridX(), getGridY(), getGridWidth(), getGridHeight()
            ));
        }

        stage.addActor(new BrainLayer(displayCtx, getGridX(), getGridY(), getGridWidth(), getGridHeight()));
        stage.addActor(new ZombieAnimationLayer(displayCtx, getGridX(), getGridY(), getGridWidth(), getGridHeight()));
        stage.addActor(new ExplosionFxLayer(displayCtx, getGridX(), getGridY(), getGridWidth(), getGridHeight()));
        stage.addActor(new ZombieGibLayer(displayCtx, getGridX(), getGridY(), getGridWidth(), getGridHeight()));
    }

    private void buildHud() {
        Table hud = new Table();
        hud.setFillParent(true);
        hud.top().right();
        hud.padTop(20f).padRight(30f);

        Label roleLabel = new Label("You: " + localRole + "   vs " + opponentUsername, skin);
        sunLabel = new Label("", skin);
        timerLabel = new Label("", skin);

        hud.add(roleLabel).right().row();
        hud.add(sunLabel).right().padTop(6f).row();
        hud.add(timerLabel).right().padTop(6f).row();

        Table reactionTable = new Table();

        for (int i = 0; i < TEXT_REACTIONS.length; i++) {
            final int reactionIndex = i;

            TextButton button = new TextButton(
                TEXT_REACTIONS[i],
                skin,
                "purple"
            );

            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    sendTextReaction(reactionIndex);
                }
            });

            reactionTable.add(button)
                .width(150f)
                .height(45f)
                .padBottom(5f);

            reactionTable.row();
        }

        hud.add(reactionTable)
            .right()
            .padTop(16f)
            .row();

        Table emojiTable = new Table();

        for (int i = 0; i < EMOJI_PATHS.length; i++) {
            final int emojiIndex = i;

            Texture texture = new Texture(
                Gdx.files.internal(EMOJI_PATHS[i])
            );

            TextureRegionDrawable drawable =
                new TextureRegionDrawable(new TextureRegion(texture));

            ImageButton emojiButton = new ImageButton(drawable);

            emojiButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    sendEmojiReaction(emojiIndex);
                }
            });

            emojiTable.add(emojiButton)
                .width(55f)
                .height(55f)
                .pad(4f);
        }

        hud.add(emojiTable)
            .right()
            .padTop(8f)
            .row();

        Table stickerTable = new Table();

        for(int i = 0; i < STICKER_PATHS.length; i++){

            final int stickerIndex = i;

            TextButton button =
                new TextButton(
                    "Sticker " + (i+1),
                    skin,
                    "purple"
                );

            button.addListener(new ChangeListener(){

                @Override
                public void changed(
                    ChangeEvent event,
                    Actor actor
                ){
                    sendStickerReaction(stickerIndex);
                }
            });

            stickerTable.add(button)
                .width(120f)
                .height(45f)
                .pad(4f);
        }

        hud.add(stickerTable)
            .right()
            .padTop(8f)
            .row();

        TextButton forfeitButton = new TextButton("Forfeit", skin, "purple");
        forfeitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                leaveMatch();
            }
        });
        hud.add(forfeitButton).right().padTop(8f).width(160f).row();

        stage.addActor(hud);
    }

    private void buildUnitBank() {
        Table bank = new Table();
        bank.setFillParent(true);
        bank.left().top();
        bank.padLeft(110f).padTop(20f);

        Table cardsTable = new Table();
        cardsTable.top();

        if (localRole == MatchRole.PLANT) {
            PlantFactory plantFactory = new PlantFactory(DataManager.getInstance());
            for (String name : Izambi.getMultiplayerPlantPool()) {
                Plant template;
                try {
                    template = plantFactory.create(name);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

                PlantCardActor card = new PlantCardActor(
                    template, Textures.getPamPlayer(), Textures.getInstance(), skin, PlantCardActor.Mode.SLOT
                );
                card.setOnClick(this::selectPlantCard);
                plantCards.add(card);
                cardsTable.add(card).size(100f, 58f).padBottom(60f).row();
            }
        } else {
            for (Map.Entry<String, Integer> entry : Izambi.getMultiplayerZombieCosts().entrySet()) {
                com.workshop.model.zombie.Zombie template;
                try {
                    template = displayCtx.getZombieFactory().create(entry.getKey());
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

                ZombieCardActor card = new ZombieCardActor(
                    template, entry.getKey(), entry.getValue(), displayCtx.getSeason().getName(),
                    Textures.getPamPlayer(), skin
                );
                card.setOnClick(this::selectZombieCard);
                zombieCards.add(card);
                cardsTable.add(card).size(100f, 58f).padBottom(60f).row();
            }
        }

        bank.add(cardsTable).top().padTop(15f);
        stage.addActor(bank);
    }

    private void selectPlantCard(PlantCardActor clicked) {
        selectedPlant = clicked.getPlant();
        for (PlantCardActor card : plantCards) {
            card.setFocused(card == clicked);
        }
    }

    private void selectZombieCard(ZombieCardActor clicked) {
        selectedZombieType = clicked.getZombieType();
        for (ZombieCardActor card : zombieCards) {
            card.setFocused(card == clicked);
        }
    }

    private void setupPlacementClick() {
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float stageX = event.getStageX();
                float stageY = event.getStageY();

                if (stageX < getGridX() || stageX >= getGridX() + getGridWidth()
                    || stageY < getGridY() || stageY >= getGridY() + getGridHeight()) {
                    return;
                }

                int column = (int) ((stageX - getGridX()) / getCellWidth());
                int row = (int) ((getGridY() + getGridHeight() - stageY) / getCellHeight());

                if (localRole == MatchRole.PLANT) {
                    placeSelectedPlant(row, column);
                } else {
                    placeSelectedZombie(row, column);
                }
            }
        });
    }

    private void placeSelectedPlant(int row, int column) {
        if (selectedPlant == null || match.isEnded()) {
            return;
        }
        boolean placed = match.placeLocal(selectedPlant.getName(), row, column);
        if (placed && isHost) {
            Toast.showSuccess(stage, skin, "Planted " + selectedPlant.getName());
        } else if (!placed) {
            Toast.showError(stage, skin, "Can't plant there.");
        }
    }

    private void placeSelectedZombie(int row, int column) {
        if (selectedZombieType == null || match.isEnded()) {
            return;
        }
        boolean placed = match.placeLocal(selectedZombieType, row, column);
        if (placed && isHost) {
            Toast.showSuccess(stage, skin, selectedZombieType + " placed");
        } else if (!placed) {
            Toast.showError(stage, skin, "Can't place that zombie there.");
        }
    }

    private void leaveMatch() {
        GameClient.get().setMatchListener(null);
        new Thread(() -> GameClient.get().leaveMatch(matchId)).start();
        game.showTravelMenu();
    }

    private float getGridX() {
        return leftTexture.getWidth() + 252;
    }

    private float getGridY() {
        return 80;
    }

    private float getGridWidth() {
        return centerTexture.getWidth() - 285;
    }

    private float getGridHeight() {
        return centerTexture.getHeight() - 278;
    }

    private float getCellWidth() {
        return getGridWidth() / displayCtx.getLevel().getColumns();
    }

    private float getCellHeight() {
        return getGridHeight() / displayCtx.getLevel().getRows();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        match.update(delta);

        if (!isHost) {
            guestMirror.applySnapshot(match.snapshot());
        }

        var snap = match.snapshot();
        if (snap != null) {
            sunLabel.setText("Sun: " + (localRole == MatchRole.PLANT ? snap.plantSun : snap.zombieSun));
            timerLabel.setText("Time: " + snap.remainingSeconds + "s");
        }

        for (PlantCardActor card : plantCards) {
            card.updateAnimation(delta);
        }
        for (ZombieCardActor card : zombieCards) {
            card.updateAnimation(delta);
        }

        if (match.isEnded() && !endHandled) {
            endHandled = true;
            showEndOverlay();
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.getViewport().apply();
        stage.draw();
    }

    private void showEndOverlay() {
        String message = match.localWon() ? "You win!" : "You lose!";

        Table overlay = new Table();
        overlay.setFillParent(true);
        Label label = new Label(message, skin, "big");
        TextButton backButton = new TextButton("Back to Travel Menu", skin, "purple");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameClient.get().setMatchListener(null);
                game.showTravelMenu();
            }
        });
        overlay.add(label).padBottom(16f).row();
        overlay.add(backButton).width(220f);
        stage.addActor(overlay);
    }

    private void sendTextReaction(int index) {
        if (index < 0 || index >= TEXT_REACTIONS.length) {
            return;
        }

        String payload = "TEXT:" + index;

        new Thread(() ->
            GameClient.get().sendMatchReaction(matchId, payload),
            "reaction-sender"
        ).start();
    }

    private void sendEmojiReaction(int index) {
        if (index < 0 || index >= EMOJI_PATHS.length) {
            return;
        }

        String payload = "EMOJI:" + index;

        new Thread(() ->
            GameClient.get().sendMatchReaction(matchId, payload),
            "emoji-reaction-sender"
        ).start();
    }

    private void sendStickerReaction(int index){

        if(index < 0 || index >= STICKER_PATHS.length){
            return;
        }

        String payload =
            "STICKER:" + index;

        new Thread(() ->
            GameClient.get()
                .sendMatchReaction(
                    matchId,
                    payload
                ),
            "sticker-reaction-sender"
        ).start();
    }

    private void handleReaction(String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }

        if (payload.startsWith("TEXT:")) {
            String indexText = payload.substring("TEXT:".length());

            try {
                int index = Integer.parseInt(indexText);

                if (index < 0 || index >= TEXT_REACTIONS.length) {
                    return;
                }

                Toast.showInfo(
                    stage,
                    skin,
                    opponentUsername + ": " + TEXT_REACTIONS[index]
                );
            } catch (NumberFormatException ignored) {
                // Invalid reaction payload.
            }
        }

        if (payload.startsWith("EMOJI:")) {
            String indexText = payload.substring("EMOJI:".length());

            try {
                int index = Integer.parseInt(indexText);

                if (index < 0 || index >= EMOJI_PATHS.length) {
                    return;
                }

                showEmojiReaction(index);
            } catch (NumberFormatException ignored) {
                // Invalid emoji reaction.
            }
        }

        if(payload.startsWith("STICKER:")){

            String indexText =
                payload.substring("STICKER:".length());

            try{

                int index =
                    Integer.parseInt(indexText);

                if(index < 0 ||
                    index >= STICKER_PATHS.length){
                    return;
                }

                showStickerReaction(index);

            }catch(NumberFormatException ignored){

            }
        }
    }

    private void showEmojiReaction(int index) {
        Texture texture = new Texture(
            Gdx.files.internal(EMOJI_PATHS[index])
        );

        Image image = new Image(texture);

        image.setSize(110f, 110f);

        image.setPosition(
            stage.getViewport().getWorldWidth() - 140f,
            stage.getViewport().getWorldHeight() - 150f
        );

        stage.addActor(image);

        image.addAction(
            Actions.sequence(
                Actions.fadeIn(0.15f),
                Actions.delay(2f),
                Actions.fadeOut(0.35f),
                Actions.run(() -> {
                    image.remove();
                    texture.dispose();
                })
            )
        );
    }

    private void showStickerReaction(int index){

        Texture[] frames =
            new Texture[STICKER_PATHS[index].length];


        for(int i = 0; i < frames.length; i++){

            frames[i] =
                new Texture(
                    Gdx.files.internal(
                        STICKER_PATHS[index][i]
                    )
                );
        }


        StickerActor sticker =
            new StickerActor(frames);


        sticker.setSize(
            120,
            120
        );


        sticker.setPosition(
            stage.getViewport().getWorldWidth() - 1000,
            stage.getViewport().getWorldHeight() - 300
        );


        stage.addActor(sticker);


        sticker.addAction(
            Actions.sequence(
                Actions.delay(3f),
                Actions.run(() -> {
                    sticker.remove();
                    sticker.dispose();
                })
            )
        );
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
        shapeRenderer.dispose();
        leftTexture.dispose();
        centerTexture.dispose();
        rightTexture.dispose();
    }
}
