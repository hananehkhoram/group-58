package com.workshop.model.zombie.behavior;

import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.season.Grave;
import com.workshop.model.zombie.BossZombieRegistry;
import com.workshop.model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ZombossSummon implements Behaviors {

    private static final int TICKS_PER_SECOND = 10;
    private static final double ACTION_COOLDOWN_SECONDS = 14.0;
    private static final double INTRO_DURATION_SECONDS = 3.0;
    private static final int INSTANT_KILL_DAMAGE = 10000;

    private final Random random = new Random();
    private long lastActionTick = -1;
    private ZombossState currentState = ZombossState.INTRO;

    private boolean isSpawned = false;
    private long spawnTick = -1;

    private boolean isDashing = false;
    private boolean isReturning = false;
    private double dashTargetX = 0;
    private double originalX = 0;

    @Override
    public void onTick(Zombie zombie, GameContext ctx) {
        if (ctx.getSeason() == null || zombie.isDead()) return;

        long currentTick = ctx.getTimeManager().getTotalTicks();

        // ۱. مدیریت مرحله اسپاون و انیمیشن اینترو (INTRO)
        if (!isSpawned) {
            handleSpawnIntro(zombie, ctx, currentTick);
            return;
        }

        if (isStunned(zombie)) {
            return;
        }

        // ۲. مدیریت حرکت Dash
        if (isDashing || isReturning) {
            updateDashPosition(zombie, ctx);
            return;
        }

        // ۳. بررسی Cooldown برای اجرای قابلیت‌ها
        if (currentTick - lastActionTick >= (long) (ACTION_COOLDOWN_SECONDS * TICKS_PER_SECOND)) {
            executeRandomAbility(zombie, ctx);
            lastActionTick = currentTick;
        }
    }

    private void handleSpawnIntro(Zombie boss, GameContext ctx, long currentTick) {
        if (spawnTick < 0) {
            spawnTick = currentTick;
            this.currentState = ZombossState.INTRO;
            ctx.announce(boss.getName() + " HAS ARRIVED!");
        }

        long elapsedTicks = currentTick - spawnTick;
        long introDurationTicks = (long) (INTRO_DURATION_SECONDS * TICKS_PER_SECOND);

        if (elapsedTicks >= introDurationTicks) {
            this.isSpawned = true;
            this.currentState = ZombossState.IDLE;
            this.lastActionTick = currentTick;
        }
    }

    private void executeRandomAbility(Zombie boss, GameContext ctx) {
        String seasonName = ctx.getSeason().getName().toLowerCase();
        String bossId = boss.getId().toLowerCase();

        if (seasonName.contains("egypt") || bossId.contains("egypt")) {
            executeEgyptAction(boss, ctx);
        } else if (seasonName.contains("beach") || bossId.contains("beach") || seasonName.contains("shark")) {
            executeBeachAction(boss, ctx);
        } else {
            executeGenericAction(boss, ctx);
        }
    }

    private void executeEgyptAction(Zombie boss, GameContext ctx) {
        int action = random.nextInt(4);

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
            case 3:
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

        this.currentState = ZombossState.WALKING;
        int newTopRow = random.nextInt(totalRows - 1);
        boss.setY(newTopRow);

        ctx.announce(boss.getName() + " moved to rows " + newTopRow + " and " + (newTopRow + 1) + "!");
        this.currentState = ZombossState.IDLE;
    }

    private void summonMinions(Zombie boss, GameContext ctx) {
        this.currentState = ZombossState.SUMMONING_ZOMBIES;
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
            ZombieFactory factory = new ZombieFactory(ctx.getDataManager());
            int count = 1 + random.nextInt(2);

            for (int i = 0; i < count; i++) {
                String name = candidates.get(random.nextInt(candidates.size()));
                Zombie minion = factory.create(name);
                minion.setX(ctx.getLevel().getColumns());
                minion.setY(random.nextInt(ctx.getLevel().getRows()));
                ctx.addZombie(minion);
            }
        }

        this.currentState = ZombossState.IDLE;
    }

    private void fireEgyptMissile(Zombie boss, GameContext ctx) {
        this.currentState = ZombossState.FIRING_MISSILE;

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();

        int targetRow = random.nextInt(rows);
        int targetCol = random.nextInt(cols);

        Plant targetPlant = ctx.getPlantGrid()[targetRow][targetCol];

        Projectile missile = new Projectile(
            INSTANT_KILL_DAMAGE,
            targetCol,
            -2.0,
            targetRow,
            4.0,
            BulletType.MISSILE,
            TrajectoryType.HOMING,
            true,
            0.0,
            1.0,
            null
        );

        if (targetPlant != null && !targetPlant.isDead()) {
            missile.setHomingTarget(targetPlant);
        }

        ctx.getProjectiles().add(missile);
        spawnRandomGraves(ctx, 2);

        this.currentState = ZombossState.IDLE;
    }

    private void launchBeachSharks(Zombie boss, GameContext ctx) {
        this.currentState = ZombossState.LAUNCHING_SHARKS;

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();
        int sharkCount = 3;

        for (int i = 0; i < sharkCount; i++) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            if (ctx.getSeason().isWaterCell(r, c, ctx)) {
                Plant plantOnWater = ctx.getPlantGrid()[r][c];
                if (plantOnWater != null && !plantOnWater.isDead()) {
                    plantOnWater.takeDamage(INSTANT_KILL_DAMAGE);
                }
            }
        }

        ctx.announce(boss.getName() + " launched sharks from underwater!");
        this.currentState = ZombossState.IDLE;
    }

    private void executeBeachVortex(Zombie boss, GameContext ctx) {
        this.currentState = ZombossState.USING_VORTEX;

        int topRow = (int) boss.getY();
        int totalRows = ctx.getLevel().getRows();
        topRow = Math.max(0, Math.min(topRow, totalRows - 2));
        int bottomRow = topRow + 1;
        int cols = ctx.getLevel().getColumns();

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = 0; c < cols; c++) {
                Plant plant = ctx.getPlantGrid()[r][c];
                if (plant != null && !plant.isDead()) {
                    plant.takeDamage(INSTANT_KILL_DAMAGE);
                }
            }
        }

        List<Zombie> zombiesToRemove = new ArrayList<>();
        for (Zombie z : ctx.getAliveZombies()) {
            if (z == boss) continue;
            int zRow = (int) z.getY();
            if (zRow == topRow || zRow == bottomRow) {
                zombiesToRemove.add(z);
            }
        }

        for (Zombie z : zombiesToRemove) {
            z.takeDamage(INSTANT_KILL_DAMAGE);
        }

        ctx.announce(boss.getName() + " activated the turbine, pulling plants and zombies!");
        this.currentState = ZombossState.IDLE;
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

                int topRow = (int) boss.getY();
                int totalRows = ctx.getLevel().getRows();
                topRow = Math.max(0, Math.min(topRow, totalRows - 2));
                int bottomRow = topRow + 1;
                int currentCol = (int) Math.round(boss.getX());

                if (currentCol >= 0 && currentCol < ctx.getLevel().getColumns()) {
                    for (int r = topRow; r <= bottomRow; r++) {
                        Plant plant = ctx.getPlantGrid()[r][currentCol];
                        if (plant != null && !plant.isDead()) {
                            plant.takeDamage(INSTANT_KILL_DAMAGE);
                        }
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

    public ZombossState getCurrentState() {
        return currentState;
    }

    private boolean isStunned(Zombie boss) {
        return false;
    }
}
