package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.enums.BulletType;
import model.plants.enums.ShootType;
import model.plants.plantFoodEffect.PlantFoodMode;

public class Shooters implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {
        //check for striker here
        //check for homing (cat-tail - 14 - 15)
    }
    public void shoot(int amount, ShootType shootType, BulletType bulletType) {
    }
    public void shootForHoming() {}

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // BARRAGE for most, MULTI_TARGET_BURST for Caulipower/Electric Blueberry, SELF_RESET for Sea/Puff-shroom
    }

}
