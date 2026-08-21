package com.workshop.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import pvz.libpvz.pam.PamPlayer;

public final class PlantAnimationLayer extends Group {

    private final GameContext gameContext;
    private final PlantAnimationResolver resolver;
    private final PamPlayer pamPlayer;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Map<Plant, PlantActor> plantActors =
        new IdentityHashMap<>();

    public PlantAnimationLayer(
        GameContext gameContext,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;

        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        resolver = new PlantAnimationResolver();
        pamPlayer = Textures.getPamPlayer();
    }

    @Override
    public void act(float delta) {
        syncPlantActors();
        super.act(delta);
    }

    private void syncPlantActors() {
        Plant[][] plantGrid = gameContext.getPlantGrid();

        Set<Plant> activePlants =
            Collections.newSetFromMap(new IdentityHashMap<>());

        for (int row = 0; row < plantGrid.length; row++) {
            for (int column = 0;
                 column < plantGrid[row].length;
                 column++) {

                Plant plant = plantGrid[row][column];

                if (plant == null || plant.isDead()) {
                    continue;
                }

                activePlants.add(plant);

                PlantActor actor = plantActors.get(plant);

                if (actor == null) {
                    actor = createPlantActor(plant);

                    if (actor == null) {
                        continue;
                    }

                    plantActors.put(plant, actor);
                    addActor(actor);
                }

                actor.setPosition(
                    getCellCenterX(column),
                    getCellCenterY(row)
                );
                actor.setZIndex(row);
            }
        }

        removeMissingPlants(activePlants);
    }

    private PlantActor createPlantActor(Plant plant) {
        PlantAnimationSpec spec =
            resolver.resolve(plant.getName());

        if (spec == null) {
            return null;
        }

        return new PlantActor(
            plant,
            spec,
            pamPlayer,
            getCellHeight()
        );
    }

    private void removeMissingPlants(Set<Plant> activePlants) {
        Iterator<Map.Entry<Plant, PlantActor>> iterator =
            plantActors.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Plant, PlantActor> entry = iterator.next();

            if (activePlants.contains(entry.getKey())) {
                continue;
            }

            entry.getValue().remove();
            iterator.remove();
        }
    }

    private float getCellHeight() {
        return gridHeight / gameContext.getLevel().getRows();
    }

    private float getCellCenterX(int column) {
        float cellWidth =
            gridWidth / gameContext.getLevel().getColumns();

        return gridX
            + column * cellWidth
            + cellWidth / 2f;
    }

    private float getCellCenterY(int row) {
        float cellHeight =
            gridHeight / gameContext.getLevel().getRows();

        return gridY
            + gridHeight
            - row * cellHeight
            - cellHeight / 2f;
    }
}
