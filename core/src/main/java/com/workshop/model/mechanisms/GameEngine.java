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
import com.workshop.model.season.Grave;
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
        if (ctx.getSeason() != null) {
            ctx.getSeason().onTick(ctx, deltaTime);
        }

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
            boolean startImmediately =
                ctx.getLevel().getLevelType()
                    == com.workshop.model.level.LevelType.PLANT_WHAT_YOU_GET;
            long delayStart = ctx.getManualWaveStartTick();
            int delay = startImmediately ? 0 : waves[0].getWaveDelay();
            if (ctx.getTimeManager().getTotalTicks() < delayStart + delay) {
                return;
            }
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

        List<Zombie> deathsThisTick =
            new ArrayList<>();

        List<Zombie> zombiesSnapshot =
            new ArrayList<>(
                ctx.getAliveZombies()
            );

        IZombieManager iZombieManager =
            getIZombieManager();

        for (Zombie zombie : zombiesSnapshot) {

            // ممکن است قبلاً توسط اتفاق دیگری حذف شده باشد
            if (!ctx.getAliveZombies().contains(zombie)) {
                continue;
            }

            zombie.update(
                ctx,
                deltaTime
            );

            despawnIfWalkedOffLawn(zombie);

            // ممکن است update باعث حذفش شده باشد
            if (!ctx.getAliveZombies().contains(zombie)) {
                continue;
            }

            if (iZombieManager != null
                && !zombie.isDead()
                && !zombie.isMovingBackward()
                && zombie.getX() <= LOSS_X) {

                boolean brainWasEaten =
                    iZombieManager.attackBrain(
                        zombie,
                        ctx
                    );

                if (brainWasEaten) {
                    killAllZombiesInRow(
                        zombie.getRow()
                    );
                }

                continue;
            }

            if (iZombieManager == null
                && !zombie.isBoss()
                && !zombie.isMovingBackward()
                && zombie.getX() <= LOSS_X) {

                LawnMower mower =
                    lawnMowers[
                        (int) zombie.getY()
                        ];

                if (!mower.isAvailable()) {
                    ctx.triggerPlayerLoss();
                    return;
                }

                if (!mower.isActivated()) {
                    mower.activate();

                    ctx.playSound(
                        "sfx:music/lawnmower"
                    );
                }
            }

            if (zombie.isDead()) {
                if (zombie.isAshed() && !zombie.isAshFinished()) {
                    continue;
                }
                if (!zombie.isAshed() && !zombie.isDeathAnimFinished()) {
                    continue;
                }

                for (
                    Behaviors behavior
                    : zombie.getBehaviors().values()
                ) {
                    behavior.onDeath(
                        zombie,
                        ctx
                    );
                }

                LootItem.tryDropLoot(
                    ctx,
                    (int) Math.floor(
                        zombie.getX()
                    ),
                    zombie.getRow()
                );

                ctx.getAliveZombies()
                    .remove(zombie);

                ctx.incrementZombieKills();

                deathsThisTick.add(
                    zombie
                );

                ctx.recordZombieKillTick();

                if (iZombieManager == null) {

                    boolean noMowerLeftInRow =
                        !lawnMowers[
                            (int) zombie.getY()
                            ].isAvailable();

                    if (noMowerLeftInRow
                        && Math.floor(
                        zombie.getX()
                    ) == 0) {

                        ctx.recordAlmostLostKill();
                    }
                }
            }
        }

        com.workshop.controller.ScoringManager
            .onZombiesDied(
                ctx,
                deathsThisTick
            );
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
            .filter(z -> z.occupiesRow(row))
            .toArray(Zombie[]::new);
    }

    private void updatePlants(double deltaTime) {

        List<Plant> plantsSnapshot =
            new ArrayList<>(
                ctx.getAlivePlants()
            );

        int currentSecond = ctx.getTimeManager().getTotalSeconds();
        for (Plant p : plantsSnapshot) {
            if (!ctx.getAlivePlants().contains(p)) {
                continue;
            }
            if (p.getName() != null) {
                String pName = p.getName().replace("-", "").toLowerCase();
                if (pName.equals("puffshroom") || pName.equals("seashroom")) {
                    if (p.getPlantTimeSecond() == 0) {
                        p.setPlantTimeSecond(currentSecond);
                    }
                    if (currentSecond - p.getPlantTimeSecond() >= 60) {
                        p.takeDamage(Integer.MAX_VALUE);
                    }
                }
            }

            PlantActivator.activate(
                p,
                ctx,
                this
            );

            if (!ctx.getAlivePlants().contains(p)) {
                continue;
            }

            if (p.getName() != null
                && p.getName()
                .equalsIgnoreCase("Imitater")) {

                continue;
            }

            flushTimedOutShots(p);

            if (p.isDead()) {
                p.discardPendingShots();
                int row = p.getRow();
                int col = p.getCol();
                boolean restoreLilyPad = p.isHasLilyPadUnderneath();

                if (row >= 0 && col >= 0
                    && row < ctx.getPlantGrid().length
                    && col < ctx.getPlantGrid()[row].length
                    && ctx.getPlantGrid()[row][col] == p) {
                    ctx.getPlantGrid()[row][col] = null;
                }
                ctx.removePulledPlant(p);

                if (ctx.getLevel().getLevelType()
                    == LevelType.Beghouled_MG
                    && ctx.getBeghouldManager() != null) {

                    ctx.getBeghouldManager()
                        .markCrater(row, col);
                    restoreLilyPad = false;
                }

                ctx.getAlivePlants().remove(p);

                ctx.incrementPlantsLost(p);

                if (restoreLilyPad) {
                    restoreLilyPad(row, col);
                }
            }
        }
    }
    public void removePlant(int row, int col) {
        Plant p = ctx.getPlantGrid()[row][col];
        if (p != null) {
            p.discardPendingShots();
            ctx.getPlantGrid()[row][col] = null;
            ctx.getAlivePlants().remove(p);
        }
    }

    private void flushTimedOutShots(Plant plant) {
        if (!plant.hasPendingShots()) {
            return;
        }
        long armed = plant.getPendingShotArmedTick();
        if (armed < 0) {
            return;
        }
        if (ctx.getTimeManager().getTotalTicks() - armed >= 15) {
            plant.releaseAllPendingShots(ctx);
        }
    }

    private void restoreLilyPad(int row, int col) {
        if (row < 0 || col < 0
            || row >= ctx.getPlantGrid().length
            || col >= ctx.getPlantGrid()[row].length) {
            return;
        }
        if (ctx.getPlantGrid()[row][col] != null) {
            return;
        }
        try {
            Plant pad = ctx.getPlantFactory().create("Lily Pad");
            pad.setRow(row);
            pad.setCol(col);
            ctx.getPlantGrid()[row][col] = pad;
            ctx.getAlivePlants().add(pad);
        } catch (RuntimeException ignored) {
        }
    }

    public void updateProjectiles(double deltaTime) {
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

        if (p.isActive()) {
            checkGraveHit(p, it);
        }
    }

    private void checkGraveHit(Projectile p, Iterator<Projectile> it) {
        int row = p.getRow();
        int col = (int) Math.floor(p.getX());

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
            p.deactivate();
            it.remove();
        }
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
            if (ctx.isExternalWinLossHandling()) {
                // A networked/couch I-Zombie match decides win/lose itself
                // (2-player rules differ from the single-player campaign).
                return;
            }
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

        if (allSpawned && !hasRemainingEnemyZombies()) {
            ctx.triggerPlayerWin();
        }
    }

    private boolean hasRemainingEnemyZombies() {
        for (Zombie zombie : ctx.getAliveZombies()) {
            if (zombie == null || zombie.isDead()) {
                continue;
            }
            if (zombie.isInitialFrozenBlock()) {
                continue;
            }
            return true;
        }
        return false;
    }

    private void despawnIfWalkedOffLawn(Zombie zombie) {
        if (zombie == null || zombie.isDead()) {
            return;
        }
        double rightEdge = ctx.getLevel().getColumns() + 0.35;
        if (zombie.isMovingBackward() && zombie.getX() >= rightEdge) {
            zombie.takeArmorPiercingDamage(Math.max(1, zombie.getHp()));
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
            if (z.occupiesRow(row)) sameRow.add(z);
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

    private void killAllZombiesInRow(int row) {

        List<Zombie> zombies =
            new ArrayList<>(
                ctx.getAliveZombies()
            );

        for (Zombie zombie : zombies) {

            if (!zombie.occupiesRow(row)) {
                continue;
            }

            if (zombie.isDead()) {
                continue;
            }

            zombie.setEating(false);

            zombie.takeArmorPiercingDamage(
                zombie.getHp() + 1
            );
        }
    }
}
