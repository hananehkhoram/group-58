package model.zombie.behavior;

import model.GameContext;
import model.mechanisms.TerrainType;
import model.mechanisms.Tile;
import model.plants.Plant;
import model.projectile.BulletType;
import model.projectile.Projectile;
import model.projectile.TrajectoryType;
import model.zombie.Zombie;


public class Submerge implements Behaviors {

    private boolean submerged;

    public Submerge() {
        this.submerged = true;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        int row = zombie.getRow();
        int col = (int) zombie.getX();

        int totalRows = ctx.getPlantGrid().length;
        int totalCols = ctx.getPlantGrid()[0].length;

        boolean isInWater = false;
        Tile currentTile = (row >= 0 && row < totalRows && col >= 0 && col < totalCols)
                ? ctx.getGameEngine().getTiles(col, row) : null;

        if (currentTile != null) {
            TerrainType terrain = currentTile.getTerrainType();
            isInWater = (terrain == TerrainType.WATER || terrain == TerrainType.LOW_TIDE);
        }

        Plant target = (row >= 0 && row < totalRows && col >= 0 && col < totalCols)
                ? ctx.getPlantGrid()[row][col] : null;
        boolean hasLiveTarget = (target != null && !target.isDead());

        if (!isInWater) {
            surface();
        } else if (hasLiveTarget) {
            surface();
        } else {
            submerge();
        }
    }




    public void surface() {
        submerged = false;
    }

    public void submerge() {
        submerged = true;
    }


    public boolean isVulnerableTo(Projectile projectile) {
        if (!submerged) return true;
        return projectile != null && projectile.getTrajectory() == TrajectoryType.LOBBED;
    }
}