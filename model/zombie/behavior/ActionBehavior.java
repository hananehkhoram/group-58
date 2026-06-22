package model.zombie.behavior;

import model.zombie.Zombie;
import java.util.List;

/**
 * Handles zombies whose special behavior maps to an "Actions" entry in zombies.json.
 *
 * Zombies:
 *   ZombieIceAgeTroglobite → PUSH_GRID_ITEM
 *   ZombieBeachOctopus     → OCTOPUS_PROJECTILE
 *   ZombieWizard           → DARK_WIZARD_ZAP
 *   ZombieArcade           → ARCADE_PUSH
 *   ZombieDarkKing         → KNIGHT_KNIGHTING
 */
public class ActionBehavior implements Behaviors {

    public enum ActionType {
        PUSH_GRID_ITEM,
        OCTOPUS_PROJECTILE,
        DARK_WIZARD_ZAP,
        ARCADE_PUSH,
        KNIGHT_KNIGHTING
    }

    private ActionType actionType;
    private ActionParams params;

    public ActionBehavior(ActionType actionType, ActionParams params) {
        this.actionType = actionType;
        this.params = params;
    }

    @Override
    public void onTick(Zombie zombie) {
        switch (actionType) {
            case PUSH_GRID_ITEM:     pushIceBlock(zombie);       break;
            case OCTOPUS_PROJECTILE: fireInkProjectile(zombie);  break;
            case DARK_WIZARD_ZAP:    zapPlant(zombie);           break;
            case ARCADE_PUSH:        pushWithCabinet(zombie);    break;
            case KNIGHT_KNIGHTING:   knightNearbyZombies(zombie);break;
        }
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    private void pushIceBlock(Zombie zombie) {
        // Troglobite: pushes ice blocks (numberOfIceblocks=3), chillInsteadOfFreeze=true
    }
    private void fireInkProjectile(Zombie zombie) {
        // Octopus: fires ink blob projectile
    }
    private void zapPlant(Zombie zombie) {
        // Wizard: transforms plant into sheep
    }
    private void pushWithCabinet(Zombie zombie) {
        // Arcade: pushes forward with cabinet; jamStyle=jam_8bit (audio/visual only)
    }
    private void knightNearbyZombies(Zombie zombie) {
        // King: knights dark zombies in range (areaX=4, areaY=3); delayBetweenKnightings=2.5s
    }

    public ActionType getActionType() { return actionType; }
    public ActionParams getParams() { return params; }

    public static class ActionParams {
        // Troglobite
        public int numberOfIceblocks;
        public boolean chillInsteadOfFreeze;
        // King
        public float delayBetweenKnightings;
        public int knightingAreaX;
        public int knightingAreaY;
        public List<String> validKnightTargets;
        public List<String> plantablePlants;
        // Arcade
        public String jamStyle;

        public ActionParams() {}
    }
}