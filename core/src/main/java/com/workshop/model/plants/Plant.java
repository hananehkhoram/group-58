package com.workshop.model.plants;

import com.workshop.model.GameContext;
import com.workshop.model.plants.plantAbilities.BaseAbility;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;
import com.workshop.model.plants.upgradeEffect.BehaviorEffect;
import com.workshop.model.plants.upgradeEffect.BehaviorKey;
import com.workshop.model.plants.upgradeEffect.StatEffect;
import com.workshop.model.plants.plantAbilities.Lobber;
import com.workshop.model.projectile.Damageable;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.zombie.Zombie;

import java.util.*;

public class Plant implements Damageable {
    // from csv file
    private int id;
    private String name;
    private PlantFamily family;
    private EnumSet<Tag> tags;
    private int sunCost;
    private int baseHp;
    private String damage;
    private BaseAbility baseAbility;
    private Map<String, String> abilityParams;
    private PlantFoodMode plantFoodMode;

    private boolean hasLilyPadUnderneath;
    private Plant coveredPlant;
    private Plant coverPlant;

    private List<StatEffect>[] statUpgrades;
    private List<BehaviorEffect>[] behaviorUpgrades;

    private Double actionInterval;
    private double rechargeTime;
    private int lastActionSecond = 0;   // timeManaging
    private int plantTimeSecond = 0;    // زمان کاشت گیاه بر حسب ثانیه
    private boolean actionComplete;
    private final List<Projectile> pendingShots = new ArrayList<>();
    private long pendingShotArmedTick = -1;

    private int level;

    private int hp;
    private int row, col;
    private boolean plantFoodActive = false;
    private float plantFoodGlowRemaining;
    private Double visualX;
    private Double visualY;
    private boolean beingPulled;

    private int freezeLevel = 0;
    private double iceHp = 0;
    private double octHp = 0;
    private boolean isIced = false;
    private boolean isOctopused = false;

    // طلسمِ جادوگر: گیاه تا زمان مرگِ همون جادوگری که طلسمش کرده به گربه تبدیل می‌مونه
    private boolean isCatified = false;
    private Zombie catifiedBy;

    public Plant() {
    }

    public double getIceHp() {
        return iceHp;
    }
    public double getOctopusHp() {
        return octHp;
    }

    @Override
    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            isIced = false;
            iceHp = 0;
            freezeLevel = 0;
            isOctopused = false;
            octHp = 0;
            isCatified = false;
            catifiedBy = null;
        }
    }

    public void heal(int amount) {
        this.hp += amount;
    }

    public void activatePlantFood(GameContext ctx) {
        if (isOctopused || isIced || isCatified) return;

        startPlantFoodGlow();
        if (baseAbility != null) {
            baseAbility.activatePlantFood(this, ctx, plantFoodMode);
        }
    }

    public void startPlantFoodGlow() {
        startPlantFoodGlow(2.8f);
    }

    public void startPlantFoodGlow(float seconds) {
        plantFoodGlowRemaining = Math.max(0.4f, seconds);
    }

    public void tickPlantFoodGlow(float delta) {
        if (plantFoodGlowRemaining > 0f) {
            plantFoodGlowRemaining = Math.max(0f, plantFoodGlowRemaining - delta);
        }
    }

    public boolean isShowingPlantFoodGlow() {
        return plantFoodGlowRemaining > 0f;
    }

    // --- Damageable ---

    @Override
    public double getX() {
        return col;
    }

    @Override
    public boolean isDead() {
        if (baseHp <= 0) {
            return false;
        }
        return hp <= 0;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void meltIce() {
        if (isIced) {
            damageIce(iceHp);
        }
    }

    @Override
    public void applySlowOrFreeze() {
        increaseFreezeLevel();
    }

    // --- Getters / setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public boolean isPeaFamily() {
        if (name == null) {
            return false;
        }
        String compact = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return compact.contains("PEA")
            || compact.contains("REPEATER")
            || compact.contains("GATLING");
    }
    public void setName(String name) { this.name = name; }

    public PlantFamily getFamily() { return family; }
    public void setFamily(PlantFamily family) { this.family = family; }

    public EnumSet<Tag> getTags() { return tags; }
    public void setTags(EnumSet<Tag> tags) { this.tags = tags; }

    public int getSunCost() { return sunCost; }
    public void setSunCost(int sunCost) { this.sunCost = sunCost; }

    public int getBaseHp() { return baseHp; }
    public void setBaseHp(int baseHp) { this.baseHp = baseHp; }

    public String getDamage() { return damage; }
    public void setDamage(String damage) { this.damage = damage; }

    public BaseAbility getBaseAbility() { return baseAbility; }
    public void setBaseAbility(BaseAbility baseAbility) { this.baseAbility = baseAbility; }

    public Map<String, String> getAbilityParams() { return abilityParams; }
    public void setAbilityParams(Map<String, String> abilityParams)
    { this.abilityParams = abilityParams; }

    public PlantFoodMode getPlantFoodMode() { return plantFoodMode; }
    public void setPlantFoodMode(PlantFoodMode plantFoodMode)
    { this.plantFoodMode = plantFoodMode; }

    public List<StatEffect>[] getStatUpgrades() { return statUpgrades; }
    public void setStatUpgrades(List<StatEffect>[] statUpgrades)
    { this.statUpgrades = statUpgrades; }

    public List<BehaviorEffect>[] getBehaviorUpgrades() { return behaviorUpgrades; }
    public void setBehaviorUpgrades(List<BehaviorEffect>[] behaviorUpgrades)
    { this.behaviorUpgrades = behaviorUpgrades; }

    public Double getActionInterval() { return actionInterval; }
    public void setActionInterval(Double actionInterval)
    { this.actionInterval = actionInterval; }

    public double getRechargeTime() { return rechargeTime; }
    public void setRechargeTime(double rechargeTime)
    { this.rechargeTime = rechargeTime; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public void setVisualPosition(double x, double y) {
        this.visualX = x;
        this.visualY = y;
        this.beingPulled = true;
    }

    public Double getVisualX() {
        return visualX;
    }

    public Double getVisualY() {
        return visualY;
    }

    public boolean isBeingPulled() {
        return beingPulled;
    }

    public int getLastActionSecond() { return lastActionSecond; }
    public void armPendingShots(List<Projectile> shots, long armedTick) {
        if (shots == null || shots.isEmpty()) {
            return;
        }
        pendingShots.addAll(shots);
        if (pendingShotArmedTick < 0) {
            pendingShotArmedTick = armedTick;
        }
    }

    public boolean hasPendingShots() {
        return !pendingShots.isEmpty();
    }

    public int pendingShotCount() {
        return pendingShots.size();
    }

    public boolean releaseNextPendingShot(GameContext ctx) {
        if (pendingShots.isEmpty() || ctx == null) {
            return false;
        }
        ctx.setNewProjectiles(pendingShots.remove(0));
        ctx.flushPendingProjectiles();
        if (pendingShots.isEmpty()) {
            pendingShotArmedTick = -1;
        }
        return true;
    }

    public void releaseAllPendingShots(GameContext ctx) {
        while (releaseNextPendingShot(ctx)) {
        }
    }

    public void discardPendingShots() {
        pendingShots.clear();
        pendingShotArmedTick = -1;
    }

    public long getPendingShotArmedTick() {
        return pendingShotArmedTick;
    }

    public float attackReleaseRatio() {
        if (isPeaFamily()) {
            return 0.40f;
        }
        if (baseAbility instanceof Lobber) {
            return 0.52f;
        }
        return 0.45f;
    }

    public void setLastActionSecond(int lastActionSecond)
    { this.lastActionSecond = lastActionSecond; }

    public int getPlantTimeSecond() { return plantTimeSecond; }
    public void setPlantTimeSecond(int plantTimeSecond) { this.plantTimeSecond = plantTimeSecond; }

    public void markActionComplete() {
        actionComplete = true;
    }

    public boolean isActionComplete() {
        return actionComplete;
    }

    public boolean isGraveDestroyer() {
        return abilityParams != null
            && "GRAVE_DESTROY".equals(abilityParams.get("explosiveType"));
    }

    public boolean hasTheTag(Tag tag) {
        return tags != null && tags.contains(tag);
    }

    public void increaseFreezeLevel() {
        freezeLevel += 1;
        if (freezeLevel >= 3) {
            isIced = true;
            iceHp = 600;
        }
    }

    public int getFreezeLevel() { return freezeLevel; }
    public boolean isHasLilyPadUnderneath() { return hasLilyPadUnderneath; }
    public void setHasLilyPadUnderneath(boolean hasLilyPadUnderneath) {
        this.hasLilyPadUnderneath = hasLilyPadUnderneath;
    }

    public boolean isStackableCover() {
        return abilityParams != null && "STACKABLE_COVER".equals(abilityParams.get("wallNutType"));
    }

    public Plant getCoveredPlant() { return coveredPlant; }
    public void setCoveredPlant(Plant coveredPlant) { this.coveredPlant = coveredPlant; }

    public Plant getCoverPlant() { return coverPlant; }
    public void setCoverPlant(Plant coverPlant) { this.coverPlant = coverPlant; }

    public boolean isLilyPad() {
        if (name != null && name.equalsIgnoreCase("Lily Pad")) {
            return true;
        }
        return abilityParams != null
            && "WATER_PLATFORM".equals(abilityParams.get("modifierType"));
    }
    public int getHp() { return hp; }

    public boolean isPlantFoodActive() { return plantFoodActive; }
    public void setPlantFoodActive(boolean plantFoodActive)
    { this.plantFoodActive = plantFoodActive; }

    private Set<BehaviorKey> activeBehaviors = new HashSet<>();
    public void addBehavior(BehaviorKey key) { this.activeBehaviors.add(key); }

    public void damageIce(double amount) {
        iceHp -= amount;
        if (iceHp <= 0) {
            iceHp = 0;
            isIced = false;
            freezeLevel = 0;
        }
    }

    public void damageOctopuse(double amount) {
        if (!isOctopused) return;

        octHp -= amount;
        if (octHp <= 0) {
            octHp = 0;
            isOctopused = false;
        }
    }

    public boolean isIced() { return isIced; }
    public boolean isOctopused() { return isOctopused; }

    public void setOctopused(boolean octopused) {
        this.isOctopused = octopused;
        if (octopused) {
            this.octHp = 400;
        } else {
            this.octHp = 0;
        }
    }

    public boolean isCatified() { return isCatified; }

    public void setCatified(boolean catified, Zombie caster) {
        this.isCatified = catified;
        this.catifiedBy = catified ? caster : null;
    }
}
