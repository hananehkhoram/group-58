package model.plants.upgradeEffect;

public class StatEffect {
    public enum statType {
        HP, COST, DAMAGE, PRODUCTION_TIME, GROW_TIME, ATTACK_SPEED;
    }
    private statType stat;
    private int delta;
}
