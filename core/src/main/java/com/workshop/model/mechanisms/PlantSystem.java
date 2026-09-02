package com.workshop.model.mechanisms;

import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.PlantActivator;
import com.workshop.model.level.LevelType;

import java.util.ArrayList;
import java.util.List;

public final class PlantSystem {

    private final GameContext ctx;
    private final GameEngine engine;

    public PlantSystem(GameContext ctx, GameEngine engine) {
        this.ctx = ctx;
        this.engine = engine;
    }

    public void update(double deltaTime) {
        List<Plant> plantsSnapshot =
            new ArrayList<>(
                ctx.getAlivePlants()
            );

        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        for (Plant p : plantsSnapshot) {
            if (!ctx.getAlivePlants().contains(p)) {
                continue;
            }
            if (p.getName() != null) {
                String pName = p.getName().replace("-", "").toLowerCase();
                if (pName.equals("puffshroom") || pName.equals("seashroom")) {
                    if (p.getPlantTimeSecond() == 0) {
                        p.setPlantTimeSecond(currentSecond);
                    }
                    if (currentSecond - p.getPlantTimeSecond() >= 60) {
                        p.takeDamage(Integer.MAX_VALUE);
                    }
                }
            }

            PlantActivator.activate(
                p,
                ctx,
                engine
            );

            if (!ctx.getAlivePlants().contains(p)) {
                continue;
            }

            if (p.getName() != null
                && p.getName()
                .equalsIgnoreCase("Imitater")) {

                continue;
            }

            flushTimedOutShots(p);

            if (p.isDead()) {
                p.discardPendingShots();
                int row = p.getRow();
                int col = p.getCol();
                boolean restoreLilyPad = p.isHasLilyPadUnderneath();

                if (row >= 0 && col >= 0
                    && row < ctx.getPlantGrid().length
                    && col < ctx.getPlantGrid()[row].length
                    && ctx.getPlantGrid()[row][col] == p) {
                    ctx.getPlantGrid()[row][col] = null;
                }
                ctx.removePulledPlant(p);

                if (ctx.getLevel().getLevelType()
                    == LevelType.Beghouled_MG
                    && ctx.getBeghouldManager() != null) {

                    ctx.getBeghouldManager()
                        .markCrater(row, col);
                    restoreLilyPad = false;
                }

                ctx.getAlivePlants().remove(p);

                ctx.incrementPlantsLost(p);

                if (restoreLilyPad) {
                    restoreLilyPad(row, col);
                }
            }
        }
    }

    public void removePlant(int row, int col) {
        Plant p = ctx.getPlantGrid()[row][col];
        if (p != null) {
            p.discardPendingShots();
            ctx.getPlantGrid()[row][col] = null;
            ctx.getAlivePlants().remove(p);
        }
    }

    private void flushTimedOutShots(Plant plant) {
        if (!plant.hasPendingShots()) {
            return;
        }
        long armed = plant.getPendingShotArmedTick();
        if (armed < 0) {
            return;
        }
        if (ctx.getTimeManager().getTotalTicks() - armed >= 15) {
            plant.releaseAllPendingShots(ctx);
        }
    }

    private void restoreLilyPad(int row, int col) {
        if (row < 0 || col < 0
            || row >= ctx.getPlantGrid().length
            || col >= ctx.getPlantGrid()[row].length) {
            return;
        }
        if (ctx.getPlantGrid()[row][col] != null) {
            return;
        }
        try {
            Plant pad = ctx.getPlantFactory().create("Lily Pad");
            pad.setRow(row);
            pad.setCol(col);
            ctx.getPlantGrid()[row][col] = pad;
            ctx.getAlivePlants().add(pad);
        } catch (RuntimeException ignored) {
        }
    }
}
