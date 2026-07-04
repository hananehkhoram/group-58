package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.zombie.Zombie;

import java.util.List;

public class Explosive implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {

    }
    public void explosion(int time, int plantRow, int plantCol, String damage, Plant plant, GameContext ctx) {
        for (int r = plantRow - 1; r <= plantRow + 1; r++) {
            for (int c = plantCol - 1; c <= plantCol + 1; c++) {
                if (r >= 0 && c >= 0){
                    List<Zombie> targetsInTile = ctx.findTargets(r, c, )
                }
            }
        }
    }
    public void waterExplosion() {}
    public void ice(Plant plant, GameContext ctx) {}
    public void forIcedCave() {}
    public void forEgyptAndDarkEra() {}

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // Most Explosives are consumable and have no Plant Food (mode == NONE);
        // mines (Potato Mine, Primal Potato Mine) instantly arm + clone themselves.
    }

}
