package com.workshop.model;

import com.workshop.controller.NewsManager;
import com.workshop.controller.SpecialLevelManager.LevelManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.MiniGame.Beghouled.BeghouledManager;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.*;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.PlantFamily;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.season.Grave;
import com.workshop.model.season.Season;
import com.workshop.model.user.UserManager;
import com.workshop.model.zombie.Zombie;
import com.workshop.model.zombie.behavior.ArmorType;
import com.workshop.view.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameContext {
    private final Level level;
    private final Season season;
    private final Plant[][] plantGrid;
    private List<Plant> alivePlants = new ArrayList<>();
    private final List<Plant> pulledPlants = new ArrayList<>();
    private final Grave[][] graveGrid;
    private final long[][] burnUntilTick;
    private List<Plant> activePlants = new ArrayList<>();
    private List<Zombie> activeZombies = new ArrayList<>();
    private List<Zombie> aliveZombies = new ArrayList<>();
    private int sunAmount = 50;
    private int currentWaveIndex = 0;
    private boolean gameEnded;
    private boolean playerWon;
    private boolean waveSpawningFinished;
    private List<Projectile> projectiles = new ArrayList<>();
    private final List<Projectile> pendingProjectiles = new ArrayList<>();
    private TimeManager timeManager;
    private SunManager sunManager;
    private DataManager dm;
    private PlantFactory plantFactory;
    private final PendingFxQueue fx = new PendingFxQueue();
    private final BoardPickups pickups = new BoardPickups();
    private final LevelStats stats = new LevelStats();

    private boolean isSetupPhase;
    private LevelManager levelManager;
    private boolean activeWaveInProgress;
    private boolean manualStartCommandReceived;
    private long manualWaveStartTick;
    private boolean battleStarted;
    private boolean paused;
    private boolean externalWinLossHandling;
    private String heldSeed;
    private BeghouledManager beghouledManager;
    private ZombieFactory zombieFactory;
    private GameEngine gameEngine;

    public GameContext(Level level, Season season) {
        this.level = level;
        this.levelManager = DataManager.getInstance().createManagerForLevel(level);
        this.season = season;
        this.dm = DataManager.getInstance();
        this.plantFactory = new PlantFactory(dm);
        this.plantGrid = new Plant[level.getRows()][level.getColumns()];
        this.graveGrid = new Grave[level.getRows()][level.getColumns()];
        this.burnUntilTick = new long[level.getRows()][level.getColumns()];
        if (level.getWaves() != null) {
            for (Wave w : level.getWaves()) {
                w.reset();
            }
        }
        if (this.levelManager != null) {
            this.levelManager.onLevelStart(this);
        }
        this.timeManager = new TimeManager();
        this.sunManager = new SunManager(this.timeManager, level.getRows(), level.getColumns());
    }

    // --- FX (delegated) ---

    public void announce(String message) { fx.announce(message); }
    public void queuePlantAttackAnimation(Plant plant) { fx.queuePlantAttackAnimation(plant); }
    public Plant pollPlantAttackAnimation() { return fx.pollPlantAttackAnimation(); }
    public void announceWindRow(int row) { fx.announceWindRow(row); }
    public Integer pollWindRow() { return fx.pollWindRow(); }
    public String pollAnnouncement() { return fx.pollAnnouncement(); }
    public void playSound(String soundKey) { fx.playSound(soundKey); }
    public String pollSoundCue() { return fx.pollSoundCue(); }
    public void spawnExplosion(int row, int col, ExplosionFx.Kind kind) { fx.spawnExplosion(row, col, kind); }
    public ExplosionFx pollExplosion() { return fx.pollExplosion(); }
    public void spawnBeachShark(int row, int col) { fx.spawnBeachShark(row, col); }
    public PendingFxQueue.BeachSharkSpawn pollBeachShark() { return fx.pollBeachShark(); }
    public void spawnEgyptMissile(int row, int col, float flightSeconds) { fx.spawnEgyptMissile(row, col, flightSeconds); }
    public PendingFxQueue.EgyptMissileSpawn pollEgyptMissile() { return fx.pollEgyptMissile(); }
    public void spawnEgyptSummon(int row, double x) { fx.spawnEgyptSummon(row, x); }
    public PendingFxQueue.EgyptSummonSpawn pollEgyptSummon() { return fx.pollEgyptSummon(); }
    public void spawnIceMissile(int row, int col, float flightSeconds) { fx.spawnIceMissile(row, col, flightSeconds); }
    public PendingFxQueue.IceMissileSpawn pollIceMissile() { return fx.pollIceMissile(); }
    public void spawnIceSummon(int row, int col) { fx.spawnIceSummon(row, col); }
    public PendingFxQueue.IceSummonSpawn pollIceSummon() { return fx.pollIceSummon(); }
    public void spawnDarkFireball(int row, int col, float flightSeconds) { fx.spawnDarkFireball(row, col, flightSeconds); }
    public PendingFxQueue.DarkFireballSpawn pollDarkFireball() { return fx.pollDarkFireball(); }
    public void spawnDarkFireBreath(int topRow, int bottomRow, float durationSeconds) {
        fx.spawnDarkFireBreath(topRow, bottomRow, durationSeconds);
    }
    public PendingFxQueue.DarkFireBreathSpawn pollDarkFireBreath() { return fx.pollDarkFireBreath(); }
    public void setBeachVortexRows(int topRow, int bottomRow) { fx.setBeachVortexRows(topRow, bottomRow); }
    public void clearBeachVortex() { fx.clearBeachVortex(); }
    public boolean hasBeachVortex() { return fx.hasBeachVortex(); }
    public int getBeachVortexTopRow() { return fx.getBeachVortexTopRow(); }
    public int getBeachVortexBottomRow() { return fx.getBeachVortexBottomRow(); }
    public void spawnProjectileHit(int row, double x, double y) { fx.spawnProjectileHit(row, x, y); }
    public ProjectileHitFx pollProjectileHit() { return fx.pollProjectileHit(); }
    public void shakeScreen(float intensity, float duration) { fx.shakeScreen(intensity, duration); }
    public ScreenShake pollScreenShake() { return fx.pollScreenShake(); }
    public void dropZombiePart(int row, double x, ZombiePartFx.Kind kind, ArmorType armorType) {
        fx.dropZombiePart(row, x, kind, armorType);
    }
    public void dropZombiePart(int row, double x, ZombiePartFx.Kind kind, ArmorType armorType, Zombie zombie) {
        fx.dropZombiePart(row, x, kind, armorType, zombie);
    }
    public ZombiePartFx pollZombiePart() { return fx.pollZombiePart(); }

    // --- pickups / cooldowns ---

    public boolean isOnCooldown(String plantName) {
        return pickups.isOnCooldown(plantName, timeManager);
    }

    public void setCooldown(String plantName, double rechargeSeconds) {
        pickups.setCooldown(plantName, rechargeSeconds, timeManager);
    }

    public void clearAllCooldowns() {
        pickups.clearAllCooldowns();
    }

    public double getRemainingCooldownSeconds(String plantName) {
        return pickups.getRemainingCooldownSeconds(plantName, timeManager);
    }

    public void produceSun(int x, int y, int amount) {
        produceSun(x, y, amount, SunType.NORMAL);
    }

    public void produceSun(int x, int y, int amount, SunType type) {
        pickups.produceSun(x, y, amount, type);
    }

    public boolean isSunPresent(int x, int y) {
        return pickups.isSunPresent(x, y);
    }

    public int collectSunAt(int x, int y) {
        return pickups.collectSunAt(x, y, this::addSun);
    }

    public Map<String, Integer> getProducedSuns() {
        return pickups.getProducedSuns();
    }

    public SunType getProducedSunTypeAt(int x, int y) {
        return pickups.getProducedSunTypeAt(x, y);
    }

    public List<LootItem> getActiveLoots() {
        return pickups.getActiveLoots();
    }

    public void addLoot(LootItem loot) {
        pickups.addLoot(loot);
    }

    public void clearLoots() {
        pickups.clearLoots();
    }

    public List<PlantFoodDrop> getActivePlantFoodDrops() {
        return pickups.getActivePlantFoodDrops();
    }

    public void addPlantFoodDrop(PlantFoodDrop drop) {
        pickups.addPlantFoodDrop(drop);
    }

    public boolean collectPlantFoodDrop(PlantFoodDrop drop) {
        return pickups.collectPlantFoodDrop(drop);
    }

    // --- battlefield ---

    public Plant findNearestPlantInRow(Zombie zombie) {
        return BattlefieldQueries.findNearestPlantInRow(plantGrid, zombie);
    }

    public void igniteCell(int row, int col, double seconds) {
        BattlefieldQueries.igniteCell(plantGrid, burnUntilTick, timeManager, row, col, seconds);
    }

    public boolean isBurnedCell(int row, int col) {
        return BattlefieldQueries.isBurnedCell(burnUntilTick, timeManager, row, col);
    }

    public Zombie findAliveBoss() {
        return BattlefieldQueries.findAliveBoss(aliveZombies);
    }

    public boolean hasHostileAhead(int row, double fromX) {
        return BattlefieldQueries.hasHostileAhead(aliveZombies, graveGrid, row, fromX);
    }

    public void addSun(int amount) {
        if (amount > 0) {
            this.sunAmount += amount;
            stats.addSunProduced(amount);
        }
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    public void triggerPlayerWin() {
        this.gameEnded = true;
        this.playerWon = true;
        LevelEndProgression.onPlayerWin(this);
    }

    public void triggerPlayerLoss() {
        this.gameEnded = true;
        this.playerWon = false;
        LevelEndProgression.onPlayerLoss();
    }

    public void incrementZombieKills() {
        stats.incrementZombieKills();
    }

    public void incrementPlantsLost(Plant p) {
        stats.incrementPlantsLost();
        Console.showMessage("Plant " + p.getName() + " at " + p.getCol() + ", " + p.getRow() + " is destroyed.");
    }

    public void addZombie(Zombie z) {
        z.setSpawnTick(timeManager.getTotalTicks());
        aliveZombies.add(z);
        List<Zombie> seenZombies = UserManager.getInstance().getCurrentUser().getSeenZombies();
        boolean alreadySeen = seenZombies.stream().anyMatch(sz -> sz.getName().equals(z.getName()));
        if (!alreadySeen) {
            NewsManager.addNews("New Zombie", "You unlocked: " + z.getName());
            seenZombies.add(z);
        }
    }

    public void placeGrave(Grave g, int row, int col) {
        graveGrid[row][col] = g;
    }

    public void removeGrave(int row, int col) {
        graveGrid[row][col] = null;
    }

    public int getCurrentWaveIndex() {
        return currentWaveIndex;
    }

    public void incrementWaveIndex() {
        currentWaveIndex++;
    }

    public boolean isWaveSpawningFinished() {
        return waveSpawningFinished;
    }

    public void setWaveSpawningFinished(boolean v) {
        this.waveSpawningFinished = v;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void setNewProjectiles(Projectile p) {
        if (p != null) {
            pendingProjectiles.add(p);
        }
    }

    public void flushPendingProjectiles() {
        if (pendingProjectiles.isEmpty()) {
            return;
        }
        projectiles.addAll(pendingProjectiles);
        pendingProjectiles.clear();
    }

    public List<Plant> getActivePlants() { return activePlants; }
    public List<Plant> getAlivePlants() { return alivePlants; }
    public List<Plant> getPulledPlants() { return pulledPlants; }

    public void addPulledPlant(Plant plant) {
        if (plant != null && !pulledPlants.contains(plant)) {
            pulledPlants.add(plant);
        }
    }

    public void removePulledPlant(Plant plant) {
        pulledPlants.remove(plant);
    }

    public List<Zombie> getActiveZombies() { return activeZombies; }
    public List<Zombie> getAliveZombies() { return aliveZombies; }
    public Level getLevel() { return level; }
    public Plant[][] getPlantGrid() { return plantGrid; }
    public Grave[][] getGraveGrid() { return graveGrid; }
    public int getSunAmount() { return sunAmount; }
    public void setSunAmount(int sunAmount) { this.sunAmount = sunAmount; }
    public int getTotalZombiesKilledInLevel() { return stats.getTotalZombiesKilledInLevel(); }
    public int getTotalSunProducedInLevel() { return stats.getTotalSunProducedInLevel(); }
    public int getTotalLostPlants() { return stats.getTotalLostPlants(); }
    public boolean isGameEnded() { return gameEnded; }
    public boolean isPlayerWon() { return playerWon; }
    public boolean isSetupPhase() { return isSetupPhase; }
    public void setSetupPhase(boolean v) { this.isSetupPhase = v; }
    public TimeManager getTimeManager() { return timeManager; }
    public PlantFactory getPlantFactory() { return plantFactory; }
    public DataManager getDataManager() { return dm; }
    public Season getSeason() { return season; }
    public SunManager getSunManager() { return sunManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public boolean isBattleStarted() { return battleStarted; }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public void setBattleStarted(boolean battleStarted) { this.battleStarted = battleStarted; }

    public boolean isExternalWinLossHandling() { return externalWinLossHandling; }
    public void setExternalWinLossHandling(boolean externalWinLossHandling) {
        this.externalWinLossHandling = externalWinLossHandling;
    }

    public void setActiveWaveInProgress(boolean activeWaveInProgress) {
        this.activeWaveInProgress = activeWaveInProgress;
    }

    public GameEngine getGameEngine() { return this.gameEngine; }
    public void setGameEngine(GameEngine gameEngine) { this.gameEngine = gameEngine; }
    public boolean isManualStartCommandReceived() { return manualStartCommandReceived; }
    public void setManualStartCommandReceived(boolean manualStartCommandReceived) {
        this.manualStartCommandReceived = manualStartCommandReceived;
    }

    public void triggerManualWaveStart() {
        this.manualStartCommandReceived = true;
        this.isSetupPhase = false;
        if (timeManager != null) {
            this.manualWaveStartTick = timeManager.getTotalTicks();
        }
    }

    public long getManualWaveStartTick() { return manualWaveStartTick; }

    public void recordPlantKill(Plant killer) { stats.recordPlantKill(killer); }
    public void recordPlantPlaced(Plant plant, int row, int col) { stats.recordPlantPlaced(plant, row, col); }
    public Set<String> getPlantNamesThatKilledThisLevel() { return stats.getPlantNamesThatKilledThisLevel(); }
    public Set<PlantFamily> getPlantFamiliesUsedToKillThisLevel() { return stats.getPlantFamiliesUsedToKillThisLevel(); }
    public Set<PlantFamily> getPlantFamiliesPlantedThisLevel() { return stats.getPlantFamiliesPlantedThisLevel(); }
    public int getTotalKillsThisLevel() { return stats.getTotalKillsThisLevel(); }
    public Set<Integer> getPlantedColumns() { return stats.getPlantedColumns(); }
    public Set<Integer> getPlantedRows() { return stats.getPlantedRows(); }
    public int getExplosivePlantsPlacedThisLevel() { return stats.getExplosivePlantsPlacedThisLevel(); }
    public int getSunProducerPlantsPlacedThisLevel() { return stats.getSunProducerPlantsPlacedThisLevel(); }
    public int getTotalPlantsPlacedThisLevel() { return stats.getTotalPlantsPlacedThisLevel(); }

    public String getHeldSeed() { return heldSeed; }
    public void setHeldSeed(String heldSeed) { this.heldSeed = heldSeed; }
    public BeghouledManager getBeghouldManager() { return beghouledManager; }
    public void setBeghouldManager(BeghouledManager beghouldManager) { this.beghouledManager = beghouldManager; }

    public void recordFirstWaveStart() { stats.recordFirstWaveStart(timeManager); }
    public void recordZombieKillTick() { stats.recordZombieKillTick(timeManager); }
    public long getFirstWaveStartTick() { return stats.getFirstWaveStartTick(); }
    public List<Long> getEarlyKillTicks() { return stats.getEarlyKillTicks(); }
    public void recordAlmostLostKill() { stats.recordAlmostLostKill(); }
    public int getAlmostLostKillsThisLevel() { return stats.getAlmostLostKillsThisLevel(); }
    public void recordLawnMowerKill() { stats.recordLawnMowerKill(); }
    public int getLawnMowerKillsThisLevel() { return stats.getLawnMowerKillsThisLevel(); }

    public void setLevelManager(LevelManager levelManager) { this.levelManager = levelManager; }
    public ZombieFactory getZombieFactory() { return zombieFactory; }
    public void setZombieFactory(ZombieFactory zombieFactory) { this.zombieFactory = zombieFactory; }

    public void incrementMultiKillPattern() { stats.incrementMultiKillPattern(); }
    public void incrementSimultaneousKillPattern() { stats.incrementSimultaneousKillPattern(); }
    public void incrementQuickKillPattern() { stats.incrementQuickKillPattern(); }
    public void incrementPrecisionFinishPattern() { stats.incrementPrecisionFinishPattern(); }
    public void bumpKillStreak() { stats.bumpKillStreak(); }
    public void resetKillStreak() { stats.resetKillStreak(); }
    public int getMultiKillPatternCount() { return stats.getMultiKillPatternCount(); }
    public int getSimultaneousKillPatternCount() { return stats.getSimultaneousKillPatternCount(); }
    public int getQuickKillPatternCount() { return stats.getQuickKillPatternCount(); }
    public int getKillStreakPatternCount() { return stats.getKillStreakPatternCount(); }
    public int getPrecisionFinishPatternCount() { return stats.getPrecisionFinishPatternCount(); }
}
