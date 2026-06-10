package model.World;

import model.level.LevelData;
import model.mechanisms.LawnMower;
import model.mechanisms.Tile;
import model.plants.plantsKinds.Plant;
import model.zombie.Zombie;

public class World {
    private LevelData.Level level;
    private Tile[][] tiles;
    private LawnMower[] lawnMowers;
    private int rows;
    private int columns;

    public void World(LevelData.Level level){}
    public Tile getTile(int x,int y){return null;}

    public boolean placePlant(Plant plant, int x, int y){return false;}
    public Plant removePlant(){return null;}

    public boolean addZombie(Zombie zombie, int x, int y){return false;}
    public void moveZombies(){}
    public Zombie[] getRowsZombies(int row){return null;}
    public boolean isRowClear(int row){return false;}
    private void attackPlant(Zombie zombie, Plant plant,int x,int y){}

    public void triggerLawnMover(int row){}
}
