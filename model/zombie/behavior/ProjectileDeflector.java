package model.zombie.behavior;

import model.GameContext;
import model.projectile.Projectile;
import model.projectile.TrajectoryType;
import model.zombie.Zombie;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Used by:
 *   - ZombieDarkJuggler  — spins and redirects direct shots back at plants
 *   - ZombieLostCityJane — bounces specific lobbed projectiles back
 *   - ZombieParasol      — کاملاً بی‌اثر می‌کنه شلیک‌های lobber
 */
public class ProjectileDeflector implements Behaviors {

    public enum DeflectMode {
        JUGGLE,  // Juggler: catches projectiles and throws them back at plants
        BOUNCE,  // Jane: deflects specific lobbed projectiles back (list-based)
        BLOCK    // Parasol: نابود می‌کنه، برنمی‌گردونه
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

    private boolean spinning = false;
    private double baseSpeed = -1;
    private long lastHitTick = -1;

    private static final int TICKS_PER_SECOND = 10;
    private static final double SPIN_IDLE_TIMEOUT_SECONDS = 1.5;
    public ProjectileDeflector(DeflectMode mode,
                               List<String> bounceableProjectiles,
                               double bounceDistance, double bounceHeight, double bounceTime) {
        this.mode = mode;
        this.bounceableProjectiles = new HashSet<>(bounceableProjectiles);
        this.bounceDistance = bounceDistance;
        this.bounceHeight = bounceHeight;
        this.bounceTime = bounceTime;
    }

    public ProjectileDeflector() {
        this.mode = DeflectMode.BLOCK;
        this.bounceableProjectiles = new HashSet<>();
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
    public void onTick(Zombie zombie, GameContext ctx) {
        if (mode != DeflectMode.JUGGLE || !spinning) return;

        long now = ctx.getTimeManager().getTotalTicks();
        if (lastHitTick >= 0 && (now - lastHitTick) > (long) (SPIN_IDLE_TIMEOUT_SECONDS * TICKS_PER_SECOND)) {
            spinning = false;
            if (baseSpeed >= 0) {
                zombie.setSpeed(baseSpeed);
                baseSpeed = -1;
            }
        }
    }

    @Override
    public void onHit(Zombie zombie, int damage) {}

    @Override
    public boolean isDestroyed() { return false; }

    public boolean canDeflect(Projectile p) {
        return switch (mode) {
            case JUGGLE -> p.getTrajectory() == TrajectoryType.STRAIGHT;
            case BLOCK -> p.getTrajectory() == TrajectoryType.LOBBED;
            case BOUNCE -> p.getTrajectory() == TrajectoryType.LOBBED;
        };
    }

    public void deflect(Projectile p, GameContext ctx, Zombie zombie) {
        p.deactivate();

        if (mode == DeflectMode.BLOCK) {
            return;
        }

        lastHitTick = ctx.getTimeManager().getTotalTicks();
        if (!spinning) {
            spinning = true;
            baseSpeed = zombie.getSpeed();
            zombie.setSpeed(baseSpeed * 1.1);
        }

        Projectile reflected = new Projectile(
                p.getDamage(), p.getX(), p.getY(), p.getRow(),
                p.getSpeed(), p.getBulletType(), p.getTrajectory(),
                true, null
        );
        ctx.setNewProjectiles(reflected);
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
    public boolean isSpinning() { return spinning; }
}