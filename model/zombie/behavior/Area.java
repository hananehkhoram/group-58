package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.plants.Tag;
import model.projectile.BulletType;
import model.projectile.Projectile;
import model.zombie.Zombie;

public class Area implements Behaviors {
    private String season;
    private boolean torchLit = true;

    public Area() {}

    public Area(String season) {
        this.season = season;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        updateTorchState(zombie, ctx);
        if (!torchLit) return;

        int row = zombie.getRow();
        int totalCols = ctx.getPlantGrid()[0].length;

        for (int col = 0; col < totalCols; col++) {
            Plant target = ctx.getPlantGrid()[row][col];
            if (target != null && !target.isDead()) {

                double distance = zombie.isMovingBackward()
                        ? target.getCol() - zombie.getX()
                        : zombie.getX() - target.getCol();

                if (distance > 0 && distance < 1.0) {
                    target.takeDamage(Integer.MAX_VALUE);
                }
            }
        }
    }

    private void updateTorchState(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int totalCols = ctx.getPlantGrid()[0].length;

        for (int col = 0; col < totalCols; col++) {
            Plant p = ctx.getPlantGrid()[row][col];
            if (p == null || p.isDead()) continue;

            if (Math.abs(p.getCol() - zombie.getX()) <= 1.0) {
                if (p.hasTheTag(Tag.ICE)) {
                    torchLit = false;
                }
                else if (p.hasTheTag(Tag.FIRE)) {
                    torchLit = true;
                }
            }
        }

        for (Projectile p : ctx.getProjectiles()) {
            if (p.getRow() == zombie.getRow() && Math.abs(p.getX() - zombie.getX()) < 0.5) {
                if (p.getBulletType() == BulletType.ICE) {
                    torchLit = false;
                } else if (p.getBulletType() == BulletType.FIRE) {
                    torchLit = true;
                }
            }
        }

        if (zombie.isIced()) {
            torchLit = false;
        }
    }

}