package model.plants.plantAbilities;

import model.GameContext;
import model.plants.Plant;
import model.plants.enums.BulletType;
import model.plants.enums.ShootType;

public class Shooters implements BaseAbility{
    @Override
    public void activate(Plant self, GameContext ctx) {
        //check for striker here
        //check for homing (cat-tail - 14 - 15)
    }
    public void shoot (int amount, ShootType shootType, BulletType bulletType){
    }
    public void shootForHoming (){}
}
