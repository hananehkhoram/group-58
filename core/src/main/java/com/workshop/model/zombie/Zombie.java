package com.workshop.model.zombie;

import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.LootItem;
import com.workshop.model.projectile.Damageable;
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
    private double speed;
    private int wavePointCost;
    private int weight;
    private Map<String, Behaviors> behaviors;
    private List<Effects> effects = new ArrayList<>();
    private Map<String, Object> extraParams;
    private double x, y;
    private boolean isBoss;
    private boolean beingSucked;
    private long spawnTick;
    private boolean isEating;
    private boolean movingBackward;
    private int maxHp;

    private final ZombieSandstorm sandstorm = new ZombieSandstorm();
    private final ZombieStatusEffects status = new ZombieStatusEffects();
    private final ZombieInjuryVisuals injury = new ZombieInjuryVisuals();
    private final ZombieEatClock eatClock = new ZombieEatClock();

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
        this.effects = new ArrayList<>();
    }

    public void setAsInitialFrozenBlock() { status.setAsInitialFrozenBlock(effects); }
    public void startSandstormEntry(double targetX, float delaySeconds) { sandstorm.start(targetX, delaySeconds); }
    public boolean isSandstormLanded() { return sandstorm.isLanded(); }
    public float getSandstormLandTime() { return sandstorm.getLandTime(); }
    public boolean isEnteredViaSandstorm() { return sandstorm.isActive(); }

    public void update(GameContext ctx, double deltaTime) {
        if (isDead()) {
            if (!injury.isAshed() && random.nextInt(100) < 5) {
                ctx.addLoot(new LootItem(LootItem.LootType.SEED, (int) getX(), getRow()));
            }
            return;
        }

        if (effects.contains(Effects.HYPNOTIZED) && this.x > Level.COLS) {
            this.hp = 0;
        }

        if (status.tick(this, effects, isBoss, deltaTime)) {
            return;
        }
        if (sandstorm.update(this, deltaTime)) {
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
        if (!isEating && !airborne && !isBoss && !status.isStunned() && !beingSucked) {
            double effectiveSpeed = speed;
            if (status.isIced()) {
                effectiveSpeed *= 0.5;
            }
            if (status.isButtered()) {
                effectiveSpeed *= 0.45;
            }
            boolean shouldMoveRight = movingBackward || effects.contains(Effects.HYPNOTIZED);
            x += shouldMoveRight ? effectiveSpeed * deltaTime : -effectiveSpeed * deltaTime;
        }
    }

    public Jumper getJumper() { return ZombieBehaviorAccess.jumper(behaviors); }
    public ProjectileDeflector getDeflector() { return ZombieBehaviorAccess.deflector(behaviors); }
    public ZombossSummon getZomboss() { return ZombieBehaviorAccess.zomboss(behaviors); }
    public Submerge getSubmerge() { return ZombieBehaviorAccess.submerge(behaviors); }
    public Armor getArmor() { return ZombieBehaviorAccess.armor(behaviors); }
    public Armor getSecondaryArmor() { return ZombieBehaviorAccess.secondaryArmor(behaviors); }

    public void setMovingBackward(boolean movingBackward) { this.movingBackward = movingBackward; }
    public boolean isMovingBackward() { return movingBackward; }
    public boolean isFacingRight() { return movingBackward || speed < 0; }

    public void takeDamage(double damage) {
        if (status.absorbFrozenBlockDamage(damage, effects)) {
            return;
        }
        status.absorbIceShellDamage(damage, effects);

        int remaining = ZombieBehaviorAccess.absorbThroughArmor(this, (int) damage, injury);
        if (remaining <= 0) {
            return;
        }

        if (isBoss) {
            ZombieBossCombat.applyBodyDamage(this, remaining, status);
        } else {
            hp -= remaining;
            if (maxHp <= 0) {
                maxHp = Math.max(hp, 1);
            }
            injury.noteBodyInjury(hp, maxHp);
        }
        if (hp <= 0) {
            Console.showMessage("Zombie of type " + this.getName()
                + " is dead at " + this.getX() + ", " + this.getY());
        }
    }

    public void takeExplosionDamage(double damage) {
        boolean wasAlive = !isDead();
        takeDamage(damage);
        if (wasAlive && isDead()) {
            injury.onExplosionKill();
        }
    }

    public boolean isAshed() { return injury.isAshed(); }
    public boolean isAshFinished() { return injury.isAshFinished(); }
    public void markAshFinished() { injury.markAshFinished(); }
    public boolean isDead() { return hp <= 0; }

    @Override
    public String name() { return name; }

    @Override
    public int getRow() { return (int) y; }

    public boolean occupiesRow(int row) {
        int top = (int) y;
        if (!isBoss) {
            return top == row;
        }
        return row == top || row == top + 1;
    }

    public int healthPhase() {
        return ZombieBossCombat.phaseForHp(hp, getMaxHp());
    }

    public void applyStun(double seconds) { status.applyStun(seconds, this); }
    public boolean isStunned() { return status.isStunned(); }

    @Override
    public void takeDamage(int amount) { takeDamage((double) amount); }

    @Override
    public void takeArmorPiercingDamage(int amount) {
        if (isBoss) {
            ZombieBossCombat.applyBodyDamage(this, amount, status);
        } else {
            hp -= amount;
            if (maxHp <= 0) {
                maxHp = Math.max(hp, 1);
            }
            injury.noteBodyInjury(hp, maxHp);
        }
    }

    public ArmorType pollArmorPop() { return injury.pollArmorPop(); }
    public boolean consumeArmDrop() { return injury.consumeArmDrop(); }
    public boolean consumeHeadDrop() { return injury.consumeHeadDrop(); }
    public boolean hasLostArm() { return injury.hasLostArm(); }
    public boolean isDeathAnimFinished() { return injury.isDeathAnimFinished(); }
    public void markDeathAnimFinished() { injury.markDeathAnimFinished(); }
    public int getMaxHp() { return maxHp > 0 ? maxHp : Math.max(hp, 1); }

    @Override
    public void meltIce() { status.meltIce(effects); }

    public void meltIce(double amount) { status.meltIce(amount, effects); }

    @Override
    public void applySlowOrFreeze() { status.applySlowOrFreeze(effects, isBoss); }

    public Armor removeArmor() { return ZombieBehaviorAccess.removeArmor(this); }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public double getEatDps() { return eatDps; }

    public void resetEatClock(GameContext ctx) { eatClock.reset(ctx); }
    public int consumeEatDamage(GameContext ctx) { return eatClock.consume(ctx, eatDps); }

    public void setMirroredIceState(boolean iced, boolean initialFrozenBlock, double iceHp) {
        status.setMirroredIceState(iced, initialFrozenBlock, iceHp, effects);
    }

    public void setMirroredDeathState(boolean ashed, boolean ashFinished, boolean deathAnimFinished) {
        injury.setMirroredDeathState(ashed, ashFinished, deathAnimFinished);
    }

    public double getSpeed() { return speed; }
    public int getWavePointCost() { return wavePointCost; }
    public int getWeight() { return weight; }
    public Map<String, Behaviors> getBehaviors() { return behaviors; }
    public Map<String, Object> getExtraParams() { return extraParams; }
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isIced() { return status.isIced(); }
    public void applyButter() { status.applyButter(effects, isBoss); }
    public boolean isButtered() { return status.isButtered(); }
    public boolean isInitialFrozenBlock() { return status.isInitialFrozenBlock(); }
    public boolean isEating() { return isEating; }
    public long getSpawnTick() { return spawnTick; }
    public void setSpawnTick(long spawnTick) { this.spawnTick = spawnTick; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    public void setHp(int hp) {
        this.hp = hp;
        if (maxHp <= 0) {
            this.maxHp = hp;
        }
    }

    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setEatDps(double eatDps) { this.eatDps = eatDps; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setWavePointCost(int wpc) { this.wavePointCost = wpc; }
    public void setWeight(int weight) { this.weight = weight; }
    public void setExtraParams(Map<String, Object> p) { this.extraParams = p; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setEating(boolean eating) { this.isEating = eating; }
    public void setBehaviors(Map<String, Behaviors> behaviors) { this.behaviors = behaviors; }

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
        if (effects == null) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (Effects e : effects) {
            sb.append("\n").append(e.toString());
        }
        return sb.toString();
    }

    public boolean isBoss() { return isBoss; }
    public void setBoss(boolean boss) { this.isBoss = boss; }
    public void setBeingSucked(boolean beingSucked) { this.beingSucked = beingSucked; }
    public boolean isBeingSucked() { return beingSucked; }
    public void setRow(int r) { this.y = r; }

    public boolean searchEffect(Effects effect) {
        if (effect == null || this.effects == null) {
            return false;
        }
        return this.effects.contains(effect);
    }

    public List<Effects> getEffect() { return effects; }
    public double getIceHp() { return status.getIceHp(); }
}
