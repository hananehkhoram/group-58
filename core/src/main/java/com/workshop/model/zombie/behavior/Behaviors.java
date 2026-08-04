package com.workshop.model.zombie.behavior;

import com.workshop.model.GameContext;
import com.workshop.model.zombie.Zombie;

public interface Behaviors {
    default void onTick (Zombie zombie, GameContext ctx){}
    default boolean isDestroyed () {return false;}
    default void onDeath(Zombie zombie, GameContext ctx) {}

}
