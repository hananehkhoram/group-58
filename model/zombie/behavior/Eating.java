package model.zombie.behavior;

import model.GameContext;
import model.zombie.Zombie;

public class Eating implements Behaviors {

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
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
