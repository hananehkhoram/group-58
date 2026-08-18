package com.workshop.view.gameplay;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.VaseGame.Vase;
import com.workshop.model.MiniGame.VaseGame.VaseContent;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.mechanisms.Tile;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import pvz.libpvz.pam.PamPlayer;
import com.badlogic.gdx.scenes.scene2d.Actor;


public final class VaseAnimationLayer extends Group {

    private static final String PLANT_VASE_PAM =
        "768/FULL/VASEBREAKER/VASE_GREEN/VASE_GREEN.PAM";

    private static final String GARGANTUAR_VASE_PAM =
        "768/FULL/VASEBREAKER/VASE_GARGANTUAR/VASE_GARGANTUAR.PAM";

    private static final String NORMAL_VASE_PAM =
        "768/FULL/VASEBREAKER/VASE_BROWN/VASE_BROWN.PAM";

    private final GameContext gameContext;
    private final GameEngine gameEngine;
    private final PamPlayer pamPlayer;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Map<Vase, Actor> vaseActors =
        new IdentityHashMap<>();

    public VaseAnimationLayer(
        GameContext gameContext,
        GameEngine gameEngine,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;
        this.gameEngine = gameEngine;

        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        this.pamPlayer =
            Textures.getPamPlayer();
    }

    @Override
    public void act(float delta) {
        syncVases();
        super.act(delta);
    }

    private void syncVases() {

        Set<Vase> activeVases =
            Collections.newSetFromMap(
                new IdentityHashMap<>()
            );

        int rows =
            gameContext.getLevel().getRows();

        int columns =
            gameContext.getLevel().getColumns();

        for (int row = 0;
             row < rows;
             row++) {

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

                Vase vase =
                    tile.getVase();

                if (vase == null
                    || vase.isBroken()) {

                    continue;
                }

                activeVases.add(vase);

                Actor actor =
                    vaseActors.get(vase);

                if (actor == null) {

                    actor =
                        createVaseActor(
                            vase,
                            column,
                            row
                        );

                    vaseActors.put(
                        vase,
                        actor
                    );

                    addActor(actor);
                }

                updateActorBounds(
                    actor,
                    column,
                    row
                );
            }
        }

        removeMissingVases(
            activeVases
        );
    }

    private Actor createVaseActor(
        Vase vase,
        int column,
        int row
    ) {

        String pamPath =
            resolvePamPath(vase);

        Actor actor =
            new VaseActor(
                vase,
                pamPlayer,
                pamPath,
                "idle"
            );

        actor.addListener(
            new ClickListener() {

                @Override
                public void clicked(
                    InputEvent event,
                    float x,
                    float y
                ) {

                    if (gameContext.isPaused()
                        || gameContext.isGameEnded()) {
                        return;
                    }

                    gameEngine.smashVase(
                        column,
                        row,
                        gameContext
                    );

                    event.stop();
                }
            }
        );

        return actor;
    }

    private void updateActorBounds(
        Actor actor,
        int column,
        int row
    ) {

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

        float width =
            cellWidth * 0.82f;

        float height =
            cellHeight * 0.95f;

        float centerX =
            gridX
                + column * cellWidth
                + cellWidth / 2f;

        float centerY =
            gridY
                + gridHeight
                - row * cellHeight
                - cellHeight / 2f;

        actor.setBounds(
            centerX - width / 2f,
            centerY - height / 2f,
            width,
            height
        );
    }

    private void removeMissingVases(
        Set<Vase> activeVases
    ) {

        Iterator<Map.Entry<Vase, Actor>>
            iterator =
            vaseActors
                .entrySet()
                .iterator();

        while (iterator.hasNext()) {

            Map.Entry<Vase, Actor>
                entry =
                iterator.next();

            if (activeVases.contains(
                entry.getKey()
            )) {

                continue;
            }

            entry.getValue().remove();
            iterator.remove();
        }
    }

    private String resolvePamPath(Vase vase) {

        if (vase.getContent() == VaseContent.PLANT) {
            return PLANT_VASE_PAM;
        }

        if (vase.getContent() == VaseContent.ZOMBIE
            && "Gargantuar".equalsIgnoreCase(
            vase.getHiddenEntityName()
        )) {

            return GARGANTUAR_VASE_PAM;
        }

        return NORMAL_VASE_PAM;
    }
}
