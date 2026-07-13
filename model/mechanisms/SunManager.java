package model.mechanisms;

import model.TimeManager;

import java.util.List;

public class SunManager{
    private TimeManager timeManager;
    private long nextDropTick;
    private List<Sun> activeSunDrops;

    public SunManager(TimeManager timeManager) {
        this.timeManager = timeManager;
        manageNextDrop();
    }
    public void update(){
        if (timeManager.getTotalTicks() >= nextDropTick) {
            generateRandom();
            manageNextDrop();
        }

        for (Sun drop : activeSunDrops) {
            drop.update();
        }
    }
    private void manageNextDrop(){
        double t = timeManager.getTotalSeconds();
        double intervalSeconds = Math.max(6 + 0.05 * t, 12);
        this.nextDropTick = timeManager.getTotalTicks() + (long) (intervalSeconds * 10);
    }
    public void generateRandom(){
        int x = (int) (Math.random() * BOARD_COLUMNS);
        int y = (int) (Math.random() * BOARD_ROWS);

        double roll = Math.random();
        SunType type;
        if (roll < 0.05) type = SunType.RADIOACTIVE;
        else if (roll < 0.20) type = SunType.SPECIAL;   // ۵٪ + ۱۵٪
        else type = SunType.NORMAL;                      // ۸۰٪

        Sun drop = new Sun(x, y, type);
        activeSunDrops.add(drop);
        view.ConsoleView.showMessage("New " + type + " sun is dropping at position (" + x + ", " + y + ")");
    }
    public boolean collectSun(int x, int y, GameEngine engine) {
        for (Sun drop : activeSunDrops) {
            if (drop.getX() == x && drop.getY() == y) {

                if (!drop.isOnGround() && drop.getType() == SunType.RADIOACTIVE) {
                    explodeRadioactive(x, y, engine);
                    activeSunDrops.remove(drop);
                    return true;
                }

                if (!drop.isOnGround()) {
                    return false;
                }

                int amount = (drop.getType() == SunType.SPECIAL) ? 100 : 25;
                engine.getCtx().addSun(amount);
                activeSunDrops.remove(drop);
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
