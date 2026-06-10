package model.zombie.behavior;

import model.zombie.Zombie;

public class SunThief implements Behaviors {
    int rate;
    int distance; //dist from robbing
    private int sumAmount;

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

    public void giveBackSuns() {
    }
}
