package com.workshop.model.mechanisms;

import com.workshop.controller.MenuManager;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.VaseGame.Vase;
import com.workshop.model.MiniGame.VaseGame.VaseContent;
import com.workshop.model.plants.PlantActivator;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.level.Level;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.TargetingMode;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.Behaviors;
import com.workshop.model.zombie.behavior.LaserShooting;
import com.workshop.model.zombie.behavior.ProjectileDeflector;
import com.workshop.model.zombie.behavior.Submerge;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.level.LevelType;

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
        if (ctx.isPaused()) {
            return;
        }

        int passedTicks = (int)(deltaTime * 10);
        int rows = this.ctx.getLevel().getRows();
        int columns = this.ctx.getLevel().getColumns();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Tile tile = this.getTiles(j, i);
                if (tile != null && tile.hasDroppedSeed()) {
                    tile.updateSeedTimer(passedTicks);
                }
            }
        }

        if (!ctx.isBattleStarted()) {
            return;
        }

        if (ctx.isGameEnded()) {
            checkGameEnd();
            return;
        }

        if (ctx.getLevelManager() != null) {
            ctx.getLevelManager().onUpdate(deltaTime, ctx);
        }
        ctx.getSunManager().update(this);
        updateWave(deltaTime);
        updateZombies(deltaTime);

        if (ctx.isGameEnded()) {
            checkGameEnd();
            return;
        }

        if (getIZombieManager() == null) {
            updateLawnMowers(deltaTime);
        }

        updatePlants(deltaTime);
        updateProjectiles(deltaTime);
        checkGameEnd();
    }

    private void updateWave(double deltaTime) {
        if (getIZombieManager() != null) {
            ctx.setWaveSpawningFinished(true);
            return;
        }

        if (ctx.getLevel().getLevelType() == com.workshop.model.level.LevelType.PLANT_WHAT_YOU_GET) {
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
            ctx.recordFirstWaveStart();
            spawnWave(waves[0]);
            return;
        }

        if (ctx.getCurrentWaveIndex() >= waves.length) {
            if (ctx.getLevel().getLevelType()
                == com.workshop.model.level.LevelType.Beghouled_MG) {

                int previousIndex =
                    (ctx.getCurrentWaveIndex() - 1) % waves.length;

                Wave previousWave = waves[previousIndex];

                if (previousWave.isThresholdReached()) {
                    int nextIndex =
                        ctx.getCurrentWaveIndex() % waves.length;

                    Wave nextWave = waves[nextIndex];
                    nextWave.reset();
                    spawnWave(nextWave);
                }

                return;
            }

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

        Iterator<Zombie> iterator =
            ctx.getAliveZombies().iterator();

        IZombieManager iZombieManager =
            getIZombieManager();

        while (iterator.hasNext()) {
            Zombie zombie = iterator.next();


            zombie.update(ctx, deltaTime);


            if (iZombieManager != null
                && !zombie.isDead()
                && !zombie.isMovingBackward()
                && zombie.getX() <= LOSS_X) {

                iZombieManager.eatBrain(
                    zombie.getRow()
                );

                iterator.remove();
                continue;
            }


            if (iZombieManager == null
                && !zombie.isMovingBackward()
                && zombie.getX() <= LOSS_X) {

                LawnMower mower =
                    lawnMowers[(int) zombie.getY()];

                if (!mower.isAvailable()) {
                    ctx.triggerPlayerLoss();
                    return;
                }

                if (!mower.isActivated()) {
                    mower.activate();
                }
            }

            if (zombie.isDead()) {
                for (
                    Behaviors behavior
                    : zombie.getBehaviors().values()
                ) {
                    behavior.onDeath(zombie, ctx);
                }

                LootItem.tryDropLoot(
                    ctx,
                    (int) Math.floor(zombie.getX()),
                    zombie.getRow()
                );

                iterator.remove();

                ctx.incrementZombieKills();

                deathsThisTick.add(zombie);

                ctx.recordZombieKillTick();


                if (iZombieManager == null) {
                    boolean noMowerLeftInRow =
                        !lawnMowers[
                            (int) zombie.getY()
                            ].isAvailable();

                    if (noMowerLeftInRow
                        && Math.floor(zombie.getX()) == 0) {

                        ctx.recordAlmostLostKill();
                    }
                }
            }
        }

        com.workshop.controller.ScoringManager
            .onZombiesDied(ctx, deathsThisTick);
    }

    private void updateLawnMowers(double deltaTime) {
        for (LawnMower l : lawnMowers) {
            if (!l.isActivated() || !l.isAvailable()) continue;

            for (Zombie z : getRowZombies(l.getRow())) {
                boolean aliveBefore = !z.isDead();
                l.trigger(z);
                if (aliveBefore && z.isDead()) {
                    ctx.getAliveZombies().remove(z);
                    ctx.incrementZombieKills();
                    ctx.recordLawnMowerKill();
                }
            }
            l.advance(deltaTime);

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
            if (p.getName() != null && p.getName().equalsIgnoreCase("Imitater")) {
                continue;
            }
            if (p.getHp() <= 0) {
                ctx.getPlantGrid()[p.getRow()][p.getCol()] = null;
                if (ctx.getLevel().getLevelType() == com.workshop.model.level.LevelType.Beghouled_MG
                        && ctx.getBeghouldManager() != null) {
                    ctx.getBeghouldManager().markCrater(p.getRow(), p.getCol());
                }
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
        for (Zombie z : ctx.getAliveZombies()) {
            if (z.isDead()){
                continue;
            }
            if (z.getRow() == p.getRow() && p.getX() >= z.getX() - 0.2 && p.getX() <= z.getX() + 0.6) {
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

                boolean aliveBeforeHit = !z.isDead();
                long deadBefore = ctx.getAliveZombies().stream().filter(Zombie::isDead).count();
                p.onHit(z);
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

    private void checkGameEnd() {
        if (ctx.isGameEnded()) {
            ctx.setBattleStarted(false);

            LevelType type = ctx.getLevel().getLevelType();

            if (type == LevelType.Wallnuts_MG
                || type == LevelType.Vase_MG
                || type == LevelType.Izambie_MG
                || type == LevelType.Beghouled_MG
                || type == LevelType.Zombotany_MG) {

                menuManager.forceChangeMenu("travelmenu");
            } else {
                menuManager.forceChangeMenu("gamemenu");
            }

            ctx.clearLoots();
            return;
        }

        IZombieManager iZombieManager =
            getIZombieManager();

        if (iZombieManager != null) {
            if (iZombieManager.areAllBrainsEaten()) {
                ctx.triggerPlayerWin();
            } else if (iZombieManager.shouldPlayerLose(ctx)) {
                ctx.triggerPlayerLoss();
            }

            return;
        }

        if (ctx.getLevel().getLevelType()
            == com.workshop.model.level.LevelType.Vase_MG) {

            boolean noZombiesRemain = ctx.getAliveZombies().isEmpty();
            boolean allVasesAreBroken = !hasUnbrokenVases();

            if (noZombiesRemain && allVasesAreBroken) {
                ctx.triggerPlayerWin();
            }

            return;
        }

        if (ctx.getLevel().getLevelType()
            == com.workshop.model.level.LevelType.Beghouled_MG) {
            return;
        }

        boolean allSpawned = ctx.isWaveSpawningFinished()
            || (ctx.getLevel().getWaves() != null
            && ctx.getCurrentWaveIndex()
            >= ctx.getLevel().getWaves().length);

        if (allSpawned && ctx.getAliveZombies().isEmpty()) {
            ctx.triggerPlayerWin();
        }
    }

    private boolean hasUnbrokenVases() {
        int rows = ctx.getLevel().getRows();
        int columns = ctx.getLevel().getColumns();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Tile tile = getTiles(column, row);

                if (tile != null
                    && tile.getVase() != null
                    && !tile.getVase().isBroken()) {
                    return true;
                }
            }
        }

        return false;
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

    private IZombieManager getIZombieManager() {
        if (
            ctx.getLevelManager()
                instanceof IZombieManager manager
        ) {
            return manager;
        }

        return null;
    }

    public Tile getTiles(int x, int y) {
        if (y < 0 || y >= tiles.length || x < 0 || x >= tiles[0].length) return null;
        return tiles[y][x];
    }
    public void smashVase(int col, int row, GameContext ctx) {
        Tile tile = this.getTiles(col, row);

        if (tile == null){
            com.workshop.view.Console.simplePrint("Invalid coordinates!\n");
            return;
        }

        Vase vase = tile.getVase();

        if (vase == null){
            com.workshop.view.Console.simplePrint("There is no vase at (" +  row + ", " + col + ")!\n");
            return;
        }

        if (vase.isBroken()){
            com.workshop.view.Console.simplePrint("Vase broken!\n");
            return;
        }

        vase.setBroken(true);
        tile.setVase(null); //DebugF
        com.workshop.view.Console.simplePrint("Crash! you smashed the vase at (" +  col + ", " + row + ")!\n");

        if (vase.getContent() == VaseContent.ZOMBIE) {
            String zombieName = vase.getHiddenEntityName();

            if (zombieName == null || zombieName.isEmpty()) {
                zombieName = "Default";
            } else if (zombieName.equalsIgnoreCase("Zombie")) {
                zombieName = "Default";
            }

            try {
                com.workshop.model.zombie.Zombie newZombie = ctx.getZombieFactory().create(zombieName);

                newZombie.setX(col);
                newZombie.setRow(row);
                newZombie.setY(row); //DebugF

                ctx.getAliveZombies().add(newZombie);

                if (zombieName.equalsIgnoreCase("Gargantuar")) {
                    com.workshop.view.Console.simplePrint("ROAR! A Gargantuar emerged from the vase at " +
                            "(" + col + ", " + row + ")!\n");
                } else {
                    com.workshop.view.Console.simplePrint("A " + zombieName + " popped out of the vase at " +
                            "(" + col + ", " + row + ")!\n");
                }

            } catch (IllegalArgumentException e) {
                com.workshop.view.Console.simplePrint("Failed to spawn zombie: " + e.getMessage() + "\n");
            }

        } else if (vase.getContent() == VaseContent.PLANT) {
            tile.setDroppedSeed(vase.getHiddenEntityName(), 100);
            com.workshop.view.Console.simplePrint("A seed packet for " + vase.getHiddenEntityName() + "dropped at" +
                    " (" + col + ", " + row + ")!\n");
        }
    }

    public LawnMower[] getLawnMowers() {return lawnMowers;}


}
