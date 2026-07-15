package model.zombie;

import model.GameContext;
import model.projectile.Damageable;
import model.season.Season;
import model.zombie.behavior.Armor;
import model.zombie.behavior.Behaviors;

import java.util.List;
import java.util.Map;

public class Zombie implements Damageable {
    private String id;
    private String name;
    private int hp;
    private double eatDps;
    private double speed;
    private int wavePointCost;
    private int weight;
    private Map<String, Behaviors> behaviors;
    private List<Effects> effects;
    private Map<String, Object> extraParams;
    private List<Season> seasonsAvailable;
    private double x, y;
    private boolean isBoss = false;

    private boolean isIced = false;
    private double iceHp = 0;

    private boolean isEating = false;

    public Zombie() {}

    public Zombie(String id, String name, int hp, double eatDps,
                  double speed, int wavePointCost, int weight) {
        this.id = id;
        this.name = name;
        this.hp = hp;
        this.eatDps = eatDps;
        this.speed = speed;
        this.wavePointCost = wavePointCost;
        this.weight = weight;
        this.behaviors = ZombieActivator.buildBehaviors(this);
    }

    public void setAsInitialFrozenBlock() {
        this.isIced = true;
        this.iceHp = 600;
    }

    public void update(GameContext ctx, double deltaTime) {
        if (isDead()) return;

        for (Behaviors b : behaviors.values()) {
            b.onTick(this, ctx);
        }

        Armor armor = getArmor();
        if (armor != null && armor.isDestroyed()) {
            armor.afterDestroy(this);
        }

        if (!isEating) {
            double effectiveSpeed = speed;
            if (isIced) effectiveSpeed *= 0.5;
            x -= effectiveSpeed * deltaTime;
        }
    }

    public void takeDamage(double damage) {
        if (isIced) {
            iceHp -= damage;
            if (iceHp <= 0) {
                isIced = false;
                System.out.println("Zombie broke free from ice!");
            }
            return;
        }

        Armor armor = getArmor();
        if (armor != null && !armor.isDestroyed()) {
            armor.onHit(this, (int) damage);
            if (armor.isDestroyed()) {
                double overflow = -armor.getArmorHP();
                if (overflow > 0) hp -= (int) overflow;
            }
        } else {
            hp -= (int) damage;
        }
    }

    public Armor getArmor() {
        Behaviors b = behaviors.get("armor");
        return (b instanceof Armor) ? (Armor) b : null;
    }

    public boolean isDead() { return hp <= 0; }

    // --- Damageable ---

    @Override
    public int getRow() {
        return (int) y;
    }

    @Override
    public void takeDamage(int amount) {
        takeDamage((double) amount); // از منطق آرمور/یخ موجود استفاده می‌کند
    }

    @Override
    public void takeArmorPiercingDamage(int amount) {
        // برخلاف takeDamage عادی، از بلوک armor رد می‌شود و مستقیم به جان اصلی می‌زند
        hp -= amount;
    }

    @Override
    public void meltIce() {
        if (isIced) {
            iceHp = 0;
            isIced = false;
        }
        // مکانیزم پیوسته‌ی ذوب (۶۰ سلامتی/ثانیه در مجاورت گیاه آتشین) اینجا نیست؛
        // آن باید جدا در GameEngine روی زامبی‌های مجاور یک گیاه آتشین tick بخورد.
    }

    @Override
    public void applySlowOrFreeze() {
        if (!isIced) {
            isIced = true;
            iceHp = 300; // TODO: مقدار دقیق را طبق سند/csv تنظیم کنید
        }
    }

    // --- Getters / Setters ---

    public String getId() { return id; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public double getEatDps() { return eatDps; }
    public double getSpeed() { return speed; }
    public int getWavePointCost() { return wavePointCost; }
    public int getWeight() { return weight; }
    public Map<String, Behaviors> getBehaviors() { return behaviors; }
    public List<Effects> getEffects() { return effects; }
    public Map<String, Object> getExtraParams() { return extraParams; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isIced() { return isIced; }
    public double getIceHp() { return iceHp; }
    public boolean isEating() { return isEating; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setHp(int hp) { this.hp = hp; }
    public void setEatDps(double eatDps) { this.eatDps = eatDps; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setWavePointCost(int wpc) { this.wavePointCost = wpc; }
    public void setWeight(int weight) { this.weight = weight; }
    public void setEffects(List<Effects> effects) { this.effects = effects; }
    public void setExtraParams(Map<String, Object> p) { this.extraParams = p; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setIced(boolean iced) { this.isIced = iced; }
    public void setIceHp(double iceHp) { this.iceHp = iceHp; }
    public void setEating(boolean eating) { this.isEating = eating; }
    public void setBehaviors (Map <String, Behaviors> behaviors){this.behaviors = behaviors;}

    public String zombieInfo() {
        return String.format("[%s] \n   HP:%d \n    Armors:%s \n    Position:%f , %f \n     Effects:%s",
                name, hp, getStringArmor(), x, y, getStringEffects());
    }

    private String getStringArmor() {
        StringBuilder sb = new StringBuilder();
        for (Behaviors b : behaviors.values()) {
            if (b instanceof Armor) {
                sb.append("\n").append(((Armor) b).getArmorType())
                        .append(": ").append(((Armor) b).getArmorHP());
            }
        }
        return sb.toString();
    }

    private String getStringEffects() {
        if (effects == null) return "none";
        StringBuilder sb = new StringBuilder();
        for (Effects e : effects) {
            sb.append("\n").append(e.toString());
        }
        return sb.toString();
    }

    public boolean isBoss() {
        return isBoss;
    }

    public void setBoss(boolean boss) {
        isBoss = boss;
    }
}