package model.zombie.behavior;

import model.GameContext;
import model.zombie.Zombie;
//
public class GetDamage implements Behaviors {

    private GetDamageType type;

    public GetDamage(GetDamageType type) {
        this.type = type;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {}

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public void onDeath(Zombie zombie, GameContext ctx) {
        if (type == GetDamageType.SUN_THIEF) {
            Behaviors b = zombie.getBehaviors().get("sunThief");
            if (b instanceof SunThief sunThief) {
                sunThief.giveBackSuns(ctx);
            }
        }
    }

    @Override
    public boolean isDestroyed() { return false; }

    public GetDamageType getType() { return type; }

    public enum GetDamageType {
        NORMAL,         // takes damage normally
        ARMORED,        // damage goes to armor first
        SHIELDED,       // has a shield layer (e.g. Knight)
        SUN_THIEF       // Ra Zombie: drops suns on death
    }
}