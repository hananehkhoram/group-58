package com.workshop.view.gameplay;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.PlantFoodDrop;
import pvz.skin.PvzSkin;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public final class PlantFoodAnimationLayer extends Group {

    private static final float ICON_SIZE = 48f;

    private final GameContext gameContext;
    private final Actor plantFoodHudTarget;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Map<PlantFoodDrop, Image> dropActors =
        new IdentityHashMap<>();

    private final Set<PlantFoodDrop> collecting =
        Collections.newSetFromMap(new IdentityHashMap<>());

    public PlantFoodAnimationLayer(
        GameContext gameContext,
        Actor plantFoodHudTarget,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;
        this.plantFoodHudTarget = plantFoodHudTarget;
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    @Override
    public void act(float delta) {
        syncDrops();
        super.act(delta);
    }

    private void syncDrops() {
        for (PlantFoodDrop drop : gameContext.getActivePlantFoodDrops()) {
            Image actor = dropActors.get(drop);

            if (actor == null) {
                actor = createPlantFoodImage();
                addHoverListener(actor, drop);
                dropActors.put(drop, actor);
                addActor(actor);
            }

            float centerX = getCellCenterX(drop.getX());
            float centerY = getCellCenterY(drop.getY());

            actor.setPosition(
                centerX - actor.getWidth() / 2f,
                centerY - actor.getHeight() / 2f
            );
        }

        Iterator<Map.Entry<PlantFoodDrop, Image>> iterator =
            dropActors.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<PlantFoodDrop, Image> entry = iterator.next();

            if (collecting.contains(entry.getKey())) {
                continue;
            }

            if (gameContext.getActivePlantFoodDrops().contains(entry.getKey())) {
                continue;
            }

            entry.getValue().remove();
            iterator.remove();
        }
    }

    private void addHoverListener(Image actor, PlantFoodDrop drop) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(
                InputEvent event,
                float x,
                float y,
                int pointer,
                Actor fromActor
            ) {
                if (gameContext.isPaused() || gameContext.isGameEnded()) {
                    return;
                }

                if (!collecting.add(drop)) {
                    return;
                }

                boolean collected = gameContext.collectPlantFoodDrop(drop);

                if (!collected) {
                    collecting.remove(drop);
                    return;
                }

                actor.clearListeners();

                flyToHud(actor, () -> {
                    dropActors.remove(drop);
                    collecting.remove(drop);
                });
            }
        });
    }

    private void flyToHud(Image actor, Runnable onFinished) {
        Vector2 target = new Vector2(
            plantFoodHudTarget.getWidth() / 2f,
            plantFoodHudTarget.getHeight() / 2f
        );

        plantFoodHudTarget.localToStageCoordinates(target);

        float targetX = target.x - actor.getWidth() / 2f;
        float targetY = target.y - actor.getHeight() / 2f;

        actor.setOrigin(actor.getWidth() / 2f, actor.getHeight() / 2f);

        actor.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveTo(targetX, targetY, 0.35f, Interpolation.pow2In),
                Actions.scaleTo(0.5f, 0.5f, 0.35f)
            ),
            Actions.run(onFinished),
            Actions.removeActor()
        ));
    }

    private Image createPlantFoodImage() {
        Image image = new Image(
            PvzSkin.get()
                .get("plantfood", ImageButton.ImageButtonStyle.class)
                .imageUp
        );
        image.setSize(ICON_SIZE, ICON_SIZE);
        return image;
    }

    private float getCellCenterX(int column) {
        float cellWidth = gridWidth / gameContext.getLevel().getColumns();
        return gridX + column * cellWidth + cellWidth / 2f;
    }

    private float getCellCenterY(int row) {
        float cellHeight = gridHeight / gameContext.getLevel().getRows();
        return gridY + gridHeight - row * cellHeight - cellHeight / 2f;
    }
}
