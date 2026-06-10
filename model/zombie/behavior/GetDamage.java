package model.zombie.behavior;

import model.zombie.Zombie;

public class GetDamage implements Behaviors{
    private GetDamageType type;
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
    public void giveBackSuns (){} //after dying

    public enum GetDamageType{}
}
