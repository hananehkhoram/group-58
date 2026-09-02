package com.workshop.model.mechanisms;

import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.VaseGame.Vase;
import com.workshop.model.level.Level;
import com.workshop.model.plants.Plant;

public class Tile {
    private int x;
    private int y;
    private GameContext ctx;
    private Vase vase;
    private String droppedSeed = null;
    private int seedDespawnTimer = 0;

    public Tile(int x, int y, GameContext ctx) {
        this.x = x;
        this.y = y;
        this.ctx = ctx;
    }

    public static Tile[][] buildTiles(GameContext ctx) {
        Tile[][] grid = new Tile[Level.ROWS][Level.COLS];
        for (int r = 0; r < Level.ROWS; r++) {
            for (int c = 0; c < Level.COLS; c++) {
                grid[r][c] = new Tile(c, r, ctx);
            }
        }
        return grid;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Vase getVase() {
        return vase;
    }

    public void setVase(Vase vase) {
        this.vase = vase;
    }

    public TerrainType getTerrainType() {
        if (ctx.getGraveGrid()[y][x] != null) {
            return TerrainType.GRAVE;
        }
        if (ctx.getBeghouldManager() != null
            && ctx.getBeghouldManager().isCrater(y, x)) {
            return TerrainType.CRATER;
        }
        if (ctx.getSeason().isNecromancyCell(y, x)) {
            return TerrainType.NECROMANCY;
        }
        if (ctx.isBurnedCell(y, x)) {
            return TerrainType.BURNED;
        }
        if (ctx.getSeason().isWaterCell(y, x, ctx)) {
            return TerrainType.WATER;
        }
        int slideTo = ctx.getSeason().getSliderNextRow(y, x);
        if (slideTo < y) return TerrainType.SLIPPERY_UP;
        if (slideTo > y) return TerrainType.SLIPPERY_DOWN;

        Plant p = ctx.getPlantGrid()[y][x];
        if (p != null && p.getFreezeLevel() >= 3) {
            return TerrainType.FROZEN;
        }
        return TerrainType.NORMAL;
    }

    public Plant getPlant() {
        return ctx.getPlantGrid()[y][x];
    }

    public boolean setPlant(Plant plant) {
        Plant existing = ctx.getPlantGrid()[y][x];

        if (plant != null && plant.isStackableCover()) {
            if (existing == null || existing.getCoverPlant() != null) {
                return false;
            }
            if (!isPlantable()) {
                return false;
            }
            existing.setCoverPlant(plant);
            plant.setCoveredPlant(existing);
            plant.setRow(y);
            plant.setCol(x);
            return true;
        }

        if (existing != null) return false;
        if (!isPlantable()) return false;
        ctx.getPlantGrid()[y][x] = plant;
        return true;
    }

    public boolean isPlantable() {
        if (vase != null && !vase.isBroken()) {
            return false;
        }
        TerrainType t = getTerrainType();

        return t != TerrainType.GRAVE
            && t != TerrainType.CRATER
            && t != TerrainType.BURNED
            && t != TerrainType.SLIPPERY_DOWN
            && t != TerrainType.SLIPPERY_UP;
    }

    public void setDroppedSeed(String seedName, int lifespanTicks) {
        this.droppedSeed = seedName;
        this.seedDespawnTimer = lifespanTicks;
    }

    public String getDroppedSeed() {
        return this.droppedSeed;
    }

    public boolean hasDroppedSeed() {
        return this.droppedSeed != null;
    }

    public void clearDroppedSeed() {
        this.droppedSeed = null;
        this.seedDespawnTimer = 0;
    }

    public void updateSeedTimer(int passedTicks) {
        if (this.droppedSeed != null) {
            this.seedDespawnTimer -= passedTicks;
            if (this.seedDespawnTimer <= 0) {
                this.droppedSeed = null;
            }
        }
    }

    public void meltIce() {
        Plant p = getPlant();
        if (p != null) {
            p.meltIce();
        }
    }

}
