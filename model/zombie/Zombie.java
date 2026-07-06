package model.zombie;

import model.season.Season;
import model.zombie.behavior.Behaviors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Zombie {
    private String id;           // e.g. ZombieDefault
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

    private boolean isIced = false;
    private double iceHp = 0;

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

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setHp(int hp) { this.hp = hp; }
    public void setEatDps(double eatDps) { this.eatDps = eatDps; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setWavePointCost(int wavePointCost) { this.wavePointCost = wavePointCost; }
    public void setWeight(int weight) { this.weight = weight; }
    public void setEffects(List<Effects> effects) { this.effects = effects; }
    public void setExtraParams(Map<String, Object> extraParams) { this.extraParams = extraParams; }

    public boolean isDead() { return hp <= 0; }

    public void update() {} // runs each game loop tick

    public String zombieInfo() {
        return String.format("[%s] HP:%d EatDPS:%.0f Speed:%.3f WaveCost:%d",
                id, hp, eatDps, speed, wavePointCost);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void takeDamage(double damage){
        if (isIced) {
            iceHp -= damage;
            if (iceHp <= 0) {
                isIced = false;
                System.out.println("Zombie broke free from ice!");
            }
        } else {
            this.hp -= damage;
        }
    }

    public boolean isIced() {
        return isIced;
    }

    public void setIced(boolean iced) {
        isIced = iced;
    }

    public double getIceHp() {
        return iceHp;
    }

    public void setIceHp(double iceHp) {
        this.iceHp = iceHp;
    }
}
