package model.mechanisms;

import controller.MenuManager;
import model.GameContext;
import model.MiniGame.VaseGame.Vase;
import model.MiniGame.VaseGame.VaseContent;
import model.plants.PlantActivator;
import model.projectile.BulletType;
import model.projectile.Projectile;
import model.level.Level;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.projectile.TrajectoryType;
import model.zombie.Zombie;
import model.zombie.behavior.Behaviors;
import model.zombie.behavior.LaserShooting;
import model.zombie.behavior.ProjectileDeflector;
import model.zombie.behavior.Submerge;
import view.ConsoleView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private static final double LOSS_X = 0.0;
    private final GameContext ctx;
    private Tile[][] tiles;
    private LawnMower[] lawnMowers;
    private final Random random = new Random();
    private MenuManager menuManager;

    public GameEngine(GameContext ctx, MenuManager menuManager) {
        this.ctx = ctx;
        this.tiles = Tile.buildTiles(ctx);
        this.lawnMowers = LawnMower.buildLawnMowers();
        this.menuManager = menuManager;
    }

    public GameContext getCtx() {
        return ctx;
    }

    public void update(double deltaTime) {

        int passedTicks = (int)(deltaTime * 10);
        int rows = this.ctx.getLevel().getRows();
        int columns = this.ctx.getLevel().getColumns();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Tile tile = this.getTiles(i, j);
                if (tile != null && tile.hasDroppedSeed()) {
                    tile.updateSeedTimer(passedTicks);
                }
            }
        }

        if (!ctx.isBattleStarted() || ctx.isGameEnded()) return;

        if (ctx.getLevelManager() != null) {
            ctx.getLevelManager().onUpdate(deltaTime, ctx);
        }
        ctx.getSunManager().update(this);
        updateWave(deltaTime);
        updateZombies(deltaTime);
        updateLawnMowers(deltaTime);
        updatePlants(deltaTime);
        updateProjectiles(deltaTime);
        checkGameEnd();
    }

    private void updateWave(double deltaTime) {
        if (ctx.getLevel().getLevelType() == model.level.LevelType.PLANT_WHAT_YOU_GET) {
            if (!ctx.isManualStartCommandReceived()) {
                return;
            }
        }

        Wave[] waves = ctx.getLevel().getWaves();

        if (waves == null || waves.length == 0) {
            ctx.setWaveSpawningFinished(true);
            return;
        }

        if (ctx.getCurrentWaveIndex() == 0) {
            spawnWave(waves[0]);
            return;
        }

        if (ctx.getCurrentWaveIndex() >= waves.length) {
            ctx.setWaveSpawningFinished(true);
            return;
        }

        Wave previousWave = waves[ctx.getCurrentWaveIndex() - 1];
        if (previousWave.isThresholdReached()) {
            Wave nextWave = waves[ctx.getCurrentWaveIndex()];
            spawnWave(nextWave);
        }
    }    private void spawnWave(Wave wave) {
        wave.start(ctx);
        ctx.incrementWaveIndex();
        ctx.setActiveWaveInProgress(true);
    }

    private void updateZombies(double deltaTime) {
        List<Zombie> deathsThisTick = new ArrayList<>();
        Iterator<Zombie> it = ctx.getAliveZombies().iterator();
        while (it.hasNext()) {
            Zombie z = it.next();
            z.update(ctx, deltaTime);

            if (!z.isMovingBackward() && z.getX() <= LOSS_X) {
                LawnMower mower = lawnMowers[(int) z.getY()];
                if (!mower.isAvailable()) {
                    ctx.triggerPlayerLoss();
                    return;
                }
                if (!mower.isActivated()) {
                    mower.activate();
                }
            }
            if (z.isDead()) {
                for (Behaviors b : z.getBehaviors().values()) {
                    b.onDeath(z, ctx);
                }
                LootItem.tryDropLoot(ctx, (int) Math.floor(z.getX()), z.getRow());
                it.remove();
                ctx.incrementZombieKills();
                deathsThisTick.add(z);
            }
        }
        controller.ScoringManager.onZombiesDied(ctx, deathsThisTick);
    }

    private void updateLawnMowers(double deltaTime) {
        for (LawnMower l : lawnMowers) {
            if (!l.isActivated() || !l.isAvailable()) continue;

            for (Zombie z : getRowZombies(l.getRow())) {
                l.trigger(z);
            }
            l.advance(deltaTime);

            if (l.isDidKilled()) {
                ctx.incrementZombieKills();
                l.setDidKilled(false);
            }
        }
    }


    public Zombie[] getRowZombies(int row) {
        return ctx.getAliveZombies().stream()
                .filter(z -> z.getY() == row)
                .toArray(Zombie[]::new);
    }

    private void updatePlants(double deltaTime) {
        Iterator<Plant> it = ctx.getAlivePlants().iterator();
        while (it.hasNext()) {
            Plant p = it.next();
            PlantActivator.activate(p, ctx, this);
            if (p.getHp() <= 0) {
                ctx.getPlantGrid()[p.getRow()][p.getCol()] = null;
                it.remove();
                ctx.incrementPlantsLost(p);
            }
        }
    }

    public void removePlant(int row, int col) {
        Plant p = ctx.getPlantGrid()[row][col];
        if (p != null) {
            ctx.getPlantGrid()[row][col] = null;
            ctx.getAlivePlants().remove(p);
        }
    }

    public void updateProjectiles(double deltaTime) {
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
    }

    private void handleZombieProjectile(Projectile p, Iterator<Projectile> it) {
        Plant target = ctx.getPlantGrid()[p.getRow()][(int) p.getX()];
        if (target != null && !target.isDead()) {
            p.onHit(target);
            if (!p.isActive()) {
                it.remove();
            }
        }
    }

    private void handlePlantProjectile(Projectile p, Iterator<Projectile> it) {
        if (checkPlantObstacle(p)) {
            p.deactivate();
            it.remove();
            return;
        }

        checkZombieHit(p, it);
    }

    private boolean checkPlantObstacle(Projectile p) {
        int pRow = p.getRow();
        int pCol = (int) Math.floor(p.getX());

        if (pCol < 0 || pCol >= Level.COLS) return false;

        Plant plantInCell = ctx.getPlantGrid()[pRow][pCol];
        if (plantInCell == null || plantInCell.isDead()) return false;

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
        for (Zombie z : ctx.getAliveZombies()) {
            if (z.getRow() == p.getRow() && Math.abs(z.getX() - p.getX()) < 0.4) {
                ProjectileDeflector deflector = z.getDeflector();
                Submerge submerge = z.getSubmerge();

                if (deflector != null && deflector.canDeflect(p)) {
                    deflector.deflect(p, ctx, z);
                    it.remove();
                    break;
                }

                if (submerge != null && !submerge.isVulnerableTo(p)) {
                    continue;
                }

                long deadBefore = ctx.getAliveZombies().stream().filter(Zombie::isDead).count();
                p.onHit(z);
                long deadAfter = ctx.getAliveZombies().stream().filter(Zombie::isDead).count();
                long newlyKilled = deadAfter - deadBefore;
                for (int i = 0; i < newlyKilled; i++) {
                    p.incrementKillCount();
                }


                LaserShooting laser = (LaserShooting) z.getBehaviors().get("laser");
                if (laser != null) {
                    laser.onProjectileHit(p.getBulletType());
                }

                if (!p.isActive()) {
                    if (p.getKillCount() >= 2) {
                        controller.ScoringManager.onProjectileKill(ctx, p.getKillCount());
                    }
                    it.remove();
                }

                break;
            }
        }
    }


    private void checkGameEnd() {
        if (ctx.isGameEnded()) {
            ConsoleView.showMessage("You are now in Game Menu.\n");
            menuManager.forceChangeMenu("gamemenu");
            ctx.clearLoots();
            return;
        }
        boolean allSpawned = ctx.isWaveSpawningFinished() ||
                (ctx.getLevel().getWaves() != null && ctx.getCurrentWaveIndex() >= ctx.getLevel().getWaves().length);
        if (allSpawned && ctx.getAliveZombies().isEmpty()) {
            ctx.triggerPlayerWin();
        }
    }

    public List<Zombie> findTargets(int row, int col, TargetingMode mode) {
        List<Zombie> sameRow = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if ((int) z.getY() == row) sameRow.add(z);
        }
        switch (mode) {
            case FIRST_IN_LANE -> {
                sameRow.sort((a, b) -> Double.compare(a.getX(), b.getX()));
                return sameRow.isEmpty() ? sameRow : sameRow.subList(0, 1);
            }
            case NEAREST -> {
                List<Zombie> result = new ArrayList<>();
                Zombie nearest = null;
                double bestDist = Double.MAX_VALUE;
                for (Zombie z : ctx.getAliveZombies()) {
                    double dRow = z.getY() - row;
                    double dCol = z.getX() - col;
                    double dist = Math.hypot(dRow, dCol);
                    if (dist < bestDist) {
                        bestDist = dist;
                        nearest = z;
                    }
                }
                if (nearest != null) result.add(nearest);
                return result;
            }
            case RANDOM -> {
                List<Zombie> all = ctx.getAliveZombies();
                List<Zombie> result = new ArrayList<>();
                if (!all.isEmpty()) {
                    result.add(all.get(random.nextInt(all.size())));
                }
                return result;
            }
            default -> {
                return sameRow;
            }
        }
    }
    public Tile getTiles(int x, int y) {
        if (y < 0 || y >= tiles.length || x < 0 || x >= tiles[0].length) return null;
        return tiles[y][x];
    }
    public void smashVase(int row, int col, GameContext ctx) {
        Tile tile = this.getTiles(row, col);

        if (tile == null){
            view.ConsoleView.simplePrint("Invalid coordinates!\n");
            return;
        }

        Vase vase = tile.getVase();

        if (vase == null){
            view.ConsoleView.simplePrint("There is no vase at (" +  row + ", " + col + ")!\n");
            return;
        }

        if (vase.isBroken()){
            view.ConsoleView.simplePrint("Vase broken!\n");
            return;
        }

        vase.setBroken(true);
        view.ConsoleView.simplePrint("Crash! you smashed the vase at (" +  row + ", " + col + ")!\n");

        if (vase.getContent() == VaseContent.ZOMBIE) {
            String zombieName = vase.getHiddenEntityName();

            if (zombieName == null || zombieName.isEmpty() || zombieName.equalsIgnoreCase("Zombie")) {
                zombieName = "Gargantuar";
            }

            try {
                model.zombie.Zombie newZombie = ctx.getZombieFactory().create(zombieName);

                newZombie.setX(col);
                newZombie.setRow(row);

                ctx.getAliveZombies().add(newZombie);
                view.ConsoleView.simplePrint("total zombie: " + ctx.getActiveZombies().size() + " | zombie X = : " + newZombie.getX() + " | zombie Y = : " + newZombie.getRow());

                if (zombieName.equalsIgnoreCase("Gargantuar")) {
                    view.ConsoleView.simplePrint("ROAR! A Gargantuar emerged from the vase at (" + col + ", " + row + ")!\n");
                } else {
                    view.ConsoleView.simplePrint("A " + zombieName + " popped out of the vase at (" + col + ", " + row + ")!\n");
                }

            } catch (IllegalArgumentException e) {
                view.ConsoleView.simplePrint("Failed to spawn zombie: " + e.getMessage());
            }

        } else if (vase.getContent() == VaseContent.PLANT) {
            tile.setDroppedSeed(vase.getHiddenEntityName(), 100);
            view.ConsoleView.simplePrint("A seed packet for " + vase.getHiddenEntityName() + "dropped at (" + row + ", " + col + ")!\n");
        }
    }

    public LawnMower[] getLawnMowers() {return lawnMowers;}

}