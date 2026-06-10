package model.mechanisms;

import model.plants.plantsKinds.Plant;
import model.zombie.Zombie;

public class GameEngine{
    private Tile[][] tiles;
    private LawnMower[] lawnMowers;

    public Tile getTiles(int x,int y) {
        return null;
    }
    public boolean canPlacePlant(Plant plant, int x, int y){return false;}
    public void removePlant(){}
    public boolean addZombie(Zombie zombie,int x,int y){return false;}
    public void moveZombies(){}
    public void triggerLawnMover(int row){}
    public Zombie[] getRowsZombies(int row){return null;}
}
