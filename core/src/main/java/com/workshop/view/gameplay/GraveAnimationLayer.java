package com.workshop.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.season.Grave;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import pvz.libpvz.pam.PamPlayer;

public final class GraveAnimationLayer extends Group {

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Map<Grave, GraveActor> graveActors =
        new IdentityHashMap<>();

    public GraveAnimationLayer(
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

        pamPlayer = Textures.getPamPlayer();
    }

    @Override
    public void act(float delta) {
        syncGraveActors();
        super.act(delta);
    }

    private void syncGraveActors() {
        Grave[][] graveGrid = gameContext.getGraveGrid();

        Set<Grave> activeGraves =
            Collections.newSetFromMap(new IdentityHashMap<>());

        for (int row = 0; row < graveGrid.length; row++) {
            for (int column = 0;
                 column < graveGrid[row].length;
                 column++) {

                Grave grave = graveGrid[row][column];

                if (grave == null) {
                    continue;
                }

                activeGraves.add(grave);

                GraveActor actor = graveActors.get(grave);

                if (actor == null) {
                    actor = new GraveActor(grave, pamPlayer, getCellHeight());
                    graveActors.put(grave, actor);
                    addActor(actor);
                }

                actor.setPosition(
                    getCellCenterX(column),
                    getCellCenterY(row)
                );
                actor.setZIndex(row);
            }
        }

        removeMissingGraves(activeGraves);
    }

    private void removeMissingGraves(Set<Grave> activeGraves) {
        Iterator<Map.Entry<Grave, GraveActor>> iterator =
            graveActors.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Grave, GraveActor> entry = iterator.next();

            if (activeGraves.contains(entry.getKey())) {
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
