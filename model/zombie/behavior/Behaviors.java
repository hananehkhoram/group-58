package model.zombie.behavior;

import model.GameContext;
import model.zombie.Zombie;

public interface Behaviors {
    default void onTick (Zombie zombie, GameContext ctx){}
    default boolean isDestroyed () {return false;}
    default void onDeath(Zombie zombie, GameContext ctx) {}

}
