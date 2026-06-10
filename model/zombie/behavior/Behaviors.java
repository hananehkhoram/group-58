package model.zombie.behavior;

import model.zombie.Zombie;

public interface Behaviors {
    void onTick (Zombie zombie);
    void onHit (Zombie zombie, int damage); // after doing the action
    boolean isDestroyed ();

}
