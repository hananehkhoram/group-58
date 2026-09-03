package com.workshop.model.zombie.behavior;

import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.ExplosionFx;
import com.workshop.model.plants.Plant;
import com.workshop.model.season.FrozenCaveChapter;
import com.workshop.model.season.Grave;
import com.workshop.model.zombie.BossZombieRegistry;
import com.workshop.model.zombie.Zombie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ZombossSummon implements Behaviors {

    private static final double ACTION_COOLDOWN_SECONDS = 5.0;
    private static final double INTRO_DURATION_SECONDS = 1.2;
    private static final double ACTION_ANIM_SECONDS = 2.6;
    private static final double BURN_DURATION_SECONDS = 4.0;
    private static final int INSTANT_KILL_DAMAGE = 10000;
    private static final double TICK_SECONDS = 0.1;

    private static final double EGYPT_MISSILE_FLIGHT_SECONDS = 1.2;
    private static final double ICE_MISSILE_FLIGHT_SECONDS = 1.25;
    private static final double ICE_SUMMON_SECONDS = 1.15;
    private static final double DARK_FIREBALL_FLIGHT_SECONDS = 1.15;
    private static final double DARK_FIRE_BREATH_SECONDS = 1.2;

    private final Random random = new Random();
    private ZombossState currentState = ZombossState.INTRO;

    private boolean isSpawned = false;
    private long spawnTick = -1;
    private double introElapsed;
    private double actionElapsed;
    private double actionAnimRemaining;
    private boolean pendingFirstAction = true;
    private boolean announcedStun;

    private boolean isDashing = false;
    private boolean isReturning = false;
    private double dashTargetX = 0;
    private double originalX = 0;

    private final List<Plant> vortexPlants = new ArrayList<>();
    private final List<Zombie> vortexZombies = new ArrayList<>();
    private double vortexTargetX;
    private double vortexWindRemaining;
    private final List<PendingShark> pendingSharks = new ArrayList<>();
    private final List<PendingMissile> pendingMissiles = new ArrayList<>();
    private final List<PendingFrozenSpawn> pendingFrozenSpawns = new ArrayList<>();
    private final List<PendingFireball> pendingFireballs = new ArrayList<>();
    private final List<PendingBurn> pendingBurns = new ArrayList<>();

    private static final class PendingShark {
        final int row;
        final int col;
        double remaining;

        PendingShark(int row, int col, double remaining) {
            this.row = row;
            this.col = col;
            this.remaining = remaining;
        }
    }

    private static final class PendingMissile {
        final int row;
        final int col;
        double remaining;
        final ExplosionFx.Kind explosionKind;
        final boolean spawnGraves;

        PendingMissile(
            int row,
            int col,
            double remaining,
            ExplosionFx.Kind explosionKind,
            boolean spawnGraves
        ) {
            this.row = row;
            this.col = col;
            this.remaining = remaining;
            this.explosionKind = explosionKind;
            this.spawnGraves = spawnGraves;
        }
    }

    private static final class PendingFrozenSpawn {
        final int row;
        final int col;
        double remaining;

        PendingFrozenSpawn(int row, int col, double remaining) {
            this.row = row;
            this.col = col;
            this.remaining = remaining;
        }
    }

    private static final class PendingFireball {
        final int row;
        final int col;
        double remaining;

        PendingFireball(int row, int col, double remaining) {
            this.row = row;
            this.col = col;
            this.remaining = remaining;
        }
    }

    private static final class PendingBurn {
        final int topRow;
        final int bottomRow;
        double remaining;

        PendingBurn(int topRow, int bottomRow, double remaining) {
            this.topRow = topRow;
            this.bottomRow = bottomRow;
            this.remaining = remaining;
        }
    }

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (ctx.getSeason() == null) {
            return;
        }

        tickPendingMissiles(ctx);
        tickPendingFrozenSpawns(ctx);
        tickPendingFireballs(ctx);
        tickPendingBurns(ctx);

        if (zombie.isDead()) {
            return;
        }

        long currentTick = ctx.getTimeManager().getTotalTicks();

        if (!isSpawned) {
            handleSpawnIntro(zombie, ctx, currentTick);
            return;
        }

        if (isStunned(zombie)) {
            tickPendingSharks(ctx);
            tickVortex(ctx);
            this.currentState = ZombossState.STUNNED;
            this.actionAnimRemaining = 0;
            this.isDashing = false;
            this.isReturning = false;
            if (!announcedStun) {
                ctx.announce(zombie.getName() + " is stunned!");
                announcedStun = true;
            }
            actionElapsed += TICK_SECONDS;
            return;
        }
        if (currentState == ZombossState.STUNNED) {
            this.currentState = ZombossState.IDLE;
        }
        announcedStun = false;

        if (isDashing || isReturning) {
            updateDashPosition(zombie, ctx);
            return;
        }

        tickPendingSharks(ctx);
        tickVortex(ctx);

        tickActionAnim();

        if (isBeachTelegraphBusy()) {
            return;
        }

        actionElapsed += TICK_SECONDS;
        if (pendingFirstAction || actionElapsed >= ACTION_COOLDOWN_SECONDS) {
            executeRandomAbility(zombie, ctx);
            actionElapsed = 0;
            pendingFirstAction = false;
        }
    }

    private void handleSpawnIntro(Zombie boss, GameContext ctx, long currentTick) {
        if (spawnTick < 0) {
            spawnTick = currentTick;
            this.currentState = ZombossState.INTRO;
            clampBossRow(boss, ctx);
            ctx.announce(boss.getName() + " HAS ARRIVED!");
        }

        introElapsed += TICK_SECONDS;
        if (introElapsed >= INTRO_DURATION_SECONDS) {
            this.isSpawned = true;
            this.currentState = ZombossState.IDLE;
            this.pendingFirstAction = true;
            this.actionElapsed = ACTION_COOLDOWN_SECONDS;
        }
    }

    private void executeRandomAbility(Zombie boss, GameContext ctx) {
        String seasonName = ctx.getSeason().getName().toLowerCase();
        String bossId = boss.getId() == null ? "" : boss.getId().toLowerCase();

        if (seasonName.contains("egypt") || bossId.contains("egypt")) {
            executeEgyptAction(boss, ctx);
        } else if (seasonName.contains("beach") || bossId.contains("beach") || seasonName.contains("shark")) {
            executeBeachAction(boss, ctx);
        } else if (seasonName.contains("dark") || bossId.contains("dark")) {
            executeDarkAgesAction(boss, ctx);
        } else if (seasonName.contains("frozen") || seasonName.contains("ice") || bossId.contains("frozen")) {
            executeFrozenCavesAction(boss, ctx);
        } else {
            executeGenericAction(boss, ctx);
        }
    }

    private void executeEgyptAction(Zombie boss, GameContext ctx) {
        int action = pendingFirstAction ? 1 + random.nextInt(3) : random.nextInt(4);

        switch (action) {
            case 0:
                moveBossToRandomRows(boss, ctx);
                break;
            case 1:
                summonMinions(boss, ctx);
                break;
            case 2:
                fireEgyptMissile(boss, ctx);
                break;
            default:
                performEgyptDash(boss);
                break;
        }
    }

    private void executeBeachAction(Zombie boss, GameContext ctx) {
        if (pendingFirstAction) {
            executeBeachVortex(boss, ctx);
            return;
        }
        int action = random.nextInt(3);
        switch (action) {
            case 0:
                launchBeachSharks(boss, ctx);
                break;
            default:
                executeBeachVortex(boss, ctx);
                break;
        }
    }

    private void executeDarkAgesAction(Zombie boss, GameContext ctx) {
        int action = pendingFirstAction ? random.nextInt(2) : random.nextInt(3);

        switch (action) {
            case 0:
                launchDragonFireballs(boss, ctx);
                break;
            case 1:
                burnOccupiedRows(boss, ctx);
                break;
            default:
                moveBossToRandomRows(boss, ctx);
                break;
        }
    }

    private void executeFrozenCavesAction(Zombie boss, GameContext ctx) {
        int action = random.nextInt(3);

        switch (action) {
            case 0:
                fireIceMissile(boss, ctx);
                break;
            case 1:
                blastIcyWind(boss, ctx);
                break;
            default:
                freezeRandomColumn(boss, ctx);
                break;
        }
    }

    private void executeGenericAction(Zombie boss, GameContext ctx) {
        int action = random.nextInt(2);
        if (action == 0) {
            moveBossToRandomRows(boss, ctx);
        } else {
            summonMinions(boss, ctx);
        }
    }

    private void moveBossToRandomRows(Zombie boss, GameContext ctx) {
        int totalRows = ctx.getLevel().getRows();
        if (totalRows < 2) return;

        beginAction(ZombossState.WALKING);
        int newTopRow = random.nextInt(totalRows - 1);
        boss.setY(newTopRow);

        ctx.announce(boss.getName() + " moved to rows " + newTopRow + " and " + (newTopRow + 1) + "!");
    }

    private void summonMinions(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.SUMMONING_ZOMBIES);
        Map<String, Zombie> pool = ctx.getDataManager().zombies.getZombieDataMap();
        String seasonName = ctx.getSeason().getName();

        List<String> candidates = new ArrayList<>();
        for (Map.Entry<String, Zombie> entry : pool.entrySet()) {
            Zombie template = entry.getValue();
            if (template == null || BossZombieRegistry.isBossId(template.getId())) continue;
            if (template.getWavePointCost() <= 0) continue;
            if (!ctx.getDataManager().zombies.isAvailableInChapter(entry.getKey(), seasonName)) continue;
            candidates.add(entry.getKey());
        }

        if (!candidates.isEmpty()) {
            int count = 1 + random.nextInt(2);
            boolean egypt = isEgyptBoss(boss, ctx);

            for (int i = 0; i < count; i++) {
                String name = candidates.get(random.nextInt(candidates.size()));
                double x;
                int row;
                if (egypt) {
                    row = summonRowInFrontOfBoss(boss, ctx);
                    x = Math.max(0.8, boss.getX() - 1.45);
                } else {
                    row = random.nextInt(ctx.getLevel().getRows());
                    x = ctx.getLevel().getColumns();
                }
                Zombie minion = spawnMinion(ctx, name, x, row);
                if (egypt && minion != null) {
                    ctx.spawnEgyptSummon(row, x);
                }
            }
        }
    }

    private static boolean isEgyptBoss(Zombie boss, GameContext ctx) {
        String season = ctx.getSeason() == null ? "" : ctx.getSeason().getName();
        String id = boss.getId() == null ? "" : boss.getId();
        return season.toLowerCase().contains("egypt") || id.toLowerCase().contains("egypt");
    }

    private int summonRowInFrontOfBoss(Zombie boss, GameContext ctx) {
        int top = bossTopRow(boss, ctx);
        int bottom = Math.min(ctx.getLevel().getRows() - 1, top + 1);
        return random.nextBoolean() ? top : bottom;
    }

    private void fireEgyptMissile(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.FIRING_MISSILE);

        int[] cell = pickMissileTarget(ctx);
        ctx.spawnEgyptMissile(cell[0], cell[1], (float) EGYPT_MISSILE_FLIGHT_SECONDS);
        pendingMissiles.add(new PendingMissile(
            cell[0],
            cell[1],
            EGYPT_MISSILE_FLIGHT_SECONDS,
            ExplosionFx.Kind.EGYPT_MISSILE,
            true
        ));

        ctx.announce(boss.getName() + " launched a missile!");
    }

    private void launchDragonFireballs(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.LAUNCHING_FIREBALLS);

        int fireballCount = 3 + random.nextInt(2);
        Set<Long> used = new HashSet<>();
        int spawned = 0;
        int attempts = 0;
        while (spawned < fireballCount && attempts < fireballCount * 10) {
            attempts++;
            int[] cell = spawned < 2 ? pickMissileTarget(ctx) : pickRandomCell(ctx);
            long key = (((long) cell[0]) << 32) | (cell[1] & 0xffffffffL);
            if (!used.add(key)) {
                continue;
            }
            double delay = DARK_FIREBALL_FLIGHT_SECONDS + spawned * 0.12;
            ctx.spawnDarkFireball(cell[0], cell[1], (float) delay);
            pendingFireballs.add(new PendingFireball(cell[0], cell[1], delay));
            spawned++;
        }

        ctx.announce(boss.getName() + " rained fireballs!");
    }

    private void burnOccupiedRows(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.BURNING_ROWS);

        int topRow = bossTopRow(boss, ctx);
        int bottomRow = topRow + 1;
        ctx.spawnDarkFireBreath(topRow, bottomRow, (float) DARK_FIRE_BREATH_SECONDS);
        pendingBurns.add(new PendingBurn(topRow, bottomRow, DARK_FIRE_BREATH_SECONDS));

        ctx.announce(boss.getName() + " scorched both lanes!");
    }

    private void fireIceMissile(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.FIRING_ICE_MISSILE);

        int[] cell = pickMissileTarget(ctx);
        ctx.spawnIceMissile(cell[0], cell[1], (float) ICE_MISSILE_FLIGHT_SECONDS);
        pendingMissiles.add(new PendingMissile(
            cell[0],
            cell[1],
            ICE_MISSILE_FLIGHT_SECONDS,
            ExplosionFx.Kind.ICEAGE_MISSILE,
            false
        ));

        ctx.announce(boss.getName() + " launched an ice missile!");
    }

    private void blastIcyWind(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.ICY_WIND);

        int rows = ctx.getLevel().getRows();
        int first = random.nextInt(rows);
        int second = random.nextInt(rows);
        if (rows > 1 && second == first) {
            second = (first + 1) % rows;
        }

        applyIcyWind(ctx, first);
        if (second != first) {
            applyIcyWind(ctx, second);
        }

        ctx.announce(boss.getName() + " blasted icy wind!");
    }

    private void applyIcyWind(GameContext ctx, int row) {
        if (ctx.getSeason() instanceof FrozenCaveChapter frozen) {
            frozen.applyIcyWindToRow(ctx, row);
            return;
        }

        ctx.announceWindRow(row);
        Plant[][] grid = ctx.getPlantGrid();
        if (row < 0 || row >= grid.length) {
            return;
        }
        for (int col = 0; col < ctx.getLevel().getColumns(); col++) {
            Plant plant = grid[row][col];
            if (plant != null && !plant.hasTheTag(com.workshop.model.plants.Tag.FIRE)) {
                plant.increaseFreezeLevel();
            }
        }
    }

    private void freezeRandomColumn(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.FREEZING_COLUMN);

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        int column = random.nextInt(cols);
        Plant[][] grid = ctx.getPlantGrid();

        for (int row = 0; row < rows; row++) {
            Plant plant = grid[row][column];
            if (plant != null && !plant.isDead()) {
                while (plant.getFreezeLevel() < 3) {
                    plant.increaseFreezeLevel();
                }
                continue;
            }

            ctx.spawnIceSummon(row, column);
            pendingFrozenSpawns.add(new PendingFrozenSpawn(row, column, ICE_SUMMON_SECONDS));
        }

        ctx.announce(boss.getName() + " froze column " + column + "!");
    }

    private void launchBeachSharks(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.LAUNCHING_SHARKS);
        pendingSharks.clear();

        List<int[]> targets = pickSharkTargets(boss, ctx);
        for (int i = 0; i < targets.size(); i++) {
            int[] cell = targets.get(i);
            ctx.spawnBeachShark(cell[0], cell[1]);
            pendingSharks.add(new PendingShark(cell[0], cell[1], 1.25 + i * 0.14));
        }

        ctx.announce(boss.getName() + " launched sharks from underwater!");
    }

    private List<int[]> pickSharkTargets(Zombie boss, GameContext ctx) {
        Plant[][] grid = ctx.getPlantGrid();
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        int maxCol = Math.max(1, Math.min(cols - 1, (int) Math.floor(boss.getX()) - 1));

        List<int[]> waterPlants = new ArrayList<>();
        List<int[]> landPlants = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c <= maxCol; c++) {
                Plant plant = grid[r][c];
                if (plant == null || plant.isDead() || plant.isBeingPulled()) {
                    continue;
                }
                int[] cell = new int[]{r, c};
                if (ctx.getSeason().isWaterCell(r, c, ctx)) {
                    waterPlants.add(cell);
                } else {
                    landPlants.add(cell);
                }
            }
        }

        List<int[]> chosen = new ArrayList<>();
        java.util.Collections.shuffle(waterPlants, random);
        java.util.Collections.shuffle(landPlants, random);
        chosen.addAll(waterPlants);
        chosen.addAll(landPlants);
        if (chosen.size() >= 3) {
            return chosen.subList(0, 3);
        }

        int top = bossTopRow(boss, ctx);
        int bottom = Math.min(rows - 1, top + 1);
        List<int[]> empty = new ArrayList<>();
        for (int r = top; r <= bottom; r++) {
            for (int c = 1; c <= maxCol; c++) {
                empty.add(new int[]{r, c});
            }
        }
        java.util.Collections.shuffle(empty, random);
        for (int[] cell : empty) {
            boolean used = false;
            for (int[] existing : chosen) {
                if (existing[0] == cell[0] && existing[1] == cell[1]) {
                    used = true;
                    break;
                }
            }
            if (!used) {
                chosen.add(cell);
            }
            if (chosen.size() >= 3) {
                break;
            }
        }
        if (chosen.isEmpty()) {
            chosen.add(new int[]{top, Math.max(1, maxCol / 2)});
        }
        return chosen;
    }

    private void tickPendingSharks(GameContext ctx) {
        if (pendingSharks.isEmpty()) {
            return;
        }
        Iterator<PendingShark> it = pendingSharks.iterator();
        while (it.hasNext()) {
            PendingShark shark = it.next();
            shark.remaining -= TICK_SECONDS;
            if (shark.remaining > 0) {
                continue;
            }
            destroyPlantAt(ctx, shark.row, shark.col);
            it.remove();
        }
    }

    private void tickPendingMissiles(GameContext ctx) {
        if (pendingMissiles.isEmpty()) {
            return;
        }
        Iterator<PendingMissile> it = pendingMissiles.iterator();
        while (it.hasNext()) {
            PendingMissile missile = it.next();
            missile.remaining -= TICK_SECONDS;
            if (missile.remaining > 0) {
                continue;
            }
            destroyPlantAt(ctx, missile.row, missile.col);
            ctx.spawnExplosion(missile.row, missile.col, missile.explosionKind);
            if (missile.spawnGraves) {
                spawnRandomGraves(ctx, 2, missile.row, missile.col);
            }
            it.remove();
        }
    }

    private void tickPendingFrozenSpawns(GameContext ctx) {
        if (pendingFrozenSpawns.isEmpty()) {
            return;
        }
        Iterator<PendingFrozenSpawn> it = pendingFrozenSpawns.iterator();
        while (it.hasNext()) {
            PendingFrozenSpawn spawn = it.next();
            spawn.remaining -= TICK_SECONDS;
            if (spawn.remaining > 0) {
                continue;
            }
            Plant plant = plantAt(ctx, spawn.row, spawn.col);
            if (plant == null || plant.isDead()) {
                Zombie frozen = spawnMinion(ctx, "Default", spawn.col, spawn.row);
                if (frozen != null) {
                    frozen.setAsInitialFrozenBlock();
                }
            }
            it.remove();
        }
    }

    private void tickPendingFireballs(GameContext ctx) {
        if (pendingFireballs.isEmpty()) {
            return;
        }
        Iterator<PendingFireball> it = pendingFireballs.iterator();
        while (it.hasNext()) {
            PendingFireball fireball = it.next();
            fireball.remaining -= TICK_SECONDS;
            if (fireball.remaining > 0) {
                continue;
            }
            ctx.igniteCell(fireball.row, fireball.col, BURN_DURATION_SECONDS);
            ctx.spawnExplosion(fireball.row, fireball.col, ExplosionFx.Kind.DARK_FIREBALL);
            spawnMinion(ctx, "Imp Dragon", fireball.col, fireball.row);
            it.remove();
        }
    }

    private void tickPendingBurns(GameContext ctx) {
        if (pendingBurns.isEmpty()) {
            return;
        }
        Iterator<PendingBurn> it = pendingBurns.iterator();
        while (it.hasNext()) {
            PendingBurn burn = it.next();
            burn.remaining -= TICK_SECONDS;
            if (burn.remaining > 0) {
                continue;
            }
            int cols = ctx.getLevel().getColumns();
            for (int row = burn.topRow; row <= burn.bottomRow; row++) {
                for (int col = 0; col < cols; col++) {
                    ctx.igniteCell(row, col, BURN_DURATION_SECONDS);
                }
                ctx.spawnExplosion(row, 0, ExplosionFx.Kind.JALAPENO);
            }
            it.remove();
        }
    }

    private Plant plantAt(GameContext ctx, int row, int col) {
        Plant[][] grid = ctx.getPlantGrid();
        if (row < 0 || col < 0 || row >= grid.length || col >= grid[row].length) {
            return null;
        }
        return grid[row][col];
    }

    private void executeBeachVortex(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.USING_VORTEX);
        vortexPlants.clear();
        vortexZombies.clear();
        vortexTargetX = boss.getX();
        vortexWindRemaining = 3.4;

        int topRow = bossTopRow(boss, ctx);
        int bottomRow = topRow + 1;
        ctx.setBeachVortexRows(topRow, bottomRow);
        int mouthCol = Math.max(0, (int) Math.floor(boss.getX()) - 2);
        ctx.spawnExplosion(topRow, mouthCol, ExplosionFx.Kind.TURBINE_WIND);
        ctx.spawnExplosion(bottomRow, mouthCol, ExplosionFx.Kind.TURBINE_WIND);

        pullAllPlantsInRow(ctx, topRow);
        pullAllPlantsInRow(ctx, bottomRow);
        pullZombiesInRows(boss, ctx, topRow, bottomRow);

        ctx.announce(boss.getName() + " activated the turbine, pulling plants and zombies!");
    }

    private void pullAllPlantsInRow(GameContext ctx, int row) {
        Plant[][] grid = ctx.getPlantGrid();
        if (row < 0 || row >= grid.length) {
            return;
        }
        for (int c = 0; c < grid[row].length; c++) {
            Plant plant = grid[row][c];
            if (plant == null || plant.isDead() || plant.isBeingPulled()) {
                continue;
            }
            grid[row][c] = null;
            plant.setVisualPosition(c, row);
            ctx.addPulledPlant(plant);
            vortexPlants.add(plant);
        }
    }

    private void pullZombiesInRows(Zombie boss, GameContext ctx, int topRow, int bottomRow) {
        for (Zombie zombie : new ArrayList<>(ctx.getAliveZombies())) {
            if (zombie == null || zombie == boss || zombie.isBoss() || zombie.isDead()) {
                continue;
            }
            if (!zombie.occupiesRow(topRow) && !zombie.occupiesRow(bottomRow)) {
                continue;
            }
            zombie.setBeingSucked(true);
            vortexZombies.add(zombie);
        }
    }

    private void tickVortex(GameContext ctx) {
        if (vortexWindRemaining > 0) {
            vortexWindRemaining -= TICK_SECONDS;
        }
        if (vortexPlants.isEmpty() && vortexZombies.isEmpty()) {
            if (vortexWindRemaining <= 0 && ctx.hasBeachVortex()) {
                ctx.clearBeachVortex();
            }
            return;
        }
        double pullSpeed = 0.07;
        Iterator<Plant> plants = vortexPlants.iterator();
        while (plants.hasNext()) {
            Plant plant = plants.next();
            if (plant == null || plant.isDead()) {
                ctx.removePulledPlant(plant);
                plants.remove();
                continue;
            }
            double x = plant.getVisualX() == null ? plant.getCol() : plant.getVisualX();
            double y = plant.getVisualY() == null ? plant.getRow() : plant.getVisualY();
            x += pullSpeed;
            plant.setVisualPosition(x, y);
            if (x >= vortexTargetX - 0.55) {
                ctx.spawnExplosion(
                    plant.getRow(),
                    Math.max(0, (int) Math.round(Math.min(x, ctx.getLevel().getColumns() - 1))),
                    ExplosionFx.Kind.PLANT_PULLED
                );
                plant.takeDamage(INSTANT_KILL_DAMAGE);
                ctx.removePulledPlant(plant);
                plants.remove();
            }
        }

        Iterator<Zombie> zombies = vortexZombies.iterator();
        while (zombies.hasNext()) {
            Zombie zombie = zombies.next();
            if (zombie == null || zombie.isDead()) {
                if (zombie != null) {
                    zombie.setBeingSucked(false);
                }
                zombies.remove();
                continue;
            }
            zombie.setX(zombie.getX() + pullSpeed);
            if (zombie.getX() >= vortexTargetX - 0.55) {
                zombie.setBeingSucked(false);
                zombie.takeDamage(INSTANT_KILL_DAMAGE);
                zombies.remove();
            }
        }

        if (vortexPlants.isEmpty() && vortexZombies.isEmpty() && vortexWindRemaining <= 0) {
            ctx.clearBeachVortex();
        }
    }

    private void spawnRandomGraves(GameContext ctx, int count, int avoidRow, int avoidCol) {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Grave[][] graveGrid = ctx.getGraveGrid();
        Plant[][] plantGrid = ctx.getPlantGrid();

        for (int i = 0; i < count; i++) {
            List<int[]> emptyCells = new ArrayList<>();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (r == avoidRow && c == avoidCol) {
                        continue;
                    }
                    if (graveGrid[r][c] != null) {
                        continue;
                    }
                    Plant plant = plantGrid[r][c];
                    if (plant != null && !plant.isDead()) {
                        continue;
                    }
                    emptyCells.add(new int[]{r, c});
                }
            }
            if (emptyCells.isEmpty()) {
                break;
            }

            int[] chosen = emptyCells.get(random.nextInt(emptyCells.size()));
            ctx.placeGrave(new Grave(Grave.GraveType.NORMAL, chosen[0], chosen[1]), chosen[0], chosen[1]);
        }
    }

    private void performEgyptDash(Zombie boss) {
        this.currentState = ZombossState.DASHING;
        this.isDashing = true;
        this.isReturning = false;
        this.originalX = boss.getX();
        this.dashTargetX = 0;
    }

    private void updateDashPosition(Zombie boss, GameContext ctx) {
        if (isDashing) {
            double dashSpeed = 0.5;
            if (boss.getX() > dashTargetX) {
                boss.setX(boss.getX() - dashSpeed);

                int topRow = bossTopRow(boss, ctx);
                int bottomRow = topRow + 1;
                int currentCol = (int) Math.round(boss.getX());

                if (currentCol >= 0 && currentCol < ctx.getLevel().getColumns()) {
                    for (int r = topRow; r <= bottomRow; r++) {
                        destroyPlantAt(ctx, r, currentCol);
                    }
                }
            } else {
                isDashing = false;
                isReturning = true;
                this.currentState = ZombossState.WALKING;
            }
        } else if (isReturning) {
            double returnSpeed = 0.2;
            if (boss.getX() < originalX) {
                boss.setX(Math.min(boss.getX() + returnSpeed, originalX));
            } else {
                isReturning = false;
                this.currentState = ZombossState.IDLE;
            }
        }
    }

    private void tickActionAnim() {
        if (currentState == ZombossState.IDLE
            || currentState == ZombossState.INTRO
            || currentState == ZombossState.STUNNED
            || currentState == ZombossState.DASHING) {
            return;
        }
        if (isBeachTelegraphBusy()) {
            return;
        }
        actionAnimRemaining -= TICK_SECONDS;
        if (actionAnimRemaining <= 0) {
            this.currentState = ZombossState.IDLE;
        }
    }

    private boolean isBeachTelegraphBusy() {
        return !vortexPlants.isEmpty()
            || !vortexZombies.isEmpty()
            || !pendingSharks.isEmpty()
            || vortexWindRemaining > 0;
    }

    private void beginAction(ZombossState state) {
        this.currentState = state;
        this.actionAnimRemaining = ACTION_ANIM_SECONDS;
    }

    public ZombossState getCurrentState() {
        return currentState;
    }

    public boolean isPhysicallyMoving() {
        return isDashing || isReturning;
    }

    private boolean isStunned(Zombie boss) {
        return boss.isStunned();
    }

    private int bossTopRow(Zombie boss, GameContext ctx) {
        int totalRows = ctx.getLevel().getRows();
        int top = (int) boss.getY();
        return Math.max(0, Math.min(top, Math.max(0, totalRows - 2)));
    }

    private void clampBossRow(Zombie boss, GameContext ctx) {
        boss.setY(bossTopRow(boss, ctx));
    }

    private int[] pickRandomCell(GameContext ctx) {
        return new int[]{
            random.nextInt(ctx.getLevel().getRows()),
            random.nextInt(ctx.getLevel().getColumns())
        };
    }

    private int[] pickMissileTarget(GameContext ctx) {
        Plant[][] grid = ctx.getPlantGrid();
        List<int[]> planted = new ArrayList<>();
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                Plant plant = grid[r][c];
                if (plant != null && !plant.isDead()) {
                    planted.add(new int[]{r, c});
                }
            }
        }
        if (!planted.isEmpty()) {
            return planted.get(random.nextInt(planted.size()));
        }
        return pickRandomCell(ctx);
    }

    private void destroyPlantAt(GameContext ctx, int row, int col) {
        Plant[][] grid = ctx.getPlantGrid();
        if (row < 0 || col < 0 || row >= grid.length || col >= grid[row].length) {
            return;
        }
        Plant plant = grid[row][col];
        if (plant != null && !plant.isDead()) {
            plant.takeDamage(INSTANT_KILL_DAMAGE);
        }
    }

    private Zombie spawnMinion(GameContext ctx, String preferredName, double x, double y) {
        ZombieFactory factory = new ZombieFactory(ctx.getDataManager());
        String[] fallbacks = {preferredName, "Imp", "Default"};
        for (String name : fallbacks) {
            if (name == null) {
                continue;
            }
            try {
                Zombie minion = factory.create(name);
                if (BossZombieRegistry.isBossId(minion.getId())) {
                    continue;
                }
                minion.setX(x);
                minion.setY(y);
                ctx.addZombie(minion);
                return minion;
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }
}
