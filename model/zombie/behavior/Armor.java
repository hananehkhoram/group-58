package model.zombie.behavior;

import model.GameContext;
import model.zombie.Zombie;

public class Armor implements Behaviors {

    private int armorHP;
    private ArmorType armorType;
    private boolean metallic;
    private double[] layerThresholds; // visual damage layer breakpoints e.g. [0.666, 0.333]

    // Enrage fields (Newspaper zombie: EnragedDamageScale=4, EnragedSpeedScale=4)
    private boolean enrageable;
    private double enragedDamageScale;
    private double enragedSpeedScale;
    private boolean enraged;

    public Armor(ArmorType armorType, int armorHP, boolean metallic, double[] layerThresholds) {
        this.armorType = armorType;
        this.armorHP = armorHP;
        this.metallic = metallic;
        this.layerThresholds = layerThresholds;
        this.enrageable = false;
    }

    /** Constructor for Newspaper zombie. */
    public Armor(ArmorType armorType, int armorHP, boolean metallic, double[] layerThresholds,
                 double enragedDamageScale, double enragedSpeedScale) {
        this(armorType, armorHP, metallic, layerThresholds);
        this.enrageable = true;
        this.enragedDamageScale = enragedDamageScale;
        this.enragedSpeedScale = enragedSpeedScale;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {}

    @Override
    public void onHit(Zombie zombie, int damage) {
        absorb(damage);
    }

    public int absorb(int damage) {
        armorHP -= damage;
        int overflow = 0;
        if (armorHP < 0) {
            overflow = -armorHP;
            armorHP = 0;
        }
        return overflow;
    }

    public void destroy() {
        armorHP = 0;
    }

    @Override
    public boolean isDestroyed() {
        return armorHP <= 0;
    }

    public void afterDestroy(Zombie zombie) {
        if (enrageable && !enraged) {
            enraged = true;
            zombie.setEatDps(zombie.getEatDps() * enragedDamageScale);
            zombie.setSpeed(zombie.getSpeed() * enragedSpeedScale);
        }
    }

    public int getArmorHP() { return armorHP; }
    public ArmorType getArmorType() { return armorType; }
    public boolean isMetallic() { return metallic; }
    public boolean isEnraged() { return enraged; }


}