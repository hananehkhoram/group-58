package com.workshop.model.zombie.behavior;

import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;

public class SquashAttack implements Behaviors {

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (zombie.isDead()) {
            return;
        }

        int row = zombie.getRow();
        int col = (int) zombie.getX();

        Plant[][] plantGrid = ctx.getPlantGrid();

        if (row < 0
            || row >= plantGrid.length
            || col < 0
            || col >= plantGrid[0].length) {
            return;
        }

        Plant target = plantGrid[row][col];

        if (target == null || target.isDead()) {
            return;
        }

        target.takeDamage(Integer.MAX_VALUE);
        zombie.takeDamage(Integer.MAX_VALUE);
        zombie.setEating(false);
    }
}
