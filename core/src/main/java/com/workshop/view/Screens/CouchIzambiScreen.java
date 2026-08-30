package com.workshop.view.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.workshop.PvzGame;
import com.workshop.model.MiniGame.Izambi.multiplayer.MatchRole;
import com.workshop.controller.MenuManager;
import com.workshop.controller.commands.Planting;
import com.workshop.controller.repository.Textures;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.controller.SpecialLevelManager.ConveyorBeltManager;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.MiniGame.Izambi.Izambi;
import com.workshop.model.MiniGame.Izambi.multiplayer.CouchIzambiMatch;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Toast;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;

public class CouchIzambiScreen extends GamePlayScreen {

    private static final int[] ROW_KEYS = {
        Input.Keys.Q, Input.Keys.W, Input.Keys.E, Input.Keys.R, Input.Keys.T
    };
    private static final int[] UNIT_KEYS = {
        Input.Keys.NUM_1, Input.Keys.NUM_2, Input.Keys.NUM_3, Input.Keys.NUM_4, Input.Keys.NUM_5
    };

    private final CouchIzambiMatch match;
    private final String[] zombiePool;
    private int selectedZombieIndex = 0;

    private Plant selectedPlantForPlacement = null;
    private Planting plantingCommand;

    private Stage hudStage;
    private Skin skin;
    private Label sunLabel;

    private boolean matchEndShown = false;

    private final List<PlantCardActor> seedBankCards = new ArrayList<>();
    private final List<ZombieCardActor> zombieBankCards = new ArrayList<>();

    private GameContext gameContext;

    public CouchIzambiScreen(PvzGame game, CouchIzambiMatch match, GameContext ctx, Runnable onRestart, Runnable onExit) {
        super(ctx, onRestart, onExit);
        this.match = match;
        this.gameContext = ctx;

        MenuManager plantingMenuManager = new MenuManager(gameContext);
        plantingMenuManager.setGameEngine(gameContext.getGameEngine());
        this.plantingCommand = new Planting(plantingMenuManager);

        IZombieManager manager = new IZombieManager(0, 5);
        List<String> keys = new ArrayList<>(manager.getAvailableZombieCosts().keySet());
        this.zombiePool = keys.stream().limit(5).toArray(String[]::new);

        initHUD();
    }

    private void initHUD() {
        hudStage = new Stage(new FitViewport(1024, 576));
        skin = PvzSkin.get();

        sunLabel = new Label("", skin);
        sunLabel.setPosition(320, 545);
        hudStage.addActor(sunLabel);

        Label guideLabel = new Label("Plants: Click Card -> Click Grid | Zombies: 1-5 Select, Q/W/E/R/T Row", skin);
        guideLabel.setPosition(320, 525);
        hudStage.addActor(guideLabel);

        buildPlantSeedBank();
        buildZombieBank();
    }

    @Override
    protected boolean suppressDefaultUI() {
        return true;
    }

    @Override
    protected boolean handlesOwnEndOfGame() {
        return true;
    }

    private void buildPlantSeedBank() {
        Table seedBankTable = new Table();
        seedBankTable.setFillParent(true);
        seedBankTable.left().top();
        seedBankTable.padLeft(10f);
        seedBankTable.padTop(20f);

        Table seedBankPanel = new Table();
        seedBankPanel.top();

        Table cardsTable = new Table();
        cardsTable.top();

        seedBankCards.clear();

        PlantFactory factory = gameContext.getPlantFactory();
        for (String plantName : Izambi.getMultiplayerPlantPool()) {
            Plant plant;
            try {
                plant = factory.create(plantName);
            } catch (Exception e) {
                continue;
            }

            PlantCardActor card = new PlantCardActor(
                plant,
                Textures.getPamPlayer(),
                Textures.getInstance(),
                skin,
                PlantCardActor.Mode.SLOT
            );

            card.setBoosted(plant.isPlantFoodActive());
            seedBankCards.add(card);

            card.setOnClick(clickedCard -> {
                if (gameContext.isOnCooldown(clickedCard.getPlant().getName())) {
                    showPlantCooldownError(clickedCard.getPlant().getName());
                    return;
                }

                this.selectedPlantForPlacement = clickedCard.getPlant();
                showPlantOnMouse(this.selectedPlantForPlacement);
                for (PlantCardActor c : seedBankCards) {
                    c.setFocused(c == clickedCard);
                }
            });

            cardsTable.add(card)
                .size(100f, 58f)
                .padBottom(10f)
                .row();
        }

        seedBankPanel.add(cardsTable).top();
        seedBankTable.add(seedBankPanel).width(115f).top();

        hudStage.addActor(seedBankTable);
    }

    private void buildZombieBank() {
        Table zombieBankTable = new Table();
        zombieBankTable.setFillParent(true);
        zombieBankTable.right().top();
        zombieBankTable.padRight(10f);
        zombieBankTable.padTop(20f);

        Table panel = new Table();
        panel.top();

        Table cardsTable = new Table();
        cardsTable.top();

        zombieBankCards.clear();

        IZombieManager manager = new IZombieManager(0, 5);
        int count = 0;
        for (var entry : manager.getAvailableZombieCosts().entrySet()) {
            if (count >= 5) break;

            Zombie zombie;
            try {
                zombie = gameContext.getZombieFactory().create(entry.getKey());
            } catch (IllegalArgumentException exception) {
                continue;
            }

            ZombieCardActor card = new ZombieCardActor(
                zombie,
                entry.getKey(),
                entry.getValue(),
                gameContext.getSeason().getName(),
                Textures.getPamPlayer(),
                skin
            );

            zombieBankCards.add(card);

            card.setOnClick(zombieCard -> {
                for (int i = 0; i < zombiePool.length; i++) {
                    if (zombiePool[i].equals(zombieCard.getZombieType())) {
                        selectedZombieIndex = i;
                        updateZombieSelectionVisuals();
                        break;
                    }
                }
            });

            cardsTable.add(card)
                .size(100f, 58f)
                .padBottom(10f)
                .row();

            count++;
        }

        panel.add(cardsTable).top();
        zombieBankTable.add(panel).width(115f).top();

        hudStage.addActor(zombieBankTable);
        updateZombieSelectionVisuals();
    }

    private void updateZombieSelectionVisuals() {
        for (int i = 0; i < zombieBankCards.size(); i++) {
            ZombieCardActor card = zombieBankCards.get(i);
            boolean isSelected = (i == selectedZombieIndex % Math.max(1, zombieBankCards.size()));
            card.setFocused(isSelected);
        }
    }

    @Override
    public void showPlantCooldownError(String plantName) {
        int seconds = (int) Math.ceil(gameContext.getRemainingCooldownSeconds(plantName));
        String message = seconds > 0
            ? plantName + " is still recharging (" + seconds + "s)"
            : plantName + " is still recharging";
        Toast.showError(hudStage, PvzSkin.get(), message);
    }

    private void plantSelectedPlant(int column, int row) {
        if (selectedPlantForPlacement == null) {
            return;
        }

        String plantName = selectedPlantForPlacement.getName();

        if (gameContext.isOnCooldown(plantName)) {
            showPlantCooldownError(plantName);
            return;
        }

        if (match.getPlantSun() < selectedPlantForPlacement.getSunCost()) {
            Toast.showError(hudStage, PvzSkin.get(), "Not enough sun!");
            return;
        }

        boolean placed = match.placePlant(plantName, row, column);

        if (placed) {
            Plant placedPlant = gameContext.getPlantGrid()[row][column];
            if (placedPlant != null) {
                int currentSecond = gameContext.getTimeManager().getTotalSeconds();
                placedPlant.setPlantTimeSecond(currentSecond);
            }

            selectedPlantForPlacement = null;
            for (PlantCardActor c : seedBankCards) {
                c.setFocused(false);
            }
            clearPlantPreview();
            return;
        }

        Toast.showError(hudStage, PvzSkin.get(), "Can't plant there.");
    }
    @Override
    public void show() {
        super.show();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (selectedPlantForPlacement == null) {
                    return false;
                }

                Vector2 stageCoords = new Vector2(screenX, screenY);
                getStage().screenToStageCoordinates(stageCoords);

                float stageX = stageCoords.x;
                float stageY = stageCoords.y;

                if (stageX < getGridX() || stageX >= getGridX() + getGridWidth()
                    || stageY < getGridY() || stageY >= getGridY() + getGridHeight()) {
                    return false;
                }

                int col = (int) ((stageX - getGridX()) / getCellWidth());
                int row = (int) ((getGridY() + getGridHeight() - stageY) / getCellHeight());

                if (row >= 0 && row < gameContext.getLevel().getRows()
                    && col >= 0 && col < gameContext.getLevel().getColumns()) {
                    plantSelectedPlant(col, row);
                    return true;
                }

                return false;
            }
        });

        if (Gdx.input.getInputProcessor() != null) {
            multiplexer.addProcessor(Gdx.input.getInputProcessor());
        }

        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                for (int i = 0; i < UNIT_KEYS.length; i++) {
                    if (keycode == UNIT_KEYS[i]) {
                        if (i < zombiePool.length) {
                            selectedZombieIndex = i;
                            updateZombieSelectionVisuals();
                        }
                        return true;
                    }
                }
                for (int row = 0; row < ROW_KEYS.length; row++) {
                    if (keycode == ROW_KEYS[row]) {
                        if (!match.isEnded() && zombiePool.length > 0) {
                            String zombieName = zombiePool[selectedZombieIndex % zombiePool.length];
                            match.placeZombie(zombieName, row, 8);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        match.update(delta);
        super.render(delta);
        updatePlantMousePreview();

        if (match.isEnded() && !matchEndShown) {
            matchEndShown = true;

            selectedPlantForPlacement = null;
            clearPlantPreview();
            for (PlantCardActor c : seedBankCards) {
                c.setFocused(false);
            }

            if (match.getWinner() == MatchRole.PLANT) {
                getWinLoseOverlay().showWin();
            } else {
                getWinLoseOverlay().showLose();
            }
        }

        for (PlantCardActor card : seedBankCards) {
            card.updateAnimation(delta);
        }

        for (ZombieCardActor card : zombieBankCards) {
            card.updateAnimation(delta);
        }

        int plantSun = match.getPlantSun();
        int zombieSun = (match.getIzambi() != null && match.getIzambi().getCtx() != null)
            ? match.getIzambi().getCtx().getSunAmount()
            : 0;

        sunLabel.setText("Plant Sun: " + plantSun + "  |  Zombie Sun: " + zombieSun + "  |  Time: " + match.remainingSeconds() + "s");

        hudStage.act(delta);
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (hudStage != null) {
            hudStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (hudStage != null) {
            hudStage.dispose();
        }
    }
}
