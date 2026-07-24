package model.mechanisms;

import model.GameContext;
import model.MiniGame.VaseGame.Vase;
import model.level.Level;
import model.plants.*;
import model.zombie.Zombie;

public class Tile {
    private int x;
    private int y;
    private GameContext ctx;
    private Vase vase;

    public Tile(int x, int y, GameContext ctx) {
        this.x = x;
        this.y = y;
        this.ctx = ctx;
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
        if (ctx.getSeason().isNecromancyCell(y,x)) {
            return TerrainType.NECROMANCY;
        }
        if (ctx.getSeason().isWaterCell(y,x, ctx)) {
            return TerrainType.WATER;
        }
        int slideTo = ctx.getSeason().getSliderNextRow(y,x);
        if (slideTo < y) return TerrainType.SLIPPERY_UP;
        if (slideTo > y) return TerrainType.SLIPPERY_DOWN;

        Plant p = ctx.getPlantGrid()[y][x];
        if (p != null && p.getFreezeLevel() >= 3) {
            return TerrainType.FROZEN;
        }
        return TerrainType.NORMAL;
    }

    public Plant getPlant() { return ctx.getPlantGrid()[y][x]; }

    public boolean setPlant(Plant plant) {
        if (ctx.getPlantGrid()[y][x] != null) return false;
        if (!isPlantable()) return false;
        ctx.getPlantGrid()[y][x] = plant;
        return true;
    }

    public Plant removePlant() {
        Plant p = ctx.getPlantGrid()[x][y];
        ctx.getPlantGrid()[x][y] = null;
        return p;
    }

    public void addZombie(Zombie zombie) {
    }

    public Zombie removeZombie() {
        return null;
    }

    public boolean isPlantable() {
        TerrainType t = getTerrainType();
        return t != TerrainType.WATER && t != TerrainType.GRAVE;
        //بعضی گیاها روی این توع زمینم میتونن کاشته بشن.برای اونا باید جدای این متد چک بشه
    }

    public boolean blocksProjectile() {
        return getTerrainType() == TerrainType.GRAVE;
    }

    public static Tile[][] buildTiles(GameContext ctx) {
        Tile[][] grid = new Tile[Level.ROWS][Level.COLS];
        for (int r = 0; r < Level.ROWS; r++) {
            for (int c = 0; c < Level.COLS; c++) {
                grid[r][c] = new Tile(c,r, ctx);
            }
        }
        return grid;
    }
}
