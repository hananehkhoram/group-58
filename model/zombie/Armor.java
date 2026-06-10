package model.zombie;

public class Armor {
    private String type;
    private int hp;

    public void reduceHealth(int damage) { this.hp -= damage; }
    public boolean isAlive() { return hp > 0; }
}

