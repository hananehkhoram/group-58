package model.zombie.behavior;

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
    public void onTick(Zombie zombie) {}

    @Override
    public void onHit(Zombie zombie, int damage) {
        armorHP -= damage;
        if (armorHP < 0) armorHP = 0;
    }

    @Override
    public boolean isDestroyed() {
        return armorHP <= 0;
    }

    /**
     * Called by the game loop when isDestroyed() first becomes true.
     * If enrageable (Newspaper), multiplies the zombie's speed and eatDps.
     */
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

    public enum ArmorType {
        CONE(370, false),
        BUCKET(1100, true),
        BRICK(2200, false),
        SHOULDER_ARMOR(1600, true),
        SHOULDER_CROWN(1600, true),
        NEWSPAPER(800, false),
        SARCOPHAGUS(0, false),
        SURFBOARD(0, false),
        KNIGHT_SHIELD(0, true),
        ARCADE(0, false),
        PARASOL(0, false),
        PIANO(0, false),
        DRAGON_IMP(0, false);

        public final int baseHealth;
        public final boolean metallic;

        ArmorType(int baseHealth, boolean metallic) {
            this.baseHealth = baseHealth;
            this.metallic = metallic;
        }
    }
}