package model.mechanisms;

import model.level.Level;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.ArrayList;

public class LawnMower {
    private final double TRIGGER_X = 0.1;
    private final int row;
    private boolean isActivated = false;
    private boolean isAvailable = true;
    private double x = 0;
    private double velocityX = 40;
    private final ArrayList<Zombie> killedZombies = new ArrayList<>();
    private boolean didKilled = false;

    public LawnMower(int row) {
        this.row = row;
    }

    public boolean checkTrigger(Zombie z) {
        return z.getY() == row && Math.abs(z.getX() - x) < TRIGGER_X;
    }

    public ArrayList<Zombie> getKilledZombies() {
        return killedZombies;
    }


    public void advance(double deltaTicks) {
        if (!isActivated || !isAvailable) return;
        x += deltaTicks / 10 * velocityX;
        if (x > Level.COLS) {
            isAvailable = false;
            ConsoleView.showMessage("The lawn mower in the row " + row
                    + " is triggered and killed these zombies: ");
            for (Zombie za : killedZombies) {
                ConsoleView.showMessage(za.getName());
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

    public void trigger(Zombie z) {
        if (!isActivated || !isAvailable) return;
        if (z.getHp() <= 0) return;
        if (checkTrigger(z) && !z.isBoss()) {
            z.setHp(0);
            killedZombies.add(z);
            setDidKilled(true);
        }
    }

    public double getX() {
        return x;
    }

    public boolean isDidKilled() {
        return didKilled;
    }

    public void setDidKilled(boolean didKilled) {
        this.didKilled = didKilled;
    }

    public static LawnMower[] buildLawnMowers() {
        LawnMower[] mowers = new LawnMower[Level.ROWS];
        for (int r = 0; r < Level.ROWS; r++) {
            mowers[r] = new LawnMower(r);
        }
        return mowers;
    }
}