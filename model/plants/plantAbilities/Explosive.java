package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;

public class Explosive implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {

    }
    public void explosion(int time, int row, int col) {}
    public void waterExplosion() {}
    public void ice() {}
    public void forIcedCave() {}
    public void forEgyptAndDarkEra() {}

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // Most Explosives are consumable and have no Plant Food (mode == NONE);
        // mines (Potato Mine, Primal Potato Mine) instantly arm + clone themselves.
    }

}
