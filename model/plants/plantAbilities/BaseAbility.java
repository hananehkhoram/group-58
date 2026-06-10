package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;

public interface BaseAbility {
    void activate(Plant self, GameContext ctx);
}
