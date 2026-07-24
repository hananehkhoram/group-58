package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;
//
public class Eating implements Behaviors {

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int col = (int) zombie.getX();

        int totalRows = ctx.getPlantGrid().length;
        int totalCols = ctx.getPlantGrid()[0].length;
        if (row < 0 || row >= totalRows || col < 0 || col >= totalCols) {
            zombie.setEating(false);
            return;
        }

        Plant target = ctx.getPlantGrid()[row][col];
        if (target != null && target.getHp() > 0) {
            boolean wasEating = zombie.isEating();
            zombie.setEating(true);
            if (!wasEating) {
                zombie.resetEatClock(ctx);
            }
            int damage = zombie.consumeEatDamage(ctx);
            if (damage > 0) {
                target.takeDamage(damage);
            }
        } else {
            zombie.setEating(false);
        }
    }
}