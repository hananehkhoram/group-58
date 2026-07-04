package model.plants.plantAbilities;

import model.GameContext;
import model.Projectile.Projectile;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.plants.enums.BulletType;
import model.plants.enums.ShootType;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.zombie.Zombie;

import java.util.List;

public class Shooters implements BaseAbility {
    @Override
    public void activate(Plant self, GameContext ctx) {
        //check for striker here
        //check for homing (cat-tail - 14 - 15)
    }
    public void shoot(String damage, int amount, String interval, ShootType shootType, BulletType bulletType, Plant self, GameContext ctx) {
        //Projectile projectile = new Projectile(self, ctx);
        int currentSecond = ctx.getTimeManager().getTotalSeconds();

        if (!interval.equals("everyRound")) {

            int intervalOfPlant = Integer.parseInt(interval);

            if (currentSecond - self.getLastActionSecond() >= intervalOfPlant){
                boolean hasShot = false;

                if (shootType == shootType.RANDOM_HOMING || shootType == shootType.NEAREST_TARGET) {

                    this.shootForHoming();
                    hasShot = true;
                }else {

                    TargetingMode mode = TargetingMode.NONE;

                    switch ((shootType)){
                        case STRAIGHT:
                        case STRAIGHT_SEQUENTIAL:
                            mode = TargetingMode.FIRST_IN_LANE;
                            break;
                        case NEAREST_TARGET:
                            mode = TargetingMode.NEAREST;
                            break;
                        case RANDOM_HOMING:
                        case RANDOM_INSTANT:
                            mode = TargetingMode.RANDOM;
                            break;
                        default:
                            mode = TargetingMode.FIRST_IN_LANE;
                            break;
                    }
                    List<Zombie> targets = ctx.findTargets(self.getRow(), self.getCol(), mode);

                    if (targets != null && !targets.isEmpty()){
                        Zombie target = targets.get(0);

                        int damageOfPlant = Integer.parseInt(damage);
                        target.setHp(target.getHp() - damageOfPlant);
                        hasShot = true;
                    }
                }

                if (hasShot){
                    self.setLastActionSecond(currentSecond);
                }
            }
        }
    }
    public void shootForHoming() {}

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        // BARRAGE for most, MULTI_TARGET_BURST for Caulipower/Electric Blueberry, SELF_RESET for Sea/Puff-shroom
    }

}
