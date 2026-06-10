package model.plants;

import model.GameContext;
import model.plants.plantAbilities.BaseAbility;
import model.plants.plantFoodEffect.PlantFoodEffect;
import model.plants.upgradeEffect.BehaviorEffect;
import model.plants.upgradeEffect.StatEffect;

import java.util.EnumSet;
import java.util.List;

public class Plant {
    // from csv file
    private List<StatEffect>[] statUpgrades;
    private List<BehaviorEffect>[] behaviorUpgrades;
    private int id;
    private String name;
    private PlantFamily family;
    private EnumSet<Tag> tags;
    private int sunCost;
    private int baseHp;
    private int damage;
    private BaseAbility baseAbility;
    private PlantFoodEffect plantFoodEffect;
    private int level;
    private double actionInterval;
    private double rechargeTime;
    private TargetingMode targetingMode;

    private int hp;
    private int row, col;
    private boolean plantFoodActive;
    private double attackCooldown;
    private boolean isIced;

    public void update(GameContext ctx){};

    public void takeDamage(int amount) {
    }

    public void heal(int amount) {
    }

    public void activatePlantFood(GameContext ctx) {
    }

    public void upgrade() {
    }


}
