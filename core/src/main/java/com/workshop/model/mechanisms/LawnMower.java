package com.workshop.model.mechanisms;

import com.workshop.model.level.Level;
import com.workshop.model.zombie.Effects;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

import java.util.ArrayList;

public class LawnMower {
    private final double TRIGGER_X = 0.1;
    private final int row;
    private boolean isActivated = false;
    private boolean isAvailable = true;
    private double x = 0;
    private double velocityX = 40;
    private final ArrayList<Zombie> killedZombies = new ArrayList<>();

    public LawnMower(int row) {
        this.row = row;
    }

    public void advance(double deltaTicks) {
        if (!isActivated || !isAvailable) return;
        x += deltaTicks / 10 * velocityX;
        if (x > Level.COLS) {
            isAvailable = false;
            Console.showMessage("The lawn mower in the row " + row
                    + " is triggered and killed these zombies: ");
            for (Zombie za : killedZombies) {
                Console.showMessage(za.getName());
            }
        }
    }

    public void activate() {
        isActivated = true;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public int getRow() {
        return row;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public boolean checkTrigger(Zombie z) {
        return z.getY() == row && z.getX() <= x;
    }

    public void trigger(Zombie z) {
        if (!isActivated || !isAvailable) return;
        if (z.getHp() <= 0 || z.isBoss() || z.getEffect().contains(Effects.HYPNOTIZED)) return;
        if (!checkTrigger(z)) return;

        z.setHp(0);
        if (!killedZombies.contains(z)) {
            killedZombies.add(z);
        }
    }

    public static LawnMower[] buildLawnMowers() {
        LawnMower[] mowers = new LawnMower[Level.ROWS];
        for (int r = 0; r < Level.ROWS; r++) {
            mowers[r] = new LawnMower(r);
        }
        return mowers;
    }

    public double getX() {
        return x;
    }
}
