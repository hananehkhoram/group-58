package model.zombie;

import model.GameContext;
import model.projectile.Damageable;
import model.season.Season;
import model.zombie.behavior.Armor;
import model.zombie.behavior.ArmorType;
import model.zombie.behavior.Behaviors;
import model.zombie.behavior.Jumper;
import model.zombie.behavior.ProjectileDeflector;
import model.zombie.behavior.Submerge;
import view.ConsoleView;

import java.util.List;
import java.util.Map;

public class Zombie implements Damageable {
    private String id;
    private String name;
    private int hp;
    private double eatDps;
    private double eatDamageAccumulator = 0.0; // باقیمانده‌ی اعشاری دمیج خوردن گیاه بین تیک‌ها
    private long lastEatTick = -1; // آخرین tickِ TimeManager که دمیج خوردن ازش حساب شده
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
    private boolean movingBackward = false;

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
        Armor secondaryArmor = getSecondaryArmor();
        if (secondaryArmor != null && secondaryArmor.isDestroyed()) {
            secondaryArmor.afterDestroy(this);
        }

        boolean airborne = getJumper() != null && !getJumper().isLanded();

        if (!isEating && !airborne) {
            double effectiveSpeed = speed;
            if (isIced) effectiveSpeed *= 0.5;
            x += movingBackward ? effectiveSpeed * deltaTime : -effectiveSpeed * deltaTime;
        }
    }

    public Jumper getJumper() {
        Behaviors b = behaviors.get("jumper");
        return (b instanceof Jumper) ? (Jumper) b : null;
    }

    public ProjectileDeflector getDeflector() {
        Behaviors b = behaviors.get("deflector");
        return (b instanceof ProjectileDeflector) ? (ProjectileDeflector) b : null;
    }

    public Submerge getSubmerge() {
        Behaviors b = behaviors.get("submerge");
        return (b instanceof Submerge) ? (Submerge) b : null;
    }

    public void setMovingBackward(boolean movingBackward) { this.movingBackward = movingBackward; }
    public boolean isMovingBackward() { return movingBackward; }

    public void takeDamage(double damage) {
        if (isIced) {
            iceHp -= damage;
            if (iceHp <= 0) {
                isIced = false;
                ConsoleView.showMessage("Zombie broke free from ice!");
            }
            return;
        }

        int remaining = (int) damage;

        Armor primary = getArmor();
        if (primary != null && !primary.isDestroyed()) {
            remaining = primary.absorb(remaining);
            if (remaining <= 0) return;
        }

        Armor secondary = getSecondaryArmor();
        if (secondary != null && !secondary.isDestroyed()) {
            remaining = secondary.absorb(remaining);
            if (remaining <= 0) return;
        }

        hp -= remaining;
        if (hp <= 0){
            ConsoleView.showMessage("Zombie of type "+this.getName() +" is dead at " + this.getX() + ", " + this.getY());
        }
    }

    public Armor getArmor() {
        Behaviors b = behaviors.get("armor");
        return (b instanceof Armor) ? (Armor) b : null;
    }

    public Armor getSecondaryArmor() {
        Behaviors b = behaviors.get("armor2");
        return (b instanceof Armor) ? (Armor) b : null;
    }

    public boolean removeArmorOfType(ArmorType type) {
        Armor primary = getArmor();
        if (primary != null && primary.getArmorType() == type && !primary.isDestroyed()) {
            primary.destroy();
            return true;
        }
        Armor secondary = getSecondaryArmor();
        if (secondary != null && secondary.getArmorType() == type && !secondary.isDestroyed()) {
            secondary.destroy();
            return true;
        }
        return false;
    }

    public boolean isDead() { return hp <= 0; }

    @Override
    public String name() {
        return name;
    }

    // --- Damageable ---

    @Override
    public int getRow() {
        return (int) y;
    }

    @Override
    public void takeDamage(int amount) {
        takeDamage((double) amount);
    }

    @Override
    public void takeArmorPiercingDamage(int amount) {
        hp -= amount;
    }

    @Override
    public void meltIce() {
        if (isIced) {
            iceHp = 0;
            isIced = false;
        }
    }

    @Override
    public void applySlowOrFreeze() {
        if (!isIced) {
            isIced = true;
            iceHp = 300;
        }
    }

    public Armor removeArmor() {
        Armor primary = getArmor();
        if (primary != null && !primary.isDestroyed()) {
            primary.afterDestroy(this);
            behaviors.remove("armor");

            return primary;
        }
        Armor secondary = getSecondaryArmor();
        if (secondary != null && !secondary.isDestroyed()) {
            secondary.afterDestroy(this);
            behaviors.remove("armor2");
            return secondary;
        }

        return null;
    }

    // --- Getters / Setters ---

    public String getId() { return id; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public double getEatDps() { return eatDps; }

    private static final int TICKS_PER_SECOND = 10;

    public void resetEatClock(GameContext ctx) {
        lastEatTick = ctx.getTimeManager().getTotalTicks();
        eatDamageAccumulator = 0;
    }

    public int consumeEatDamage(GameContext ctx) {
        long now = ctx.getTimeManager().getTotalTicks();
        if (lastEatTick < 0) lastEatTick = now;
        long elapsedTicks = now - lastEatTick;
        lastEatTick = now;

        eatDamageAccumulator += eatDps * (elapsedTicks / (double) TICKS_PER_SECOND);
        int wholeDamage = (int) eatDamageAccumulator;
        eatDamageAccumulator -= wholeDamage;
        return wholeDamage;
    }
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
        return String.format("[%s] \n   HP:%d \n    Armors:%s \n    Position: %f , %f \n    Effects:%s",
                name, hp, getStringArmor(), x, y, getStringEffects());
    }

    private String getStringArmor() {
        StringBuilder sb = new StringBuilder();
        for (Behaviors b : behaviors.values()) {
            if (b instanceof Armor) {
                sb.append("\n       ").append(((Armor) b).getArmorType())
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
    public void setRow (int r){this.y = r;}
}