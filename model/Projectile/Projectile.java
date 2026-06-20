package model.Projectile;

import model.zombie.Zombie;

public abstract class Projectile {
    protected int damage;
    protected double x, y;
    protected int row;
    protected double speed;
    protected boolean isActive;

    public Projectile(int damage, double x, double y, int row, double speed, boolean isActive) {
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.row = row;
        this.speed = speed;
        this.isActive = isActive;
    }

    public double getX() {
        return x;
    }

    public int getRow() {
        return row;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getDamage() {
        return damage;
    }

    public double getSpeed() {
        return speed;
    }

    public double getY() {
        return y;
    }

    public abstract void onHit(Zombie zombie);

    public void update(double time){}
}
