package model.zombie.behavior;

import model.GameContext;
import model.projectile.BulletType;
import model.projectile.Projectile;
import model.projectile.TrajectoryType;
import model.zombie.Zombie;

public class ProjectileDeflector implements Behaviors {

    public enum DeflectMode {
        JUGGLE,  // Juggler: catches projectiles and throws them back at plants
        BOUNCE,  // Jane: deflects specific lobbed projectiles back (list-based)
        BLOCK    // Parasol: نابود می‌کنه، برنمی‌گردونه
    }

    private DeflectMode mode;

    private double moveSpeedMultiplierWhileJuggling = 1.5;

    private boolean spinning = false;
    private double baseSpeed = -1;
    private long lastHitTick = -1;

    private static final int TICKS_PER_SECOND = 10;
    private static final double SPIN_IDLE_TIMEOUT_SECONDS = 1.5;

    public ProjectileDeflector(DeflectMode mode,
                               double bounceDistance, double bounceHeight, double bounceTime) { //parasol
        this.mode = mode;
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
            zombie.setSpeed(baseSpeed * moveSpeedMultiplierWhileJuggling);
        }

        if (mode == DeflectMode.JUGGLE) {
            Projectile reflected = new Projectile(
                    p.getDamage(),
                    p.getX(),
                    p.getY(),
                    p.getRow(),
                    p.getSpeed(),
                    p.getBulletType(),
                    p.getTrajectory(),
                    true,
                    p.getOwnerPlant());
            ctx.setNewProjectiles(reflected);
        }
    }
}