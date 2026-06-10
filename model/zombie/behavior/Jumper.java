package model.zombie.behavior;

import model.zombie.Zombie;

public class Jumper implements Behaviors {
    public boolean reverseTheWay; //turns backward

    @Override
    public void onTick(Zombie zombie) {

    }

    @Override
    public void onHit(Zombie zombie, int damage) {

    }

    public void turnBackward() {}
    public void jump (int x, int y){}

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
