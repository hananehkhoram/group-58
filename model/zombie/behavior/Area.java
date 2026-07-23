package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;

public class Area implements Behaviors {
    private String season;
    private int torchReach;
    private java.util.List<String> targetPlants;
    private boolean torchLit = true;

    public Area() {}

    public Area(String season) {
        this.season = season;
    }

    // For ZombieExplorer
    public Area(int torchReach, java.util.List<String> targetPlants) {
        this.torchReach = torchReach;
        this.targetPlants = targetPlants;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        updateTorchState(zombie, ctx);
        if (!torchLit) return;

        int row = zombie.getRow();
        int totalCols = ctx.getPlantGrid()[0].length;
        int frontCol = zombie.isMovingBackward()
                ? (int) Math.ceil(zombie.getX()) + 1
                : (int) Math.floor(zombie.getX()) - 1;
        if (frontCol < 0 || frontCol >= totalCols) return;
        Plant target = ctx.getPlantGrid()[row][frontCol];
        if (target != null && !target.isDead()) {
            target.takeDamage(Integer.MAX_VALUE);
        }
    }

    private void updateTorchState(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int totalCols = ctx.getPlantGrid()[0].length;

        for (int col = 0; col < totalCols; col++) {
            Plant p = ctx.getPlantGrid()[row][col];
            if (p == null || p.isDead()) continue;
            if (Math.abs(p.getCol() - zombie.getX()) > 1.0) continue;

            if (p.hasWaterTag()) {
                torchLit = false;
            } else if (p.hasFireTag()) {
                torchLit = true;
            }
        }

        if (zombie.isIced()) {
            torchLit = false;
        }
        // TODO: «پرتابه‌ی آتشین روشنش می‌کنه» هنوز جایی وصل نیست — هیچ فلگ عمومی «آتش گرفتن» روی Zombie نداریم
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public boolean isTorchLit() { return torchLit; }
    public String getSeason() { return season; }
    public int getTorchReach() { return torchReach; }
    public java.util.List<String> getTargetPlants() { return targetPlants; }
}