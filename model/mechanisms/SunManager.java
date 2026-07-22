package model.mechanisms;

import model.TimeManager;
import model.user.UserManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SunManager {
    private TimeManager timeManager;
    private long nextDropTick;
    private List<Sun> activeSunDrops;
    private final int rows;
    private final int columns;

    public SunManager(TimeManager timeManager, int rows, int columns) {
        this.timeManager = timeManager;
        this.rows = rows;
        this.columns = columns;
        this.activeSunDrops = new ArrayList<>();
        manageNextDrop();
    }

    public void update(GameEngine engine) {
        boolean seasonAllowsSun = engine.getCtx().getSeason() == null ||
                engine.getCtx().getSeason().sunFallsFromSky();
        boolean levelManagerBlocksSun = engine.getCtx().getLevelManager() != null &&
                engine.getCtx().getLevelManager().disableSkySun();

        if (seasonAllowsSun && !levelManagerBlocksSun) {
            if (timeManager.getTotalTicks() >= nextDropTick) {
                generateRandom();
                manageNextDrop();
            }
        }

        for (Sun drop : activeSunDrops) {
            drop.update();
        }
    }

    private void manageNextDrop() {
        double t = timeManager.getTotalSeconds();
        double intervalSeconds = Math.max(6 + 0.05 * t, 12);
        int dl = UserManager.getInstance().getCurrentUser().getDifficultyLevel();
        double decreaseFactor = 3.0 / dl;
        intervalSeconds *= decreaseFactor;
        this.nextDropTick = timeManager.getTotalTicks() + (long) (intervalSeconds * 10);
    }

    public void generateRandom() {
        int x = (int) (Math.random() * columns);
        int y = (int) (Math.random() * rows);

        double roll = Math.random();
        SunType type;
        if (roll < 0.05) type = SunType.RADIOACTIVE;
        else if (roll < 0.20) type = SunType.SPECIAL;
        else type = SunType.NORMAL;

        Sun drop = new Sun(x, y, type);
        activeSunDrops.add(drop);
        view.ConsoleView.showMessage("New " + type + " sun is dropping at position (" + x + ", " + y + ")");
    }

    public List<Sun> getActiveSunDrops() {
        return activeSunDrops;
    }

    public int stealSun(Sun sun) {
        if (!sun.isOnGround()) return 0;
        int amount = (sun.getType() == SunType.SPECIAL) ? 100 : 25;
        activeSunDrops.remove(sun);
        return amount;
    }

    public boolean collectSun(int x, int y, GameEngine engine) {
        Iterator<Sun> iterator = activeSunDrops.iterator();
        while (iterator.hasNext()) {
            Sun drop = iterator.next();
            if (drop.getX() == x && drop.getY() == y) {
                if (!drop.isOnGround() && drop.getType() == SunType.RADIOACTIVE) {
                    explodeRadioactive(x, y, engine);
                    iterator.remove();
                    return true;
                }

                if (!drop.isOnGround()) {
                    return false;
                }

                int amount = (drop.getType() == SunType.SPECIAL) ? 100 : 25;
                engine.getCtx().addSun(amount);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private void explodeRadioactive(int x, int y, GameEngine engine) {
        for (var z : engine.getCtx().getAliveZombies()) {
            if (Math.abs(z.getX() - x) <= 2 && Math.abs(z.getY() - y) <= 2) {
                z.takeDamage(150);
            }
        }
        for (var p : engine.getCtx().getAlivePlants()) {
            if (Math.abs(p.getCol() - x) <= 1 && Math.abs(p.getRow() - y) <= 1) {
                p.takeDamage(80);
            }
        }
    }
}