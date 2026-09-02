package com.workshop.model;

import com.workshop.model.mechanisms.ExplosionFx;
import com.workshop.model.mechanisms.ProjectileHitFx;
import com.workshop.model.mechanisms.ScreenShake;
import com.workshop.model.mechanisms.ZombiePartFx;
import com.workshop.model.plants.Plant;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.ArmorType;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Holds pending visual/audio event queues that UI layers poll each frame.
 * Extracted from {@link GameContext} without changing enqueue/poll behavior.
 */
public final class PendingFxQueue {

    public static final class BeachSharkSpawn {
        public final int row;
        public final int col;

        public BeachSharkSpawn(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public static final class EgyptMissileSpawn {
        public final int row;
        public final int col;
        public final float flightSeconds;

        public EgyptMissileSpawn(int row, int col, float flightSeconds) {
            this.row = row;
            this.col = col;
            this.flightSeconds = flightSeconds;
        }
    }

    public static final class EgyptSummonSpawn {
        public final int row;
        public final double x;

        public EgyptSummonSpawn(int row, double x) {
            this.row = row;
            this.x = x;
        }
    }

    public static final class IceMissileSpawn {
        public final int row;
        public final int col;
        public final float flightSeconds;

        public IceMissileSpawn(int row, int col, float flightSeconds) {
            this.row = row;
            this.col = col;
            this.flightSeconds = flightSeconds;
        }
    }

    public static final class IceSummonSpawn {
        public final int row;
        public final int col;

        public IceSummonSpawn(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public static final class DarkFireballSpawn {
        public final int row;
        public final int col;
        public final float flightSeconds;

        public DarkFireballSpawn(int row, int col, float flightSeconds) {
            this.row = row;
            this.col = col;
            this.flightSeconds = flightSeconds;
        }
    }

    public static final class DarkFireBreathSpawn {
        public final int topRow;
        public final int bottomRow;
        public final float durationSeconds;

        public DarkFireBreathSpawn(int topRow, int bottomRow, float durationSeconds) {
            this.topRow = topRow;
            this.bottomRow = bottomRow;
            this.durationSeconds = durationSeconds;
        }
    }

    private final Deque<String> pendingAnnouncements = new ArrayDeque<>();
    private final Deque<Integer> pendingWindRows = new ArrayDeque<>();
    private final Deque<Plant> pendingPlantAttackAnimations = new ArrayDeque<>();
    private final Deque<String> pendingSoundCues = new ArrayDeque<>();
    private final Deque<ExplosionFx> pendingExplosions = new ArrayDeque<>();
    private final Deque<ScreenShake> pendingShakes = new ArrayDeque<>();
    private final Deque<BeachSharkSpawn> pendingBeachSharks = new ArrayDeque<>();
    private final Deque<EgyptMissileSpawn> pendingEgyptMissiles = new ArrayDeque<>();
    private final Deque<EgyptSummonSpawn> pendingEgyptSummons = new ArrayDeque<>();
    private final Deque<IceMissileSpawn> pendingIceMissiles = new ArrayDeque<>();
    private final Deque<IceSummonSpawn> pendingIceSummons = new ArrayDeque<>();
    private final Deque<DarkFireballSpawn> pendingDarkFireballs = new ArrayDeque<>();
    private final Deque<DarkFireBreathSpawn> pendingDarkFireBreaths = new ArrayDeque<>();
    private final Deque<ProjectileHitFx> pendingProjectileHits = new ArrayDeque<>();
    private final Deque<ZombiePartFx> pendingZombieParts = new ArrayDeque<>();

    private int beachVortexTopRow = -1;
    private int beachVortexBottomRow = -1;

    public void announce(String message) {
        if (message != null && !message.isBlank()) {
            pendingAnnouncements.addLast(message);
        }
    }

    public String pollAnnouncement() {
        return pendingAnnouncements.pollFirst();
    }

    public void queuePlantAttackAnimation(Plant plant) {
        if (plant != null) {
            pendingPlantAttackAnimations.addLast(plant);
        }
    }

    public Plant pollPlantAttackAnimation() {
        return pendingPlantAttackAnimations.pollFirst();
    }

    public void announceWindRow(int row) {
        pendingWindRows.addLast(row);
    }

    public Integer pollWindRow() {
        return pendingWindRows.pollFirst();
    }

    public void playSound(String soundKey) {
        if (soundKey != null && !soundKey.isBlank()) {
            pendingSoundCues.addLast(soundKey);
        }
    }

    public String pollSoundCue() {
        return pendingSoundCues.pollFirst();
    }

    public void spawnExplosion(int row, int col, ExplosionFx.Kind kind) {
        if (kind == null) {
            return;
        }
        pendingExplosions.addLast(new ExplosionFx(row, col, kind));
        shakeScreen(kind.shakeIntensity, kind.shakeDuration);
    }

    public ExplosionFx pollExplosion() {
        return pendingExplosions.pollFirst();
    }

    public void spawnBeachShark(int row, int col) {
        pendingBeachSharks.addLast(new BeachSharkSpawn(row, col));
        shakeScreen(8f, 0.22f);
    }

    public BeachSharkSpawn pollBeachShark() {
        return pendingBeachSharks.pollFirst();
    }

    public void spawnEgyptMissile(int row, int col, float flightSeconds) {
        pendingEgyptMissiles.addLast(new EgyptMissileSpawn(row, col, flightSeconds));
    }

    public EgyptMissileSpawn pollEgyptMissile() {
        return pendingEgyptMissiles.pollFirst();
    }

    public void spawnEgyptSummon(int row, double x) {
        pendingEgyptSummons.addLast(new EgyptSummonSpawn(row, x));
    }

    public EgyptSummonSpawn pollEgyptSummon() {
        return pendingEgyptSummons.pollFirst();
    }

    public void spawnIceMissile(int row, int col, float flightSeconds) {
        pendingIceMissiles.addLast(new IceMissileSpawn(row, col, flightSeconds));
    }

    public IceMissileSpawn pollIceMissile() {
        return pendingIceMissiles.pollFirst();
    }

    public void spawnIceSummon(int row, int col) {
        pendingIceSummons.addLast(new IceSummonSpawn(row, col));
    }

    public IceSummonSpawn pollIceSummon() {
        return pendingIceSummons.pollFirst();
    }

    public void spawnDarkFireball(int row, int col, float flightSeconds) {
        pendingDarkFireballs.addLast(new DarkFireballSpawn(row, col, flightSeconds));
    }

    public DarkFireballSpawn pollDarkFireball() {
        return pendingDarkFireballs.pollFirst();
    }

    public void spawnDarkFireBreath(int topRow, int bottomRow, float durationSeconds) {
        pendingDarkFireBreaths.addLast(new DarkFireBreathSpawn(topRow, bottomRow, durationSeconds));
    }

    public DarkFireBreathSpawn pollDarkFireBreath() {
        return pendingDarkFireBreaths.pollFirst();
    }

    public void setBeachVortexRows(int topRow, int bottomRow) {
        this.beachVortexTopRow = topRow;
        this.beachVortexBottomRow = bottomRow;
    }

    public void clearBeachVortex() {
        this.beachVortexTopRow = -1;
        this.beachVortexBottomRow = -1;
    }

    public boolean hasBeachVortex() {
        return beachVortexTopRow >= 0 && beachVortexBottomRow >= beachVortexTopRow;
    }

    public int getBeachVortexTopRow() {
        return beachVortexTopRow;
    }

    public int getBeachVortexBottomRow() {
        return beachVortexBottomRow;
    }

    public void spawnProjectileHit(int row, double x, double y) {
        pendingProjectileHits.addLast(new ProjectileHitFx(row, x, y));
    }

    public ProjectileHitFx pollProjectileHit() {
        return pendingProjectileHits.pollFirst();
    }

    public void shakeScreen(float intensity, float duration) {
        pendingShakes.addLast(new ScreenShake(intensity, duration));
    }

    public ScreenShake pollScreenShake() {
        return pendingShakes.pollFirst();
    }

    public void dropZombiePart(int row, double x, ZombiePartFx.Kind kind, ArmorType armorType) {
        dropZombiePart(row, x, kind, armorType, null);
    }

    public void dropZombiePart(
        int row,
        double x,
        ZombiePartFx.Kind kind,
        ArmorType armorType,
        Zombie zombie
    ) {
        pendingZombieParts.addLast(new ZombiePartFx(row, x, kind, armorType, zombie));
    }

    public ZombiePartFx pollZombiePart() {
        return pendingZombieParts.pollFirst();
    }
}
