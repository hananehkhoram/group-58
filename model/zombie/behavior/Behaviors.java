package model.zombie.behavior;

import model.GameContext;
import model.zombie.Zombie;

public interface Behaviors {
    void onTick (Zombie zombie, GameContext ctx);
    void onHit (Zombie zombie, int damage); // after doing the action
    boolean isDestroyed ();

}
