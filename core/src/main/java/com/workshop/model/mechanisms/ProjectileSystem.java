package com.workshop.model.mechanisms;

import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.season.Grave;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.LaserShooting;
import com.workshop.model.zombie.behavior.ProjectileDeflector;
import com.workshop.model.zombie.behavior.Submerge;

import java.util.ArrayList;
import java.util.Iterator;

public final class ProjectileSystem {

    private final GameContext ctx;

    public ProjectileSystem(GameContext ctx) {
        this.ctx = ctx;
    }

    public void update(double deltaTime) {
        ctx.flushPendingProjectiles();
        Iterator<Projectile> it = ctx.getProjectiles().iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(deltaTime);

            if (!p.isActive() || p.isOutOfBounds()) {
                p.deactivate();
                it.remove();
                continue;
            }

            if (p.isFromZombie()) {
                handleZombieProjectile(p, it);
            } else {
                handlePlantProjectile(p, it);
            }
        }
        ctx.flushPendingProjectiles();
    }

    private void handleZombieProjectile(Projectile p, Iterator<Projectile> it) {
        Plant target = ctx.getPlantGrid()[p.getRow()][(int) p.getX()];
        if (target != null && !target.isDead()) {
            spawnProjectileHit(p);
            p.onHit(target);
            if (!p.isActive()) {
                it.remove();
            }
        }
    }

    private void handlePlantProjectile(Projectile p, Iterator<Projectile> it) {
        if (checkPlantObstacle(p)) {
            spawnProjectileHit(p);
            p.deactivate();
            it.remove();
            return;
        }

        checkZombieHit(p, it);

        if (p.isActive()) {
            checkGraveHit(p, it);
        }
    }

    private void spawnProjectileHit(Projectile p) {
        ctx.spawnProjectileHit(p.getRow(), p.getX(), p.getY());
    }

    private void checkGraveHit(Projectile p, Iterator<Projectile> it) {
        int row = p.getRow();
        int col = (int) Math.floor(p.getX());
        if (p.getBulletType() == BulletType.MAGIC || p.getTrajectory() == TrajectoryType.LOBBED) {
            return;
        }

        if (row < 0 || row >= ctx.getLevel().getRows()
            || col < 0 || col >= ctx.getLevel().getColumns()) {
            return;
        }

        Grave grave = ctx.getGraveGrid()[row][col];
        if (grave == null) {
            return;
        }

        grave.takeDamage(p.getDamage(), ctx);

        if (p.getTrajectory() != TrajectoryType.PIERCING) {
            spawnProjectileHit(p);
            p.deactivate();
            it.remove();
        }
    }

    private boolean checkPlantObstacle(Projectile p) {
        int pRow = p.getRow();
        int pCol = (int) Math.floor(p.getX());

        if (pCol < 0 || pCol >= Level.COLS) return false;

        if (p.getTrajectory() == TrajectoryType.LOBBED) {
            return false;
        }

        Plant plantInCell = ctx.getPlantGrid()[pRow][pCol];
        if (plantInCell == null || plantInCell.isDead()) return false;

        if (p.getTrajectory() == TrajectoryType.BOWLING) {
            int maxRows = ctx.getLevel().getRows();
            int newRow = (pRow == 0) ? pRow + 1
                : (pRow == maxRows - 1) ? pRow - 1
                  : (Math.random() < 0.5 ? pRow - 1 : pRow + 1);
            p.bounceLane(newRow);
            return false;
        }

        boolean isBlocked = plantInCell.isIced() || plantInCell.isOctopused();
        if (isBlocked && p.getTrajectory() != TrajectoryType.LOBBED) {
            double effectiveDamage = (p.getBulletType() == BulletType.FIRE) ? p.getDamage() * 2 : p.getDamage();

            if (plantInCell.isOctopused()) {
                plantInCell.damageOctopuse(effectiveDamage);
            } else if (plantInCell.isIced()) {
                if (p.getBulletType() == BulletType.FIRE) {
                    plantInCell.meltIce();
                } else {
                    plantInCell.damageIce(effectiveDamage);
                }
            }
            return true;
        }
        return false;
    }

    private void checkZombieHit(Projectile p, Iterator<Projectile> it) {
        for (Zombie z : new ArrayList<>(ctx.getAliveZombies())) {
            if (z.isDead()){
                continue;
            }
            if (!z.occupiesRow(p.getRow())) {
                continue;
            }
            double hitLeft = z.isBoss() ? z.getX() - 0.5 : z.getX() - 0.2;
            double hitRight = z.isBoss() ? z.getX() + 1.6 : z.getX() + 0.6;
            if (p.getX() >= hitLeft && p.getX() <= hitRight) {
                ProjectileDeflector deflector = z.getDeflector();
                Submerge submerge = z.getSubmerge();

                if (deflector != null && deflector.canDeflect(p)) {
                    spawnProjectileHit(p);
                    deflector.deflect(p, ctx, z);
                    it.remove();
                    break;
                }

                if (submerge != null && !submerge.isVulnerableTo(p)) {
                    continue;
                }

                boolean firstHit = !p.hasAlreadyHit(z);
                boolean aliveBeforeHit = !z.isDead();
                long deadBefore = ctx.getAliveZombies().stream().filter(Zombie::isDead).count();
                p.onHit(z);
                if (firstHit) {
                    spawnProjectileHit(p);
                }
                long deadAfter = ctx.getAliveZombies().stream().filter(Zombie::isDead).count();
                long newlyKilled = deadAfter - deadBefore;
                for (int i = 0; i < newlyKilled; i++) {
                    p.incrementKillCount();
                }
                if (aliveBeforeHit && z.isDead()) {
                    ctx.recordPlantKill(p.getOwnerPlant());
                }

                applyLobberSplash(p, z);


                LaserShooting laser = (LaserShooting) z.getBehaviors().get("laser");
                if (laser != null) {
                    laser.onProjectileHit(p.getBulletType());
                }

                if (!p.isActive()) {
                    if (p.getKillCount() >= 2) {
                        com.workshop.controller.ScoringManager.onProjectileKill(ctx, p.getKillCount());
                    }
                    it.remove();
                }

                break;
            }
        }
    }

    private void applyLobberSplash(Projectile p, Zombie primaryTarget) {
        com.workshop.model.plants.Plant owner = p.getOwnerPlant();
        if (owner == null || owner.getAbilityParams() == null) return;
        String lobType = owner.getAbilityParams().get("lobType");
        if (!"AOE".equals(lobType) && !"AOE_ICE".equals(lobType) && !"AOE_FIRE".equals(lobType)) return;

        for (Zombie other : ctx.getAliveZombies()) {
            if (other == primaryTarget || other.isDead()) continue;
            if (Math.abs(other.getRow() - primaryTarget.getRow()) <= 1
                && Math.abs(other.getX() - primaryTarget.getX()) <= 1.0) {
                other.takeDamage(p.getDamage());
                if ("AOE_ICE".equals(lobType)) {
                    other.applySlowOrFreeze();
                }
            }
        }
    }
}
