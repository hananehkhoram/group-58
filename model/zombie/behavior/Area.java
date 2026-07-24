package model.zombie.behavior;

import model.GameContext;
import model.level.Level;
import model.plants.Plant;
import model.plants.Tag;
import model.projectile.BulletType;
import model.projectile.Projectile;
import model.zombie.Zombie;
import view.ConsoleView;

public class Area implements Behaviors {

    public enum AreaType {
        TORCH,
        FISHERMAN
    }

    private AreaType areaType = AreaType.TORCH;
    private String season;
    private boolean torchLit = true;

    private int fishermanCooldown = 5;
    private int lastFishermanActionSecond = 0;

    public Area() {
        this.areaType = AreaType.TORCH;
    }

    public Area(AreaType areaType) {
        this.areaType = areaType;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        switch (areaType) {
            case TORCH -> handleTorchLogic(zombie, ctx);
            case FISHERMAN -> handleFishermanLogic(zombie, ctx);
        }
    }

    private void handleFishermanLogic(Zombie zombie, GameContext ctx) {
        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        if (currentSecond - lastFishermanActionSecond < fishermanCooldown) {
            return;
        }

        int row = zombie.getRow();
        int fishermanCol = (int) zombie.getX();

        Plant targetPlant = null;
        int targetCol = -1;

        for (int col = fishermanCol; col >= 0; col--) {
            Plant p = ctx.getPlantGrid()[row][col];
            if (p != null && !p.isDead()) {
                targetPlant = p;
                targetCol = col;
                break;
            }
        }

        if (targetPlant == null) return;
        if (targetCol == fishermanCol - 1) {
            targetPlant.takeDamage(Integer.MAX_VALUE);
            lastFishermanActionSecond = currentSecond;
            return;
        }


        int rightCol = targetCol + 1;
        if (rightCol < Level.COLS && ctx.getPlantGrid()[row][rightCol] == null) {
            ctx.getPlantGrid()[row][targetCol] = null;
            ctx.getPlantGrid()[row][rightCol] = targetPlant;
            targetPlant.setCol(rightCol);

            lastFishermanActionSecond = currentSecond;

            ConsoleView.showMessage(String.format(
                    "Fisherman Zombie pulled %s from col %d to col %d!\n",
                    targetPlant.getName(), targetCol, rightCol));
        }
    }

    private void handleTorchLogic(Zombie zombie, GameContext ctx) {
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
                } else if (p.hasTheTag(Tag.FIRE)) {
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