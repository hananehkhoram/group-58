package model.zombie.behavior;

import model.zombie.Zombie;

public class Eating implements Behaviors {

    @Override
    public void onTick(Zombie zombie) {
        //eats the plant
    }

    @Override
    public void onHit(Zombie zombie, int damage) {

    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
