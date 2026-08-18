package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.Sun;
import com.workshop.model.mechanisms.SunType;
import pvz.skin.PvzSkin;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.workshop.model.mechanisms.GameEngine;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import java.util.HashSet;
import java.util.Set;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

public final class SunAnimationLayer extends Group {

    private final GameContext gameContext;
    private final GameEngine gameEngine;

    private final Actor sunHudTarget;

    private final Set<Sun> collectingSkySuns =
        java.util.Collections.newSetFromMap(new IdentityHashMap<>());

    private final Set<String> collectingProducedSuns =
        new HashSet<>();

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Map<Sun, Image> skySunActors =
        new IdentityHashMap<>();

    private final Map<String, Image> producedSunActors =
        new HashMap<>();

    public SunAnimationLayer(
        GameContext gameContext,
        GameEngine gameEngine,
        Actor sunHudTarget,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;
        this.gameEngine = gameEngine;
        this.sunHudTarget = sunHudTarget;
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    @Override
    public void act(float delta) {
        syncSkySuns();
        syncProducedSuns();

        super.act(delta);
    }

    private void syncSkySuns() {

        for (Sun sun :
            gameContext.getSunManager().getActiveSunDrops()) {

            Image actor = skySunActors.get(sun);

            if (actor == null) {
                actor = createSunImage(sun.getType(), 25);

                addSkySunHoverListener(actor, sun);

                skySunActors.put(sun, actor);
                addActor(actor);
            }

            updateAppearance(
                actor,
                sun.getType(),
                sun.getType() == SunType.SPECIAL ? 100 : 25
            );

            float targetX = getCellCenterX(sun.getX());
            float targetY = getCellCenterY(sun.getY());

            float startY =
                gridY + gridHeight + 100f;

            float currentY;

            if (sun.isOnGround()) {
                currentY = targetY;
            } else {
                currentY = MathUtils.lerp(
                    startY,
                    targetY,
                    sun.getFallProgress()
                );
            }

            actor.setPosition(
                targetX - actor.getWidth() / 2f,
                currentY - actor.getHeight() / 2f
            );
        }

        Iterator<Map.Entry<Sun, Image>> iterator =
            skySunActors.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<Sun, Image> entry =
                iterator.next();

            if (collectingSkySuns.contains(entry.getKey())) {
                continue;
            }

            if (gameContext
                .getSunManager()
                .getActiveSunDrops()
                .contains(entry.getKey())) {

                continue;
            }

            entry.getValue().remove();
            iterator.remove();
        }
    }

    private void addSkySunHoverListener(
        Image actor,
        Sun sun
    ) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(
                InputEvent event,
                float x,
                float y,
                int pointer,
                Actor fromActor
            ) {
                if (gameContext.isPaused()
                    || gameContext.isGameEnded()) {
                    return;
                }

                if (!collectingSkySuns.add(sun)) {
                    return;
                }

                boolean radioactiveInAir =
                    sun.getType() == SunType.RADIOACTIVE
                        && !sun.isOnGround();

                boolean collected = gameContext
                    .getSunManager()
                    .collectSun(sun, gameEngine);

                if (!collected) {
                    collectingSkySuns.remove(sun);
                    return;
                }

                actor.clearListeners();

                if (radioactiveInAir) {
                    actor.addAction(Actions.sequence(
                        Actions.fadeOut(0.15f),

                        Actions.run(() -> {
                            skySunActors.remove(sun);
                            collectingSkySuns.remove(sun);
                        }),

                        Actions.removeActor()
                    ));

                    return;
                }

                flyToSunHud(actor, () -> {
                    skySunActors.remove(sun);
                    collectingSkySuns.remove(sun);
                });
            }
        });
    }

    private void syncProducedSuns() {

        Map<String, Integer> producedSuns =
            gameContext.getProducedSuns();

        for (Map.Entry<String, Integer> entry :
            producedSuns.entrySet()) {

            String key = entry.getKey();
            int amount = entry.getValue();

            String[] parts = key.split(",");

            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());

            SunType type =
                gameContext.getProducedSunTypeAt(x, y);

            Image actor =
                producedSunActors.get(key);

            if (actor == null) {
                actor = createSunImage(type, amount);

                addProducedSunHoverListener(
                    actor,
                    x,
                    y
                );

                producedSunActors.put(key, actor);
                addActor(actor);
            }

            updateAppearance(actor, type, amount);

            float centerX = getCellCenterX(x);
            float centerY = getCellCenterY(y);

            actor.setPosition(
                centerX - actor.getWidth() / 2f,
                centerY - actor.getHeight() / 2f
            );
        }

        Iterator<Map.Entry<String, Image>> iterator =
            producedSunActors.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<String, Image> entry =
                iterator.next();

            if (collectingProducedSuns.contains(entry.getKey())) {
                continue;
            }

            if (producedSuns.containsKey(entry.getKey())) {
                continue;
            }

            entry.getValue().remove();
            iterator.remove();
        }
    }

    private void addProducedSunHoverListener(
        Image actor,
        int x,
        int y
    ) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(
                InputEvent event,
                float localX,
                float localY,
                int pointer,
                Actor fromActor
            ) {
                if (gameContext.isPaused()
                    || gameContext.isGameEnded()) {
                    return;
                }

                String key = x + ", " + y;

                if (!collectingProducedSuns.add(key)) {
                    return;
                }

                int collectedAmount =
                    gameContext.collectSunAt(x, y);

                if (collectedAmount <= 0) {
                    collectingProducedSuns.remove(key);
                    return;
                }

                actor.clearListeners();

                flyToSunHud(actor, () -> {
                    producedSunActors.remove(key);
                    collectingProducedSuns.remove(key);
                });
            }
        });
    }

    private void flyToSunHud(
        Image actor,
        Runnable onFinished
    ) {
        Vector2 target = new Vector2(
            sunHudTarget.getWidth() / 2f,
            sunHudTarget.getHeight() / 2f
        );

        sunHudTarget.localToStageCoordinates(target);

        float targetX =
            target.x - actor.getWidth() / 2f;

        float targetY =
            target.y - actor.getHeight() / 2f;

        actor.setOrigin(
            actor.getWidth() / 2f,
            actor.getHeight() / 2f
        );

        actor.addAction(Actions.sequence(

            Actions.parallel(
                Actions.moveTo(
                    targetX,
                    targetY,
                    0.35f,
                    Interpolation.pow2In
                ),

                Actions.scaleTo(
                    0.45f,
                    0.45f,
                    0.35f
                )
            ),

            Actions.run(onFinished),

            Actions.removeActor()
        ));
    }

    private Image createSunImage(
        SunType type,
        int amount
    ) {
        Image image = new Image(
            PvzSkin.get(),
            "image_ui_hud_ingame_sun"
        );

        updateAppearance(image, type, amount);
        return image;
    }

    private void updateAppearance(
        Image image,
        SunType type,
        int amount
    ) {

        float size;

        switch (type) {

            case SPECIAL:
                size = 95f;
                image.setColor(Color.WHITE);
                break;

            case RADIOACTIVE:
                size = 80f;
                image.setColor(
                    Color.valueOf("B45CFF")
                );
                break;

            case LARGE:
                size = 100f;
                image.setColor(Color.WHITE);
                break;

            case RAMP_UP:
                size = getRampUpSize(amount);
                image.setColor(Color.WHITE);
                break;

            case BURST_CONSUME:
                size = 105f;
                image.setColor(
                    Color.valueOf("FFD45C")
                );
                break;

            case NORMAL:
            default:
                size = 72f;
                image.setColor(Color.WHITE);
                break;
        }

        image.setSize(size, size);
    }

    private float getRampUpSize(int amount) {

        if (amount >= 75) {
            return 90f;
        }

        if (amount >= 50) {
            return 75f;
        }

        return 60f;
    }

    private float getCellCenterX(int column) {

        float cellWidth =
            gridWidth
                / gameContext.getLevel().getColumns();

        return gridX
            + column * cellWidth
            + cellWidth / 2f;
    }

    private float getCellCenterY(int row) {

        float cellHeight =
            gridHeight
                / gameContext.getLevel().getRows();

        return gridY
            + gridHeight
            - row * cellHeight
            - cellHeight / 2f;
    }
}
