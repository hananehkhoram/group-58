package com.workshop.model.zombie;

/**
 * Sandstorm dash entry state for a zombie. Mutates position via {@link Zombie#setX(double)}.
 */
final class ZombieSandstorm {

    private static final double SANDSTORM_DASH_SPEED = 3.6;
    private static final float SANDSTORM_LAND_SECONDS = 0.75f;

    private double sandstormTargetX;
    private float sandstormDelay;
    private boolean sandstormLanded;
    private float sandstormLandTime;
    private boolean enteredViaSandstorm;

    void start(double targetX, float delaySeconds) {
        enteredViaSandstorm = true;
        sandstormTargetX = targetX;
        sandstormDelay = Math.max(0f, delaySeconds);
        sandstormLanded = false;
        sandstormLandTime = 0f;
    }

    boolean isLanded() {
        return sandstormLanded;
    }

    float getLandTime() {
        return sandstormLandTime;
    }

    boolean isActive() {
        return enteredViaSandstorm;
    }

    boolean update(Zombie zombie, double deltaTime) {
        if (!enteredViaSandstorm) {
            return false;
        }

        zombie.setEating(false);

        if (sandstormDelay > 0f) {
            sandstormDelay -= (float) deltaTime;
            return true;
        }

        if (!sandstormLanded) {
            zombie.setX(zombie.getX() - SANDSTORM_DASH_SPEED * deltaTime);
            if (zombie.getX() <= sandstormTargetX) {
                zombie.setX(sandstormTargetX);
                sandstormLanded = true;
                sandstormLandTime = 0f;
            }
            return true;
        }

        sandstormLandTime += (float) deltaTime;
        if (sandstormLandTime >= SANDSTORM_LAND_SECONDS) {
            enteredViaSandstorm = false;
        }
        return false;
    }
}
