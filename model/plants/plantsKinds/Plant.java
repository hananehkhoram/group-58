package model.plants.plantsKinds;

import model.GameContext;
import model.plants.PlantFamily;
import model.plants.Tag;
import model.plants.TargetingMode;
import model.plants.plantAbilities.BaseAbility;
import model.plants.plantFoodEffect.PlantFoodEffect;
import model.plants.upgradeEffect.BehaviorEffect;
import model.plants.upgradeEffect.StatEffect;

import java.util.EnumSet;
import java.util.List;

public abstract class Plant {
    // from csv file
    protected List<StatEffect>[] statUpgrades;
    protected List<BehaviorEffect>[] behaviorUpgrades;
    protected int id;
    protected String name;
    protected PlantFamily family;
    protected EnumSet<Tag> tags;
    protected int sunCost;
    protected int baseHp;
    protected int damage;
    protected BaseAbility baseAbility;
    protected PlantFoodEffect plantFoodEffect;
    protected int level;
    protected double actionInterval;
    protected double rechargeTime;
    protected TargetingMode targetingMode;

    protected int hp;
    protected int row, col;
    protected boolean plantFoodActive;
    protected double attackCooldown;

    protected int seedPacketsCollected;
    protected int seedPacketsRequired;
    protected int coinsRequired;

    public abstract void update(GameContext ctx);

    public void takeDamage(int amount) {
    }

    public void heal(int amount) {
    }

    public void activatePlantFood(GameContext ctx) {
    }

    public void upgrade() {
    }


}
