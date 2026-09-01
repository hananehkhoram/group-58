package com.workshop.view.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.workshop.controller.repository.Textures;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.ExplosionFx;
import pvz.libpvz.pam.PamPlayer;

import java.util.List;

public final class ExplosionFxLayer extends Group {

    private static final String POTATO_PAM =
        "768/INITIAL/EFFECTS/POTATOMINE_EXPLOSION/POTATOMINE_EXPLOSION.PAM";
    private static final String PRIMAL_POTATO_PAM =
        "768/INITIAL/EFFECTS/PRIMAL_POTATOMINE_EXPLOSION/PRIMAL_POTATOMINE_EXPLOSION.PAM";
    private static final String CHERRY_TOP_PAM =
        "768/FULL/EFFECTS/CHERRYBOMB_EXPLOSION_TOP/CHERRYBOMB_EXPLOSION_TOP.PAM";
    private static final String CHERRY_REAR_PAM =
        "768/FULL/EFFECTS/CHERRYBOMB_EXPLOSION_REAR/CHERRYBOMB_EXPLOSION_REAR.PAM";
    private static final String GRAPESHOT_PAM =
        "768/INITIAL/EFFECTS/ESCAPEROOT_EXPLOSION_GRAPESHOT/ESCAPEROOT_EXPLOSION_GRAPESHOT.PAM";
    private static final String JALAPENO_PAM =
        "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM";
    private static final String GENERIC_FRONT_PAM =
        "768/INITIAL/EFFECTS/GENERIC_EXPLOSION_FRONT/GENERIC_EXPLOSION_FRONT.PAM";
    private static final String GENERIC_BACK_PAM =
        "768/INITIAL/EFFECTS/GENERIC_EXPLOSION_BACK/GENERIC_EXPLOSION_BACK.PAM";

    private final GameContext gameContext;
    private final PamPlayer pamPlayer;
    private final float gridX;
    private final float gridY;
    private final float gridWidth;
    private final float gridHeight;

    public ExplosionFxLayer(
        GameContext gameContext,
        float gridX,
        float gridY,
        float gridWidth,
        float gridHeight
    ) {
        this.gameContext = gameContext;
        this.pamPlayer = Textures.getPamPlayer();
        this.gridX = gridX;
        this.gridY = gridY;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    @Override
    public void act(float delta) {
        ExplosionFx fx;
        while ((fx = gameContext.pollExplosion()) != null) {
            spawn(fx);
        }
        super.act(delta);
    }

    private void spawn(ExplosionFx fx) {
        switch (fx.kind) {
            case POTATO -> addBurst(fx.row, fx.col, POTATO_PAM, 1.6f, 1.15f, null);
            case PRIMAL_POTATO -> addBurst(fx.row, fx.col, PRIMAL_POTATO_PAM, 2.1f, 1.25f, null);
            case CHERRY -> {
                addBurst(fx.row, fx.col, CHERRY_REAR_PAM, 2.4f, 1.2f, null);
                addBurst(fx.row, fx.col, CHERRY_TOP_PAM, 2.4f, 1.2f, null);
            }
            case GRAPESHOT -> addBurst(fx.row, fx.col, GRAPESHOT_PAM, 2.2f, 1.2f, null);
            case JALAPENO -> spawnLaneFire(fx.row);
            case DOOM -> {
                addBurst(fx.row, fx.col, GENERIC_BACK_PAM, 3.4f, 1.5f, null);
                addBurst(fx.row, fx.col, GENERIC_FRONT_PAM, 3.4f, 1.5f, null);
            }
            case GENERIC -> {
                addBurst(fx.row, fx.col, GENERIC_BACK_PAM, 1.8f, 1.15f, null);
                addBurst(fx.row, fx.col, GENERIC_FRONT_PAM, 1.8f, 1.15f, null);
            }
            case ICEAGE_MISSILE -> addBurst(
                fx.row,
                fx.col,
                "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_ICEAGE/ZOMBOSS_MISSILE_EXPLOSION_ICEAGE.PAM",
                3.0f,
                1.4f,
                null
            );
            case EGYPT_MISSILE -> addBurst(
                fx.row,
                fx.col,
                "768/INITIAL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_EGYPT/ZOMBOSS_MISSILE_EXPLOSION_EGYPT.PAM",
                3.1f,
                1.45f,
                null
            );
            case DARK_FIREBALL -> addBurst(
                fx.row,
                fx.col,
                "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_DARK/ZOMBOSS_MISSILE_EXPLOSION_DARK.PAM",
                2.6f,
                1.35f,
                null
            );
            case BEACH_SHARK -> addBurst(
                fx.row,
                fx.col,
                "768/FULL/EFFECTS/ZOMBOSS_SHARK_PROJECTILE/ZOMBOSS_SHARK_PROJECTILE.PAM",
                1.85f,
                1.35f,
                "attack"
            );
            case TURBINE_WIND -> addBurst(
                fx.row,
                fx.col,
                "768/FULL/EFFECTS/ZOMBOSS_TURBINE_WIND/ZOMBOSS_TURBINE_WIND.PAM",
                2.4f,
                1.6f,
                "animation"
            );
            case PLANT_PULLED -> addBurst(
                fx.row,
                fx.col,
                "768/FULL/EFFECTS/ZOMBOSS_PLANT_PULLED/ZOMBOSS_PLANT_PULLED.PAM",
                1.4f,
                0.9f,
                "animation"
            );
        }
    }

    private void spawnLaneFire(int row) {
        int columns = gameContext.getLevel().getColumns();
        for (int col = 0; col < columns; col++) {
            addBurst(row, col, JALAPENO_PAM, 1.35f, 1.35f, null);
        }
    }

    private void addBurst(
        int row,
        int col,
        String pamPath,
        float sizeInCells,
        float lifetime,
        String preferredClip
    ) {
        addActor(new BurstActor(
            getCellCenterX(col),
            getCellCenterY(row),
            getCellHeight() * sizeInCells,
            pamPath,
            lifetime,
            preferredClip
        ));
    }

    private float getCellHeight() {
        return gridHeight / gameContext.getLevel().getRows();
    }

    private float getCellWidth() {
        return gridWidth / gameContext.getLevel().getColumns();
    }

    private float getCellCenterX(int column) {
        return gridX + column * getCellWidth() + getCellWidth() / 2f;
    }

    private float getCellCenterY(int row) {
        return gridY + gridHeight - row * getCellHeight() - getCellHeight() / 2f;
    }

    private final class BurstActor extends Actor {
        private final float centerX;
        private final float centerY;
        private final float targetHeight;
        private final String pamPath;
        private final float lifetime;
        private String clip;
        private boolean resolved;
        private float scale = 1f;
        private float stateTime;

        private final String preferredClip;

        BurstActor(
            float centerX,
            float centerY,
            float targetHeight,
            String pamPath,
            float lifetime,
            String preferredClip
        ) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.targetHeight = targetHeight;
            this.pamPath = pamPath;
            this.lifetime = lifetime;
            this.preferredClip = preferredClip;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            if (stateTime >= lifetime) {
                remove();
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (!resolved) {
                resolve();
                resolved = true;
            }
            if (clip == null) {
                return;
            }

            batch.flush();
            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(centerX, centerY, 0f)
                .scale(scale, scale, 1f)
                .translate(-centerX, -centerY, 0f);
            batch.setTransformMatrix(scaled);
            batch.setColor(1f, 1f, 1f, parentAlpha);
            pamPlayer.draw(batch, pamPath, clip, stateTime, centerX, centerY, false);
            batch.flush();
            batch.setTransformMatrix(original);
            batch.setColor(1f, 1f, 1f, parentAlpha);
        }

        private void resolve() {
            List<String> clips = pamPlayer.clips(pamPath);
            if (clips == null || clips.isEmpty()) {
                Gdx.app.error("ExplosionFxLayer", "No clips for " + pamPath);
                return;
            }
            if (preferredClip != null) {
                for (String candidate : clips) {
                    if (preferredClip.equalsIgnoreCase(candidate)) {
                        clip = candidate;
                        break;
                    }
                }
            }
            if (clip == null && clips.contains("animation")) {
                clip = "animation";
            } else if (clip == null && clips.contains("idle")) {
                clip = "idle";
            } else if (clip == null) {
                clip = clips.get(0);
            }

            Rectangle bounds = pamPlayer.bounds(pamPath, clip);
            if (bounds != null && bounds.height > 0f) {
                scale = targetHeight / bounds.height;
            }
        }
    }
}
