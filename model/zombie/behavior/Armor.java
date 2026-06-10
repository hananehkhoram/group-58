package model.zombie.behavior;

import model.zombie.Zombie;

public class Armor implements Behaviors {

    private int armorHP;
    private ArmorType armorType;
    private boolean rolling;

    @Override
    public void onTick(Zombie zombie) {

    }

    @Override
    public void onHit(Zombie zombie, int damage) {

    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    public void afterDestroy() {
    }

    public enum ArmorType {
        CONE, BUCKET, BRICK, ICE_BLOCK, SHOULDER_CROWN,
        SARCOPHAGUS, SURFBOARD, KNIGHT_SHIELD, ARCADE, PARASOL, PIANO, NEWSPAPER, DRAGON_IMP;
    }


}
