package model.plants;

import model.GameContext;
import model.plants.plantAbilities.BaseAbility;
import model.plants.plantFoodEffect.PlantFoodMode;
import model.plants.upgradeEffect.BehaviorEffect;
import model.plants.upgradeEffect.StatEffect;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class Plant {
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

    private boolean plantFood;

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
    private boolean plantFoodActive;
    private double attackCooldown;
    private boolean isIced;

    public Plant() {
    }

    public void update(GameContext ctx) {
    }

    public void takeDamage(int amount) {
    }

    public void heal(int amount) {
    }

    public void activatePlantFood(GameContext ctx) {
        if (baseAbility != null) {
            baseAbility.activatePlantFood(this, ctx, plantFoodMode);
        }
    }

    public void upgrade() {
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

    public boolean isPlantFood() {
        return plantFood;
    }

    public void setPlantFood(boolean plantFood) {
        this.plantFood = plantFood;
    }

    public int getLastActionSecond() {
        return lastActionSecond;
    }

    public void setLastActionSecond(int lastActionSecond) {
        this.lastActionSecond = lastActionSecond;
    }
}
