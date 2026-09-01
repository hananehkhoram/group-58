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
        playPendingAttackAnimations();
        super.act(delta);
        releaseSyncedShots();
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

                if (plant == null || plant.isDead() || plant.isBeingPulled()) {
                    continue;
                }

                placePlantActor(plant, activePlants, column, row);
            }
        }

        for (Plant plant : gameContext.getPulledPlants()) {
            if (plant == null || plant.isDead()) {
                continue;
            }
            double drawCol = plant.getVisualX() != null ? plant.getVisualX() : plant.getCol();
            double drawRow = plant.getVisualY() != null ? plant.getVisualY() : plant.getRow();
            placePlantActor(plant, activePlants, drawCol, drawRow);
        }

        removeMissingPlants(activePlants);
    }

    private void placePlantActor(
        Plant plant,
        Set<Plant> activePlants,
        double column,
        double row
    ) {
        activePlants.add(plant);

        PlantActor actor = plantActors.get(plant);
        if (actor == null) {
            actor = createPlantActor(plant);
            if (actor == null) {
                return;
            }
            plantActors.put(plant, actor);
            addActor(actor);
        }

        actor.setPosition(getCellCenterX(column), getCellCenterY(row));
        actor.setZIndex(Math.max(0, (int) Math.round(row)));
    }

    private PlantActor createPlantActor(Plant plant) {
        PlantAnimationSpec spec =
            resolver.resolve(plant.getName());

        if (spec == null) {
            return null;
        }

        PlantAnimationSpec lilyPadSpec = plant.isHasLilyPadUnderneath()
            ? resolver.resolve("Lily Pad")
            : null;

        return new PlantActor(
            plant,
            spec,
            pamPlayer,
            getCellHeight(),
            lilyPadSpec
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

    private float getCellCenterX(double column) {
        float cellWidth =
            gridWidth / gameContext.getLevel().getColumns();

        return gridX
            + (float) column * cellWidth
            + cellWidth / 2f;
    }

    private float getCellCenterY(double row) {
        float cellHeight =
            gridHeight / gameContext.getLevel().getRows();

        return gridY
            + gridHeight
            - (float) row * cellHeight
            - cellHeight / 2f;
    }

    private void playPendingAttackAnimations() {
        Plant plant;

        while ((plant = gameContext.pollPlantAttackAnimation()) != null) {
            PlantActor actor = plantActors.get(plant);

            if (actor != null) {
                actor.play(PlantAnimationState.ATTACK);
            } else {
                plant.releaseAllPendingShots(gameContext);
            }
        }
    }

    private void releaseSyncedShots() {
        for (Map.Entry<Plant, PlantActor> entry : plantActors.entrySet()) {
            Plant plant = entry.getKey();
            PlantActor actor = entry.getValue();
            if (plant == null || actor == null || !plant.hasPendingShots()) {
                continue;
            }
            int due = actor.dueShotReleases();
            for (int i = 0; i < due; i++) {
                if (!plant.releaseNextPendingShot(gameContext)) {
                    break;
                }
                actor.markShotReleased();
            }
        }
    }
}
