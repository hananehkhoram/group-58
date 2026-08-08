package com.workshop.model.zombie.behavior;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

public class JalapenoExplosion implements Behaviors {

    private static final long EXPLOSION_DELAY_TICKS = 100;
    private boolean exploded;

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (exploded || zombie.isDead()) {
            return;
        }

        long currentTick =
            ctx.getTimeManager().getTotalTicks();

        long elapsedTicks =
            currentTick - zombie.getSpawnTick();

        if (elapsedTicks < EXPLOSION_DELAY_TICKS) {
            return;
        }

        destroyPlantsInRow(zombie.getRow(), ctx);

        exploded = true;

        Console.showMessage(
            "Jalapeno Zombie burned all plants in row "
                + zombie.getRow()
                + "."
        );

        zombie.takeDamage(Integer.MAX_VALUE);
    }

    private void destroyPlantsInRow(
        int row,
        GameContext ctx) {

        Plant[][] plantGrid = ctx.getPlantGrid();

        if (row < 0 || row >= plantGrid.length) {
            return;
        }

        for (Plant plant : plantGrid[row]) {
            if (plant != null && !plant.isDead()) {
                plant.takeDamage(Integer.MAX_VALUE);
            }
        }
    }
}
