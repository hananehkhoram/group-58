package model.zombie.behavior;

public enum ArmorType {
    CONE(370, false),
    BUCKET(1100, true),
    BRICK(2200, false),
    SHOULDER_ARMOR(1600, true),  // شوالیه: شانه‌بند — magnetshroom نمی‌تونه بقاپدش
    SHOULDER_CROWN(1600, true),  // شوالیه: کلاهخود — magnetshroom (صفحه ۳۴ سند)
    NEWSPAPER(190, false);

    public final int baseHealth;
    public final boolean metallic;

    ArmorType(int baseHealth, boolean metallic) {
        this.baseHealth = baseHealth;
        this.metallic = metallic;
    }
}
