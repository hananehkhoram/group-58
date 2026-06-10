package model.zombie.behavior;

import model.zombie.Zombie;

import java.util.List;

public class Damage implements Behaviors{
    private List<TargetType> targetType;
    int distance;
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
    public boolean isRecovered(){ return false;}
    public void destroy(){} //destroy the target

    public enum TargetType {
        PLANT, HYPNOTIZED_ZOMBIE, EXPLORER;
    }
}
