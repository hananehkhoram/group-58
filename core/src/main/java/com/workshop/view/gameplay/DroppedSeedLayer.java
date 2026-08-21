package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.mechanisms.Tile;
import com.workshop.model.plants.Plant;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class DroppedSeedLayer extends Group {

    public interface SeedPickListener {
        void onSeedPicked(String plantName);
    }

    private final GameContext gameContext;
    private final GameEngine gameEngine;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final SeedPickListener seedPickListener;

    private final PlantAnimationResolver resolver =
        new PlantAnimationResolver();

    private final Map<Tile, Actor> seedActors =
        new HashMap<>();

    public DroppedSeedLayer(
        GameContext gameContext,
        GameEngine gameEngine,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight,
        SeedPickListener seedPickListener
    ) {
        this.gameContext = gameContext;
        this.gameEngine = gameEngine;

        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        this.seedPickListener = seedPickListener;
    }

    @Override
    public void act(float delta) {
        syncDroppedSeeds();
        super.act(delta);
    }

    private void syncDroppedSeeds() {
        int rows =
            gameContext.getLevel().getRows();

        int columns =
            gameContext.getLevel().getColumns();

        for (int row = 0; row < rows; row++) {
            for (int column = 0;
                 column < columns;
                 column++) {

                Tile tile =
                    gameEngine.getTiles(
                        column,
                        row
                    );

                if (tile == null) {
                    continue;
                }

                if (!tile.hasDroppedSeed()) {
                    removeActor(tile);
                    continue;
                }

                if (!seedActors.containsKey(tile)) {
                    createSeedActor(
                        tile,
                        column,
                        row
                    );
                }
            }
        }
    }

    private void createSeedActor(
        Tile tile,
        int column,
        int row
    ) {
        String plantName =
            tile.getDroppedSeed();

        Plant plant;

        try {
            plant =
                gameContext
                    .getPlantFactory()
                    .create(plantName);
        } catch (Exception e) {
            Gdx.app.error(
                "DroppedSeedLayer",
                "Could not create dropped plant: "
                    + plantName,
                e
            );
            return;
        }

        PlantAnimationSpec spec =
            resolver.resolve(
                plant.getName()
            );

        if (spec == null) {
            return;
        }

        float cellWidth =
            gridWidth
                / gameContext
                .getLevel()
                .getColumns();

        float cellHeight =
            gridHeight
                / gameContext
                .getLevel()
                .getRows();

        PlantActor plantActor =
            new PlantActor(
                plant,
                spec,
                Textures.getPamPlayer(),
                cellHeight
            );

        plantActor.setTouchable(
            Touchable.disabled
        );

        Group wrapper =
            new Group();

        float x =
            gridX
                + column * cellWidth;

        float y =
            gridY
                + gridHeight
                - (row + 1) * cellHeight;

        wrapper.setBounds(
            x,
            y,
            cellWidth,
            cellHeight
        );

        plantActor.setPosition(
            cellWidth / 2f,
            cellHeight / 2f
        );

        wrapper.addActor(
            plantActor
        );

        wrapper.addListener(
            new ClickListener() {

                @Override
                public void clicked(
                    InputEvent event,
                    float localX,
                    float localY
                ) {
                    pickSeed(
                        tile,
                        plantName
                    );

                    event.stop();
                }
            }
        );

        seedActors.put(
            tile,
            wrapper
        );

        addActor(wrapper);
    }

    private void pickSeed(
        Tile tile,
        String plantName
    ) {
        if (gameContext.getHeldSeed() != null) {
            return;
        }

        gameContext.setHeldSeed(
            plantName
        );

        tile.clearDroppedSeed();

        removeActor(tile);

        if (seedPickListener != null) {
            Gdx.app.postRunnable(
                () -> seedPickListener.onSeedPicked(
                    plantName
                )
            );
        }
    }

    private void removeActor(
        Tile tile
    ) {
        Actor actor =
            seedActors.remove(tile);

        if (actor != null) {
            actor.remove();
        }
    }
}
