package model.zombie.behavior;

import model.GameContext;
import model.zombie.Zombie;

public class Shooting implements Behaviors{
    private ShootingType shootingType;
    private int rate;
    private int amount;

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
    public void makeTomb(GameContext ctx){}

    public enum ShootingType{
    GARGANTUAR, TOMBRAISER, HUNTER, FISHERMAN;
    }
}
