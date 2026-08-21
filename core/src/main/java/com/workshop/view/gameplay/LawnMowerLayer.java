package com.workshop.view.gameplay;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.mechanisms.LawnMower;
import pvz.libpvz.pam.PamPlayer;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class LawnMowerLayer extends Group {

    private final GameContext gameContext;
    private final GameEngine gameEngine;
    private final PamPlayer pamPlayer;

    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    private final Map<LawnMower, MowerActor> actors = new IdentityHashMap<>();

    public LawnMowerLayer(
        GameContext gameContext,
        GameEngine gameEngine,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;
        this.gameEngine = gameEngine;
        this.pamPlayer = Textures.getPamPlayer();
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    @Override
    public void act(float delta) {
        syncMowers();
        super.act(delta);
    }

    private void syncMowers() {
        LawnMower[] mowers = gameEngine.getLawnMowers();
        if (mowers == null) {
            return;
        }

        for (LawnMower mower : mowers) {
            if (mower == null) {
                continue;
            }

            if (!mower.isAvailable()) {
                MowerActor gone = actors.remove(mower);
                if (gone != null) {
                    gone.remove();
                }
                continue;
            }

            MowerActor actor = actors.get(mower);
            if (actor == null) {
                actor = new MowerActor(mower, resolvePamPath());
                actors.put(mower, actor);
                addActor(actor);
            }

            actor.setPosition(
                toStageX(mower.getX()),
                toStageY(mower.getRow())
            );
        }
    }

    private String resolvePamPath() {
        String season = gameContext.getSeason() != null
            ? gameContext.getSeason().getName()
            : "";

        switch (season) {
            case "Ancient Egypt":
                return "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";
            case "FrozenCave":
                return "768/FULL/MOWERS/MOWER_ICEAGE/MOWER_ICEAGE.PAM";
            case "Big Wave Beach":
                return "768/FULL/MOWERS/MOWER_BEACH/MOWER_BEACH.PAM";
            case "Dark Ages":
                return "768/FULL/MOWERS/MOWER_DARK/MOWER_DARK.PAM";
            default:
                return "768/INITIAL/MOWERS/MOWER_TUTORIAL/MOWER_TUTORIAL.PAM";
        }
    }

    private float toStageX(double tileX) {
        float cellWidth = gridWidth / gameContext.getLevel().getColumns();
        return gridX + (float) tileX * cellWidth - cellWidth * 0.55f;
    }

    private float toStageY(int row) {
        float cellHeight = gridHeight / gameContext.getLevel().getRows();
        return gridY
            + gridHeight
            - row * cellHeight
            - cellHeight / 2f;
    }

    private final class MowerActor extends Actor {
        private final LawnMower mower;
        private final String pamPath;
        private String idleClip;
        private String runClip;
        private float stateTime;

        MowerActor(LawnMower mower, String pamPath) {
            this.mower = mower;
            this.pamPath = pamPath;

            List<String> clips = pamPlayer.clips(pamPath);
            idleClip = pick(clips, "idle", "animation");
            runClip = pick(clips, "running", "animation", "idle");

            if (idleClip == null && clips != null && !clips.isEmpty()) {
                idleClip = clips.get(0);
            }
            if (runClip == null) {
                runClip = idleClip;
            }
        }

        private String pick(List<String> clips, String... names) {
            if (clips == null) {
                return null;
            }
            for (String name : names) {
                if (clips.contains(name)) {
                    return name;
                }
            }
            return null;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            String clip = mower.isActivated() ? runClip : idleClip;
            if (clip == null) {
                return;
            }

            batch.setColor(1f, 1f, 1f, parentAlpha);
            pamPlayer.draw(
                batch,
                pamPath,
                clip,
                stateTime,
                getX(),
                getY(),
                true
            );
        }
    }
}
