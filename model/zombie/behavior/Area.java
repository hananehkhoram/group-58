package model.zombie.behavior;

import model.zombie.Zombie;

public class Area implements Behaviors {
    private String season;

    // Explorer: max torch reach in grid units (37)
    private int torchReach;

    // Explorer: specific plants it can eat (includelist)
    private java.util.List<String> targetPlants;

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
    public void onTick(Zombie zombie) {}

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public String getSeason() { return season; }
    public int getTorchReach() { return torchReach; }
    public java.util.List<String> getTargetPlants() { return targetPlants; }
}
