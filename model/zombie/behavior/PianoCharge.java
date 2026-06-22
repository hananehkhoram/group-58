package model.zombie.behavior;

import model.zombie.Zombie;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class PianoCharge implements Behaviors {

    private boolean rolling;  // true until piano is destroyed by a break-plant
    private double rollSpeed;         // 0.4
    private double normalSpeed;       // 0.12
    private double rollingEatDps;     // 4000
    private int streetWidth;          // 3
    private int streetHeight;         // 2
    private Set<String> breakPlants;  // plants that destroy the piano

    public PianoCharge(double rollSpeed, double normalSpeed, double rollingEatDps,
                       int streetWidth, int streetHeight, List<String> breakPlants) {
        this.rolling = true;
        this.rollSpeed = rollSpeed;
        this.normalSpeed = normalSpeed;
        this.rollingEatDps = rollingEatDps;
        this.streetWidth = streetWidth;
        this.streetHeight = streetHeight;
        this.breakPlants = new HashSet<>(breakPlants);
    }

    @Override
    public void onTick(Zombie zombie) {
        if (rolling) {
        }
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public void breakPiano(Zombie zombie) {
        rolling = false;
    }

    public boolean willBreakOn(String plantId) {
        return breakPlants.contains(plantId);
    }

    public boolean isRolling() { return rolling; }
    public double getCurrentSpeed() { return rolling ? rollSpeed : normalSpeed; }
    public double getRollingEatDps() { return rollingEatDps; }
    public int getStreetWidth() { return streetWidth; }
    public int getStreetHeight() { return streetHeight; }
}