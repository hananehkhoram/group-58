package com.workshop.model.zombie;

import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.LootItem;
import com.workshop.model.projectile.Damageable;
import com.workshop.model.season.Season;
import com.workshop.model.zombie.behavior.*;
import com.workshop.view.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Zombie implements Damageable {
    private static final Random random = new Random();
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
    private List<Effects> effects = new ArrayList<>();
    private Map<String, Object> extraParams;
    private double x, y;
    private boolean isBoss = false;
    private boolean beingSucked = false;

    private long spawnTick;


    private boolean isIced = false;
    private double iceHp = 0;
    private static final double BUTTER_STUN_SECONDS = 4.0;
    private static final double BOSS_STUN_SECONDS = 8.0;
    private double butterRemaining;
    private double stunRemaining;
    private boolean initialFrozenBlock = false;
    private static final double SANDSTORM_DASH_SPEED = 3.6;
    private static final float SANDSTORM_LAND_SECONDS = 0.75f;

    private double sandstormTargetX;
    private float sandstormDelay;
    private boolean sandstormLanded;
    private float sandstormLandTime;
    private boolean enteredViaSandstorm = false;

    public void startSandstormEntry(double targetX, float delaySeconds) {
        enteredViaSandstorm = true;
        sandstormTargetX = targetX;
        sandstormDelay = Math.max(0f, delaySeconds);
        sandstormLanded = false;
        sandstormLandTime = 0f;
    }

    public boolean isSandstormLanded() {
        return sandstormLanded;
    }

    public float getSandstormLandTime() {
        return sandstormLandTime;
    }

    private boolean updateSandstormDash(double deltaTime) {
        if (!enteredViaSandstorm) {
            return false;
        }

        setEating(false);

        if (sandstormDelay > 0f) {
            sandstormDelay -= (float) deltaTime;
            return true;
        }

        if (!sandstormLanded) {
            x -= SANDSTORM_DASH_SPEED * deltaTime;
            if (x <= sandstormTargetX) {
                x = sandstormTargetX;
                sandstormLanded = true;
                sandstormLandTime = 0f;
            }
            return true;
        }

        sandstormLandTime += (float) deltaTime;
        if (sandstormLandTime >= SANDSTORM_LAND_SECONDS) {
            enteredViaSandstorm = false;
        }
        return false;
    }

    private boolean isEating = false;
    private boolean movingBackward = false;
    private boolean ashed;
    private boolean ashFinished;
    private int maxHp;
    private boolean lostArm;
    private boolean lostHead;
    private boolean deathAnimFinished;
    private boolean pendingArmDrop;
    private boolean pendingHeadDrop;
    private final java.util.ArrayDeque<ArmorType> pendingArmorPops = new java.util.ArrayDeque<>();

    public Zombie() {
        this.effects = new ArrayList<>();
    }

    public Zombie(String id, String name, int hp, double eatDps,
                  double speed, int wavePointCost, int weight) {
        this.id = id;
        this.name = name;
        this.hp = hp;
        this.eatDps = eatDps;
        this.speed = speed;
        this.wavePointCost = wavePointCost;
        this.weight = weight;
        this.maxHp = hp;
        this.isBoss = BossZombieRegistry.isBossId(id);
        this.behaviors = ZombieActivator.buildBehaviors(this);
        this.effects = new  ArrayList<>();
    }

    public void setAsInitialFrozenBlock() {
        initialFrozenBlock = true;
        isIced = true;
        iceHp = 600;

        if (!effects.contains(Effects.FROZEN)) {
            effects.add(Effects.FROZEN);
        }
    }

    public void update(GameContext ctx, double deltaTime) {
        if (isDead()) {
            if (!ashed && random.nextInt(100) < 5) {
                ctx.addLoot(new LootItem(LootItem.LootType.SEED, (int) getX(), getRow()));}
            return;
        }

        if (effects.contains(Effects.HYPNOTIZED) && this.x > Level.COLS) {
            this.hp = 0;
        }

        if (isBoss && isIced) {
            isIced = false;
            iceHp = 0;
            initialFrozenBlock = false;
            if (effects != null) {
                effects.remove(Effects.FROZEN);
            }
        }

        if (butterRemaining > 0) {
            butterRemaining -= deltaTime;
            if (butterRemaining <= 0) {
                butterRemaining = 0;
                if (effects != null) {
                    effects.remove(Effects.BUTTERED);
                }
            } else {
                setEating(false);
                return;
            }
        }

        if (stunRemaining > 0) {
            stunRemaining -= deltaTime;
            if (stunRemaining < 0) {
                stunRemaining = 0;
            }
            setEating(false);
        }

        if (initialFrozenBlock) {
            return;
        }

        if (updateSandstormDash(deltaTime)) {
            return;
        }

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

        if (!isEating && !airborne && !isBoss && stunRemaining <= 0 && !beingSucked) {
            double effectiveSpeed = speed;
            if (isIced) {
                effectiveSpeed *= 0.5;
            }
            if (isButtered()) {
                effectiveSpeed *= 0.45;
            }
            boolean shouldMoveRight = movingBackward || effects.contains(Effects.HYPNOTIZED);

            x += shouldMoveRight ? effectiveSpeed * deltaTime : -effectiveSpeed * deltaTime;
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

    public ZombossSummon getZomboss() {
        if (behaviors == null) {
            return null;
        }
        Behaviors b = behaviors.get("zombossSummon");
        return (b instanceof ZombossSummon) ? (ZombossSummon) b : null;
    }

    public Submerge getSubmerge() {
        Behaviors b = behaviors.get("submerge");
        return (b instanceof Submerge) ? (Submerge) b : null;
    }

    public void setMovingBackward(boolean movingBackward)
    { this.movingBackward = movingBackward; }
    public boolean isMovingBackward() { return movingBackward; }

    public boolean isFacingRight() {
        return movingBackward || speed < 0;
    }

    public void takeDamage(double damage) {

        if (initialFrozenBlock) {
            iceHp -= damage;

            if (iceHp <= 0) {
                initialFrozenBlock = false;
                isIced = false;

                if (effects != null) {
                    effects.remove(Effects.FROZEN);
                }

                Console.showMessage(
                    "Zombie broke free from ice!"
                );
            }

            return;
        }

        if (isIced) {
            iceHp -= damage;
            if (iceHp <= 0) {
                isIced = false;
                if (effects != null) effects.remove(Effects.FROZEN);
                Console.showMessage("Zombie broke free from ice!");
            }
        }

        int remaining = (int) damage;

        Armor primary = getArmor();
        if (primary != null && !primary.isDestroyed()) {
            remaining = primary.absorb(remaining);
            if (primary.isDestroyed()) {
                pendingArmorPops.addLast(primary.getArmorType());
            }
            if (remaining <= 0) return;
        }

        Armor secondary = getSecondaryArmor();
        if (secondary != null && !secondary.isDestroyed()) {
            remaining = secondary.absorb(remaining);
            if (secondary.isDestroyed()) {
                pendingArmorPops.addLast(secondary.getArmorType());
            }
            if (remaining <= 0) return;
        }

        if (isBoss) {
            applyBossBodyDamage(remaining);
        } else {
            hp -= remaining;
            noteBodyInjury();
        }
        if (hp <= 0){
            Console.showMessage("Zombie of type "+this.getName() +
                " is dead at " + this.getX() + ", " + this.getY());
        }
    }

    public void takeExplosionDamage(double damage) {
        boolean wasAlive = !isDead();
        takeDamage(damage);
        if (wasAlive && isDead()) {
            ashed = true;
            ashFinished = false;
            pendingHeadDrop = false;
            pendingArmDrop = false;
        }
    }

    public boolean isAshed() {
        return ashed;
    }

    public boolean isAshFinished() {
        return ashFinished;
    }

    public void markAshFinished() {
        ashFinished = true;
    }

    public Armor getArmor() {
        Behaviors b = behaviors.get("armor");
        return (b instanceof Armor) ? (Armor) b : null;
    }

    public Armor getSecondaryArmor() {
        Behaviors b = behaviors.get("armor2");
        return (b instanceof Armor) ? (Armor) b : null;
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

    public boolean occupiesRow(int row) {
        int top = (int) y;
        if (!isBoss) {
            return top == row;
        }
        return row == top || row == top + 1;
    }

    public int healthPhase() {
        int cap = getMaxHp();
        if (hp <= 0 || cap <= 0) {
            return 0;
        }
        return (int) Math.ceil(hp * 3.0 / cap);
    }

    public void applyStun(double seconds) {
        stunRemaining = Math.max(stunRemaining, seconds);
        setEating(false);
    }

    public boolean isStunned() {
        return stunRemaining > 0;
    }

    @Override
    public void takeDamage(int amount) {
        takeDamage((double) amount);
    }

    @Override
    public void takeArmorPiercingDamage(int amount) {
        if (isBoss) {
            applyBossBodyDamage(amount);
        } else {
            hp -= amount;
            noteBodyInjury();
        }
    }

    private void applyBossBodyDamage(int remaining) {
        if (remaining <= 0) {
            return;
        }
        int cap = getMaxHp();
        int phase = healthPhase();
        if (phase <= 0) {
            hp -= remaining;
            return;
        }

        if (isStunned()) {
            hp = Math.max(phaseFloorHp(phase), hp - remaining);
            return;
        }

        int newHp = hp - remaining;
        int newPhase = phaseForHp(newHp, cap);
        if (newPhase < phase) {
            int droppedTo = phase - 1;
            if (droppedTo <= 0) {
                hp = newHp;
                return;
            }
            hp = phaseCapHp(droppedTo);
            applyStun(BOSS_STUN_SECONDS);
            Console.showMessage(name + " is stunned!");
            return;
        }
        hp = newHp;
    }

    private static int phaseForHp(int current, int cap) {
        if (current <= 0 || cap <= 0) {
            return 0;
        }
        return (int) Math.ceil(current * 3.0 / cap);
    }

    private int phaseCapHp(int phase) {
        return Math.max(1, (int) Math.floor(getMaxHp() * phase / 3.0));
    }

    private int phaseFloorHp(int phase) {
        if (phase <= 1) {
            return 1;
        }
        return phaseCapHp(phase - 1) + 1;
    }

    private void noteBodyInjury() {
        if (maxHp <= 0) {
            maxHp = Math.max(hp, 1);
        }
        if (!lostArm && hp > 0 && hp <= maxHp / 2) {
            lostArm = true;
            pendingArmDrop = true;
        }
        if (!lostHead && hp <= 0 && !ashed) {
            lostHead = true;
            pendingHeadDrop = true;
        }
    }

    public ArmorType pollArmorPop() {
        return pendingArmorPops.pollFirst();
    }
    public boolean consumeArmDrop() {
        if (!pendingArmDrop) {
            return false;
        }
        pendingArmDrop = false;
        return true;
    }

    public boolean consumeHeadDrop() {
        if (!pendingHeadDrop) {
            return false;
        }
        pendingHeadDrop = false;
        return true;
    }

    public boolean hasLostArm() {
        return lostArm;
    }
    public boolean isDeathAnimFinished() {
        return deathAnimFinished;
    }
    public void markDeathAnimFinished() {
        deathAnimFinished = true;
    }
    public int getMaxHp() {
        return maxHp > 0 ? maxHp : Math.max(hp, 1);
    }
    @Override
    public void meltIce() {
        if (isIced) {
            iceHp = 0;
            isIced = false;
            initialFrozenBlock = false;

            if (effects != null) {
                effects.remove(Effects.FROZEN);
            }
        }
    }

    public void meltIce(double amount){
        if (isIced){
            iceHp -= amount;
            if (iceHp <= 0){
                iceHp = 0;
                isIced = false;
                initialFrozenBlock = false;
                if (effects != null) {
                    effects.remove(Effects.FROZEN);
                }
            }
        }
    }

    @Override
    public void applySlowOrFreeze() {
        if (isBoss) {
            return;
        }
        if (!isIced) {
            isIced = true;
            effects.add(Effects.FROZEN);
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

    public void setMirroredIceState(boolean iced, boolean initialFrozenBlock, double iceHp) {
        this.isIced = iced;
        this.initialFrozenBlock = initialFrozenBlock;
        this.iceHp = iceHp;
        if (iced) {
            if (!effects.contains(Effects.FROZEN)) {
                effects.add(Effects.FROZEN);
            }
        } else {
            effects.remove(Effects.FROZEN);
        }
    }

    public void setMirroredDeathState(boolean ashed, boolean ashFinished, boolean deathAnimFinished) {
        this.ashed = ashed;
        this.ashFinished = ashFinished;
        this.deathAnimFinished = deathAnimFinished;
    }

    public double getSpeed() { return speed; }
    public int getWavePointCost() { return wavePointCost; }
    public int getWeight() { return weight; }
    public Map<String, Behaviors> getBehaviors() { return behaviors; }
    public Map<String, Object> getExtraParams() { return extraParams; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isIced() { return isIced; }

    public void applyButter() {
        if (isBoss) {
            return;
        }
        butterRemaining = BUTTER_STUN_SECONDS;
        if (effects != null && !effects.contains(Effects.BUTTERED)) {
            effects.add(Effects.BUTTERED);
        }
    }

    public boolean isButtered() {
        return butterRemaining > 0;
    }

    public boolean isInitialFrozenBlock() {
        return initialFrozenBlock;
    }
    public boolean isEnteredViaSandstorm() { return enteredViaSandstorm; }
    public boolean isEating() { return isEating; }
    public long getSpawnTick() {return spawnTick;}

    public void setSpawnTick(long spawnTick) {this.spawnTick = spawnTick;}
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setHp(int hp) {
        this.hp = hp;
        if (maxHp <= 0) {
            this.maxHp = hp;
        }
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }
    public void setEatDps(double eatDps) { this.eatDps = eatDps; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setWavePointCost(int wpc) { this.wavePointCost = wpc; }
    public void setWeight(int weight) { this.weight = weight; }
    public void setExtraParams(Map<String, Object> p) { this.extraParams = p; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
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
    public void setBoss(boolean boss) {
        this.isBoss = boss;
    }

    public void setBeingSucked(boolean beingSucked) {
        this.beingSucked = beingSucked;
    }

    public boolean isBeingSucked() {
        return beingSucked;
    }
    public void setRow (int r){this.y = r;}
    public boolean searchEffect(Effects effect) {
        if (effect == null || this.effects == null) return false;
        return this.effects.contains(effect);
    }
    public List<Effects> getEffect() {return effects;}

    public double getIceHp() {
        return iceHp;
    }
}
