package model.plants;

import model.GameContext;
import model.plants.plantAbilities.BaseAbility;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.plants.upgradeEffect.BehaviorEffect;
import model.plants.upgradeEffect.BehaviorKey;
import model.plants.upgradeEffect.StatEffect;
import model.projectile.Damageable;

import java.util.*;

public class Plant implements Damageable {
    // from csv file
    private int id;
    private String name;
    private PlantFamily family;
    private EnumSet<Tag> tags;
    private int sunCost;
    private int baseHp;
    // Kept as String: source data includes multi-hit ("20x2"), stacked-level
    // ("20/40/60/80/100") and special ("Insta-kill") values, not plain ints.
    private String damage;
    private BaseAbility baseAbility;
    private Map<String, String> abilityParams;
    private PlantFoodMode plantFoodMode;

    private boolean hasLilyPadUnderneath;


    // Index 0 = upgrades unlocked at level 2, index 1 = level 3, index 2 = level 4.
    private List<StatEffect>[] statUpgrades;
    private List<BehaviorEffect>[] behaviorUpgrades;

    // Nullable: some plants (mines, wall-nuts, one-shot consumables) have no
    // periodic action, so the CSV cell is blank for them.
    private Double actionInterval;
    private double rechargeTime;
    private TargetingMode targetingMode;
    private int lastActionSecond = 0;   //timeManaging

    private int level;

    private int hp;
    private int row, col;
    private boolean plantFoodActive = false;

    private int freezeLevel = 0;
    private double iceHp = 0;
    private double octHp = 0;
    private boolean isIced = false;
    private boolean isOctopused =false;

    public Plant() {
    }

    @Override
    public void takeDamage(int amount) {
        if (isOctopused) {
            damageOctopuse(amount);
            return;
        }

        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            isIced = false;
            iceHp = 0;
            freezeLevel = 0;
        }
    }

    public void heal(int amount) {
        this.hp += amount;
    }

    public void activatePlantFood(GameContext ctx) {
        if (baseAbility != null) {
            baseAbility.activatePlantFood(this, ctx, plantFoodMode);
        }
    }

    public void upgrade() {
    }

    // --- Damageable ---

    @Override
    public double getX() {
        return col;
    }

    @Override
    public boolean isDead() {
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

    // --- Getters / setters used by PlantLoader ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlantFamily getFamily() {
        return family;
    }

    public void setFamily(PlantFamily family) {
        this.family = family;
    }

    public EnumSet<Tag> getTags() {
        return tags;
    }

    public void setTags(EnumSet<Tag> tags) {
        this.tags = tags;
    }

    public int getSunCost() {
        return sunCost;
    }

    public void setSunCost(int sunCost) {
        this.sunCost = sunCost;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public BaseAbility getBaseAbility() {
        return baseAbility;
    }

    public void setBaseAbility(BaseAbility baseAbility) {
        this.baseAbility = baseAbility;
    }

    public Map<String, String> getAbilityParams() {
        return abilityParams;
    }

    public void setAbilityParams(Map<String, String> abilityParams) {
        this.abilityParams = abilityParams;
    }

    public PlantFoodMode getPlantFoodMode() {
        return plantFoodMode;
    }

    public void setPlantFoodMode(PlantFoodMode plantFoodMode) {
        this.plantFoodMode = plantFoodMode;
    }

    public List<StatEffect>[] getStatUpgrades() {
        return statUpgrades;
    }

    public void setStatUpgrades(List<StatEffect>[] statUpgrades) {
        this.statUpgrades = statUpgrades;
    }

    public List<BehaviorEffect>[] getBehaviorUpgrades() {
        return behaviorUpgrades;
    }

    public void setBehaviorUpgrades(List<BehaviorEffect>[] behaviorUpgrades) {
        this.behaviorUpgrades = behaviorUpgrades;
    }

    public Double getActionInterval() {
        return actionInterval;
    }

    public void setActionInterval(Double actionInterval) {
        this.actionInterval = actionInterval;
    }

    public double getRechargeTime() {
        return rechargeTime;
    }

    public void setRechargeTime(double rechargeTime) {
        this.rechargeTime = rechargeTime;
    }

    public TargetingMode getTargetingMode() {
        return targetingMode;
    }

    public void setTargetingMode(TargetingMode targetingMode) {
        this.targetingMode = targetingMode;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }


    public int getLastActionSecond() {
        return lastActionSecond;
    }

    public void setLastActionSecond(int lastActionSecond) {
        this.lastActionSecond = lastActionSecond;
    }

    public boolean hasTheTag(Tag tag){
        for (Tag t : tags){
            if (t.equals(tag)) return true;
        }
        return false;
    }
    public void increaseFreezeLevel(){
        freezeLevel += 1;
        if (freezeLevel >= 3){
            isIced = true;
            iceHp = 600;
        }
    }

    public int getFreezeLevel() {
        return freezeLevel;
    }

    public boolean isHasLilyPadUnderneath() {
        return hasLilyPadUnderneath;
    }

    public int getHp() {
        return hp;
    }
    public boolean isPlantFoodActive() {
        return plantFoodActive;
    }
    public void setPlantFoodActive(boolean plantFoodActive) {
        this.plantFoodActive = plantFoodActive;
    }
    private Set<BehaviorKey> activeBehaviors = new HashSet<>();
    public void addBehavior(BehaviorKey key) {
    this.activeBehaviors.add(key);
    }
    public void damageIce(double amount) {
        iceHp -= amount;
        if (iceHp <= 0) {
            iceHp = 0;
            isIced = false;
            freezeLevel = 0;
        }
    }

    public void damageOctopuse(double amount) {
        octHp -= amount;
        if (octHp <= 0) {
            octHp = 0;
            isOctopused = false;
        }
    }

    public boolean isIced() {
        return isIced;
    }

    public boolean isOctopused() {
        return isOctopused;
    }

    public void setOctopused(boolean octopused) {
        this.isOctopused = octopused;
        if (octopused) {
            this.octHp = 600; // مقدار HP اولیه‌ برای اختاپوس
        } else {
            this.octHp = 0;
        }
    }
}