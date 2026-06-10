package model.mechanisms;

import model.GameContext;
import model.plants.plantsKinds.Plant;
import model.zombie.Zombie;

public class Tile {
    private int x;
    private int y;
    private TerrainType terrainType;
    private int terrainHealth;
    private Plant plant;
    private GameContext ctx;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public int getTerrainHealth() {
        return terrainHealth;
    }

    public Plant getPlant() {
        return plant;
    }

    public Zombie[] getZombies() {
        return zombies;
    }

    public boolean setPlant(Plant plant) {
        return false;
    }

    public Plant removePlant() {
        return null;
    }

    public void addZombie(Zombie zombie) {
    }

    public Zombie removeZombie() {
        return null;
    }

    public boolean isPlantable() {
        return false;
    }

    public boolean blocksProjectile() {
        return false;
    }
}
