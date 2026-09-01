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

    private final Random random = new Random();
    private long lastActionTick = -1;
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

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (ctx.getSeason() == null || zombie.isDead()) return;

        long currentTick = ctx.getTimeManager().getTotalTicks();

        if (!isSpawned) {
            handleSpawnIntro(zombie, ctx, currentTick);
            return;
        }

        if (isStunned(zombie)) {
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

        tickActionAnim();

        actionElapsed += TICK_SECONDS;
        if (pendingFirstAction || actionElapsed >= ACTION_COOLDOWN_SECONDS) {
            executeRandomAbility(zombie, ctx);
            actionElapsed = 0;
            pendingFirstAction = false;
            lastActionTick = currentTick;
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
            this.lastActionTick = currentTick;
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
                performEgyptDash(boss, ctx);
                break;
        }
    }

    private void executeBeachAction(Zombie boss, GameContext ctx) {
        int action = random.nextInt(4);

        switch (action) {
            case 0:
                moveBossToRandomRows(boss, ctx);
                break;
            case 1:
                summonMinions(boss, ctx);
                break;
            case 2:
                launchBeachSharks(boss, ctx);
                break;
            case 3:
                executeBeachVortex(boss, ctx);
                break;
        }
    }

    private void executeDarkAgesAction(Zombie boss, GameContext ctx) {
        int action = random.nextInt(4);

        switch (action) {
            case 0:
                moveBossToRandomRows(boss, ctx);
                break;
            case 1:
                summonMinions(boss, ctx);
                break;
            case 2:
                launchDragonFireballs(boss, ctx);
                break;
            default:
                burnOccupiedRows(boss, ctx);
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

            for (int i = 0; i < count; i++) {
                String name = candidates.get(random.nextInt(candidates.size()));
                spawnMinion(ctx, name, ctx.getLevel().getColumns(), random.nextInt(ctx.getLevel().getRows()));
            }
        }
    }

    private void fireEgyptMissile(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.FIRING_MISSILE);

        int[] cell = pickRandomCell(ctx);
        destroyPlantAt(ctx, cell[0], cell[1]);
        ctx.spawnExplosion(cell[0], cell[1], ExplosionFx.Kind.GENERIC);
        spawnRandomGraves(ctx, 2);

        ctx.announce(boss.getName() + " launched a missile!");
    }

    private void launchDragonFireballs(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.LAUNCHING_FIREBALLS);

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        int fireballCount = 3 + random.nextInt(3);
        int maxCells = Math.max(1, rows * cols);
        fireballCount = Math.min(fireballCount, maxCells);

        Set<Long> used = new HashSet<>();
        int spawned = 0;
        int attempts = 0;
        while (spawned < fireballCount && attempts < fireballCount * 8) {
            attempts++;
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            long key = (((long) row) << 32) | (col & 0xffffffffL);
            if (!used.add(key)) {
                continue;
            }

            ctx.igniteCell(row, col, BURN_DURATION_SECONDS);
            ctx.spawnExplosion(row, col, ExplosionFx.Kind.GENERIC);
            spawnMinion(ctx, "Imp Dragon", col, row);
            spawned++;
        }

        ctx.announce(boss.getName() + " rained fireballs!");
    }

    private void burnOccupiedRows(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.BURNING_ROWS);

        int topRow = bossTopRow(boss, ctx);
        int bottomRow = topRow + 1;
        int cols = ctx.getLevel().getColumns();

        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = 0; col < cols; col++) {
                ctx.igniteCell(row, col, BURN_DURATION_SECONDS);
            }
            ctx.spawnExplosion(row, 0, ExplosionFx.Kind.JALAPENO);
        }

        ctx.announce(boss.getName() + " scorched both lanes!");
    }

    private void fireIceMissile(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.FIRING_ICE_MISSILE);

        int[] cell = pickRandomCell(ctx);
        destroyPlantAt(ctx, cell[0], cell[1]);
        ctx.spawnExplosion(cell[0], cell[1], ExplosionFx.Kind.ICEAGE_MISSILE);

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

            Zombie frozen = spawnMinion(ctx, "Default", column, row);
            if (frozen != null) {
                frozen.setAsInitialFrozenBlock();
            }
        }

        ctx.announce(boss.getName() + " froze column " + column + "!");
    }

    private void launchBeachSharks(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.LAUNCHING_SHARKS);

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        int sharkCount = 3;

        for (int i = 0; i < sharkCount; i++) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            if (ctx.getSeason().isWaterCell(r, c, ctx)) {
                destroyPlantAt(ctx, r, c);
            }
        }

        ctx.announce(boss.getName() + " launched sharks from underwater!");
    }

    private void executeBeachVortex(Zombie boss, GameContext ctx) {
        beginAction(ZombossState.USING_VORTEX);

        int topRow = bossTopRow(boss, ctx);
        int bottomRow = topRow + 1;
        int cols = ctx.getLevel().getColumns();

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = 0; c < cols; c++) {
                destroyPlantAt(ctx, r, c);
            }
        }

        List<Zombie> zombiesToRemove = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if (z == boss) continue;
            if (z.occupiesRow(topRow) || z.occupiesRow(bottomRow)) {
                zombiesToRemove.add(z);
            }
        }

        for (Zombie z : zombiesToRemove) {
            z.takeDamage(INSTANT_KILL_DAMAGE);
        }

        ctx.announce(boss.getName() + " activated the turbine, pulling plants and zombies!");
    }

    private void spawnRandomGraves(GameContext ctx, int count) {
        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        Grave[][] graveGrid = ctx.getGraveGrid();

        for (int i = 0; i < count; i++) {
            List<int[]> emptyCells = new ArrayList<>();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (graveGrid[r][c] == null) {
                        emptyCells.add(new int[]{r, c});
                    }
                }
            }
            if (emptyCells.isEmpty()) break;

            int[] chosen = emptyCells.get(random.nextInt(emptyCells.size()));
            graveGrid[chosen[0]][chosen[1]] = new Grave(Grave.GraveType.NORMAL, chosen[0], chosen[1]);
        }
    }

    private void performEgyptDash(Zombie boss, GameContext ctx) {
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
        actionAnimRemaining -= TICK_SECONDS;
        if (actionAnimRemaining <= 0) {
            this.currentState = ZombossState.IDLE;
        }
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
