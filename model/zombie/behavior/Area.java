package model.zombie.behavior;

import model.season.Season;
import model.zombie.Zombie;

public class Area implements Behaviors{
    private Season season; //some zombies are just depended on seasons
    @Override
    public void onTick(Zombie zombie) {

    }

    @Override
    public void onHit(Zombie zombie, int damage) {

    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
