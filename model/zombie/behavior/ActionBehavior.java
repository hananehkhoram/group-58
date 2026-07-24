package model.zombie.behavior;

import model.GameContext;
import model.plants.Plant;
import model.zombie.Zombie;

import java.util.HashSet;
import java.util.Set;

public class ActionBehavior implements Behaviors {

    public enum ActionType {
        DARK_WIZARD_ZAP,
        KNIGHT_KNIGHTING
    }

    private final ActionType actionType;
    private final ActionParams params;

    private long lastActionTick = 0;

    private final Set<Plant> catifiedPlants = new HashSet<>();

    public ActionBehavior(ActionType actionType, ActionParams params) {
        this.actionType = actionType;
        this.params = params;
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        switch (actionType) {
            case DARK_WIZARD_ZAP:
                catifiedPlants.removeIf(Plant::isDead);
                zapPlant(zombie, ctx);
                break;
            case KNIGHT_KNIGHTING:
                knightNearbyZombies(zombie, ctx);
                break;
        }
    }

    private void knightNearbyZombies(Zombie king, GameContext ctx) {
        king.setSpeed(0);

        long currentTick = ctx.getTimeManager().getTotalTicks();
        float delayInSeconds = (params != null && params.delayBetweenKnightings > 0)
                ? params.delayBetweenKnightings : 2.5f;
        long delayInTicks = (long) (delayInSeconds * 10);

        if (currentTick - lastActionTick < delayInTicks) {
            return;
        }

        int areaX = (params != null && params.knightingAreaX > 0) ? params.knightingAreaX : 4;
        int areaY = (params != null && params.knightingAreaY > 0) ? params.knightingAreaY : 3;

        for (Zombie z : ctx.getAliveZombies()) {
            if (z == king || z.isDead()) continue;

            boolean inXRange = Math.abs(z.getX() - king.getX()) <= areaX;
            boolean inYRange = Math.abs(z.getRow() - king.getRow()) <= areaY;

            if (inXRange && inYRange) {
                if (z.getArmor() == null && z.getSecondaryArmor() == null) {
                    Armor crown = new Armor(ArmorType.SHOULDER_CROWN, ArmorType.SHOULDER_CROWN.baseHealth, true, z.getX(), z.getY());
                    z.getBehaviors().put("armor", crown);

                    Armor shoulder = new Armor(ArmorType.SHOULDER_ARMOR, ArmorType.SHOULDER_ARMOR.baseHealth, true, z.getX(), z.getY());
                    z.getBehaviors().put("armor2", shoulder);

                    lastActionTick = currentTick;
                    break;
                }
            }
        }
    }


    private void zapPlant(Zombie wizard, GameContext ctx) {
        long currentTick = ctx.getTimeManager().getTotalTicks();
        float delayInSeconds = (params != null && params.zapDelay > 0) ? params.zapDelay : 3.0f;
        long delayInTicks = (long) (delayInSeconds * 10);

        if (currentTick - lastActionTick < delayInTicks) {
            return;
        }

        Plant target = ctx.findNearestPlantInRow(wizard);
        if (target != null && !target.isDead() && !target.isCatified()) {
            target.setCatified(true, wizard);
            catifiedPlants.add(target);
            lastActionTick = currentTick;
        }
    }


    @Override
    public void onDeath(Zombie zombie, GameContext ctx) {
        if (actionType == ActionType.DARK_WIZARD_ZAP) {
            for (Plant p : catifiedPlants) {
                if (!p.isDead()) {
                    p.setCatified(false, null);
                }
            }
            catifiedPlants.clear();
        }
    }

    public static class ActionParams {
        public float delayBetweenKnightings = 2.5f;
        public int knightingAreaX = 4;
        public int knightingAreaY = 3;

        public float zapDelay = 3.0f;

        public ActionParams() {}
    }
}