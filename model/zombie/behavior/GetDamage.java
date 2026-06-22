package model.zombie.behavior;

import model.zombie.Zombie;

public class GetDamage implements Behaviors {

    private GetDamageType type;

    public GetDamage(GetDamageType type) {
        this.type = type;
    }

    @Override
    public void onTick(Zombie zombie) {}

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public void giveBackSuns() {} // Ra Zombie: drops stolen suns on death

    public GetDamageType getType() { return type; }

    public enum GetDamageType {
        NORMAL,         // takes damage normally
        ARMORED,        // damage goes to armor first
        SHIELDED,       // has a shield layer (e.g. Knight)
        SUN_THIEF       // Ra Zombie: drops suns on death
    }
}
