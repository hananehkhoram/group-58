package model.mechanisms;

import model.GameContext;
import model.plants.*;
import model.zombie.Zombie;

public class Tile {
    private int x;
    private int y;
    private GameContext ctx;

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

    public TerrainType getTerrainType() {
        if (ctx.getGraveGrid()[x][y] != null) {
            return TerrainType.GRAVE;
        }
        if (ctx.getSeason().isNecromancyCell(x, y)) {
            return TerrainType.NECROMANCY;
        }
        if (ctx.getSeason().isWaterCell(x, y, ctx)) {
            return TerrainType.WATER;
        }
        int slideTo = ctx.getSeason().getSliderNextRow(x, y);
        if (slideTo < x) return TerrainType.SLIPPERY_UP;
        if (slideTo > x) return TerrainType.SLIPPERY_DOWN;

        Plant p = ctx.getPlantGrid()[x][y];
        if (p != null && p.getFreezeLevel() >= 3) {
            return TerrainType.FROZEN;
        }
        return TerrainType.NORMAL;
    }

    public Plant getPlant() { return ctx.getPlantGrid()[x][y]; }

    public boolean setPlant(Plant plant) {
        if (ctx.getPlantGrid()[x][y] != null) return false;
        if (!isPlantable()) return false;
        ctx.getPlantGrid()[x][y] = plant;
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
}
