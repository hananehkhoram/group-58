package model.zombie.behavior;

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
