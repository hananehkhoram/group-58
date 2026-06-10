package model.zombie.behavior;

import model.zombie.Zombie;

public class LaserShooting implements Behaviors{
    private int distance; //distance from attack
    private int damageR;// برد اسلحه
    private int timer; //time needed to work again
    private int timesRemain;
    private GunType gunType;
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
    public enum GunType {
        LASER, DYNAMITE;
    }

}
