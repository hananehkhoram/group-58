package model.zombie.behavior;

import model.zombie.Zombie;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Zombies that catch or deflect plant projectiles.
 *
 * Used by:
 *   - ZombieDarkJuggler  — catches and re-launches juggleable projectiles
 *   - ZombieLostCityJane — bounces specific lobbed projectiles back
 */
public class ProjectileDeflector implements Behaviors {

    public enum DeflectMode {
        JUGGLE,  // Juggler: catches projectiles and throws them back at plants
        BOUNCE   // Jane: deflects lobbed projectiles with parasol
    }

    private DeflectMode mode;
    private Set<String> juggleableProjectiles;
    private Set<String> bounceableProjectiles;
    private Set<String> angleAgnosticProjectiles;
    private Set<String> unthrowableProjectiles;

    // Physics params
    private double bounceDistance;   // 160 grid units
    private double bounceHeight;     // 80 (Juggler) / 120 (Jane)
    private double bounceTime;       // 0.9 seconds

    // Juggler-specific
    private int catchArcDegrees;              // 120 — cone in front of zombie
    private int juggleLaunchDelay;            // 2 ticks between catching and re-throwing
    private int maxProjectilesToJuggle;       // 1000 (effectively unlimited)
    private double moveSpeedMultiplierWhileJuggling; // 1.1 — slightly faster while juggling

    public ProjectileDeflector(DeflectMode mode,
                               List<String> bounceableProjectiles,
                               double bounceDistance, double bounceHeight, double bounceTime) {
        this.mode = mode;
        this.bounceableProjectiles = new HashSet<>(bounceableProjectiles);
        this.bounceDistance = bounceDistance;
        this.bounceHeight = bounceHeight;
        this.bounceTime = bounceTime;
    }

    // Full constructor for Juggler
    public ProjectileDeflector(List<String> juggleableProjectiles,
                               List<String> bounceableProjectiles,
                               List<String> angleAgnosticProjectiles,
                               List<String> unthrowableProjectiles,
                               int catchArcDegrees, int juggleLaunchDelay,
                               int maxProjectilesToJuggle, double moveSpeedMultiplier,
                               double bounceDistance, double bounceHeight, double bounceTime) {
        this.mode = DeflectMode.JUGGLE;
        this.juggleableProjectiles = new HashSet<>(juggleableProjectiles);
        this.bounceableProjectiles = new HashSet<>(bounceableProjectiles);
        this.angleAgnosticProjectiles = new HashSet<>(angleAgnosticProjectiles);
        this.unthrowableProjectiles = new HashSet<>(unthrowableProjectiles);
        this.catchArcDegrees = catchArcDegrees;
        this.juggleLaunchDelay = juggleLaunchDelay;
        this.maxProjectilesToJuggle = maxProjectilesToJuggle;
        this.moveSpeedMultiplierWhileJuggling = moveSpeedMultiplier;
        this.bounceDistance = bounceDistance;
        this.bounceHeight = bounceHeight;
        this.bounceTime = bounceTime;
    }

    @Override
    public void onTick(Zombie zombie) {}

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public boolean canDeflect(String projectileId) {
        if (mode == DeflectMode.JUGGLE) return juggleableProjectiles.contains(projectileId);
        return bounceableProjectiles.contains(projectileId);
    }

    public boolean canThrowBack(String projectileId) {
        return mode == DeflectMode.JUGGLE
                && !unthrowableProjectiles.contains(projectileId);
    }

    public DeflectMode getMode() { return mode; }
    public double getBounceDistance() { return bounceDistance; }
    public double getBounceHeight() { return bounceHeight; }
    public double getBounceTime() { return bounceTime; }
    public double getMoveSpeedMultiplierWhileJuggling() { return moveSpeedMultiplierWhileJuggling; }
    public int getCatchArcDegrees() { return catchArcDegrees; }
}