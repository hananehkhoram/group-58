package com.workshop.model;

import com.workshop.controller.NewsManager;
import com.workshop.controller.QuestManager;
import com.workshop.controller.ScoringManager;
import com.workshop.controller.SpecialLevelManager.*;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.MiniGame.Beghouled.BeghouledManager;
import com.workshop.model.level.Level;
import com.workshop.model.level.LevelType;
import com.workshop.model.mechanisms.*;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.PlantFamily;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.season.Grave;
import com.workshop.model.season.Season;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

import java.util.*;

public class GameContext {
    private final Level level;
    private final Season season;
    private final Plant[][] plantGrid;
    private List<Plant> alivePlants = new ArrayList<>();//گیاهای زنده روی زمین
    private final Grave[][] graveGrid;
    private List<Plant> activePlants = new ArrayList<>();//گیاهای انتخاب شده
    private List<Zombie> activeZombies = new ArrayList<>();//zombies to spawn
    private List<Zombie> aliveZombies = new ArrayList<>();//زامبیای زنده رو زمین
    private final Map<String, Long> plantCooldowns = new HashMap<>();
    private int sunAmount = 50;
    private int currentWaveIndex = 0;
    private boolean gameEnded = false;
    private boolean playerWon = false;
    private boolean waveSpawningFinished = false;
    private List<Projectile> projectiles = new ArrayList<>();
    private TimeManager timeManager;
    private Map<String, Integer> producedSuns = new HashMap<>();
    private Map<String, SunType> producedSunTypes = new HashMap<>();
    private final List<LootItem> activeLoots = new ArrayList<>();
    private SunManager sunManager;
    private int totalSunProducedInLevel = 0;
    private int totalLostPlants = 0;
    private int totalZombiesKilledInLevel = 0;

    private final List<PlantFoodDrop> activePlantFoodDrops = new ArrayList<>();

    private DataManager dm;
    private PlantFactory plantFactory;
    private final Deque<String> pendingAnnouncements = new ArrayDeque<>();
    private final Deque<Integer> pendingWindRows = new ArrayDeque<>();

    public void announce(String message) {
        if (message != null && !message.isBlank()) {
            pendingAnnouncements.addLast(message);
        }
    }

    private final Deque<Plant> pendingPlantAttackAnimations =
        new ArrayDeque<>();

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

    private boolean pendingSandstorm;

    public void announceSandstorm() {
        pendingSandstorm = true;
    }

    public boolean pollSandstorm() {
        if (!pendingSandstorm) {
            return false;
        }
        pendingSandstorm = false;
        return true;
    }

    public String pollAnnouncement() {
        return pendingAnnouncements.pollFirst();
    }
    private final Deque<String> pendingSoundCues = new ArrayDeque<>();
    public void playSound(String soundKey) {
        if (soundKey != null && !soundKey.isBlank()) {
            pendingSoundCues.addLast(soundKey);
        }
    }

    public String pollSoundCue() {
        return pendingSoundCues.pollFirst();
    }

    private final Deque<ExplosionFx> pendingExplosions = new ArrayDeque<>();
    private final Deque<ScreenShake> pendingShakes = new ArrayDeque<>();

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

    private final Deque<ProjectileHitFx> pendingProjectileHits = new ArrayDeque<>();

    public void spawnProjectileHit(int row, double x) {
        pendingProjectileHits.addLast(new ProjectileHitFx(row, x));
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

    private final Deque<ZombiePartFx> pendingZombieParts = new ArrayDeque<>();

    public void dropZombiePart(int row, double x, ZombiePartFx.Kind kind) {
        dropZombiePart(row, x, kind, null);
    }

    public void dropZombiePart(
        int row,
        double x,
        ZombiePartFx.Kind kind,
        com.workshop.model.zombie.behavior.ArmorType armorType
    ) {
        dropZombiePart(row, x, kind, armorType, null);
    }

    public void dropZombiePart(
        int row,
        double x,
        ZombiePartFx.Kind kind,
        com.workshop.model.zombie.behavior.ArmorType armorType,
        com.workshop.model.zombie.Zombie zombie
    ) {
        pendingZombieParts.addLast(new ZombiePartFx(row, x, kind, armorType, zombie));
    }

    public ZombiePartFx pollZombiePart() {
        return pendingZombieParts.pollFirst();
    }
    private boolean isSetupPhase = false;
    private LevelManager levelManager;
    private boolean activeWaveInProgress = false;
    private boolean manualStartCommandReceived = false;
    private long manualWaveStartTick = 0;
    private boolean battleStarted = false;

    private int multiKillPatternCount = 0;
    private int simultaneousKillPatternCount = 0;
    private int quickKillPatternCount = 0;
    private int killStreakPatternCount = 0;
    private int precisionFinishPatternCount = 0;
    private int currentKillStreak = 0;

    private final Set<PlantFamily> plantFamiliesUsedToKillThisLevel = new HashSet<>();
    private final Set<PlantFamily> plantFamiliesPlantedThisLevel = new HashSet<>();
    private final Set<String> plantNamesThatKilledThisLevel = new HashSet<>();
    private int totalKillsThisLevel = 0;

    private final Set<Integer> plantedColumns = new HashSet<>();
    private final Set<Integer> plantedRows = new HashSet<>();
    private int explosivePlantsPlacedThisLevel = 0;
    private int sunProducerPlantsPlacedThisLevel = 0;
    private int totalPlantsPlacedThisLevel = 0;
    private long firstWaveStartTick = -1;
    private final List<Long> earlyKillTicks = new ArrayList<>();   // برای «سرعت عمل»
    private int almostLostKillsThisLevel = 0;                       // برای «تقریبا پیروز»
    private int lawnMowerKillsThisLevel = 0;
    private String heldSeed = null;
    private BeghouledManager beghouledManager;

    private int zombiesKilledByLawnMowerThisLevel = 0;
    private com.workshop.controller.repository.factory.ZombieFactory zombieFactory;

    private GameEngine gameEngine;

    private boolean paused = false;

    public GameContext(Level level, Season season) {
        this.level = level;
        this.levelManager = DataManager.getInstance().createManagerForLevel(level);
        this.season = season;
        this.dm = DataManager.getInstance();
        this.plantFactory = new PlantFactory(dm);
        this.plantGrid = new Plant[level.getRows()][level.getColumns()];
        this.graveGrid = new Grave[level.getRows()][level.getColumns()];
        if (level.getWaves() != null) {
            for (com.workshop.model.mechanisms.Wave w : level.getWaves()) {
                w.reset();
            }
        }
        if (this.levelManager != null) this.levelManager.onLevelStart(this);
        this.timeManager = new TimeManager();
        this.sunManager = new SunManager(this.timeManager, level.getRows(), level.getColumns());

    }

    public boolean isOnCooldown(String plantName) {
        long availableAt = plantCooldowns.getOrDefault(plantName, 0L);
        return timeManager.getTotalTicks() < availableAt;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    public void setCooldown(String plantName, double rechargeSeconds) {
        long ticksFromNow = (long) (rechargeSeconds * 10);
        plantCooldowns.put(plantName, timeManager.getTotalTicks() + ticksFromNow);
    }

    public void clearAllCooldowns() { // برای cheat remove-cooldown
        plantCooldowns.clear();
    }
    public double getRemainingCooldownSeconds(String plantName) {
        long availableAt = plantCooldowns.getOrDefault(plantName, 0L);
        long remainingTicks = availableAt - timeManager.getTotalTicks();
        if (remainingTicks <= 0) return 0;
        return remainingTicks / 10.0;
    }

    public void produceSun(int x, int y, int amount) {
        produceSun(x, y, amount, SunType.NORMAL);
    }

    public void produceSun(
        int x,
        int y,
        int amount,
        SunType type
    ) {
        String key = x + ", " + y;

        producedSuns.put(
            key,
            producedSuns.getOrDefault(key, 0) + amount
        );

        producedSunTypes.put(key, type);
    }

    public boolean isSunPresent(int x, int y) {
        return producedSuns.containsKey(x + ", " + y);
    }

    public int collectSunAt(int x, int y) {
        String key = x + ", " + y;
        if (producedSuns.containsKey(key)) {
            int amount = producedSuns.remove(key);
            producedSunTypes.remove(key);

            addSun(amount);
            return amount;
        }
        return 0;
    }

    public Plant findNearestPlantInRow(Zombie zombie) {
        int row = zombie.getRow();
        Plant nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Plant p : getPlantGrid()[row]) {
            if (p == null || p.isDead()) continue;
            double dist = Math.abs(zombie.getX() - p.getCol());
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    public void addSun(int amount) {
        if (amount > 0) {
            this.sunAmount += amount;
            this.totalSunProducedInLevel += amount;
        }
    }

    public void triggerPlayerWin() {
        this.gameEnded = true;
        this.playerWon = true;
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getDifficultyLevel() == 5) {
                currentUser.setWinStreakAtMaxDifficulty(currentUser.getWinStreakAtMaxDifficulty() + 1);
            } else {
                currentUser.setWinStreakAtMaxDifficulty(0);
            }
            currentUser.setNumberOfPassedLevels(currentUser.getNumberOfPassedLevels() + 1);

            if (isMiniGameLevel()) {

                List<Level> miniLevels = this.season.getLevels();

                int index = miniLevels.indexOf(this.level);

                if (index + 1 < miniLevels.size()) {

                    currentUser.unlockLevel(
                        miniLevels
                            .get(index + 1)
                            .getName()
                    );
                }
            }

            if (isMiniGameLevel()) {
                currentUser.incrementMinigamesCompleted();
            }

            List<Level> levelsInSeason = this.season.getLevels();
            if (levelsInSeason == null) {
                levelsInSeason = new java.util.ArrayList<>();
            } // DebugF
            int levelIndex = levelsInSeason.indexOf(this.level);

            int chapterNumber = DataManager.getInstance().seasons.getChapterNumber(this.season);

            if(chapterNumber > 0) {
                currentUser.setLastLevel(levelIndex + 1);
                currentUser.setLastSeason(DataManager.getInstance().seasons.getChapterNumber(this.season));
            }

            if (levelIndex + 1 < levelsInSeason.size()) {
                currentUser.unlockLevel(levelsInSeason.get(levelIndex + 1).getName());
                NewsManager.addNews("New Level In Season","You unlocked new level: "+
                    levelsInSeason.get(levelIndex + 1).getName()+" in seasson: "+currentUser.getLastSeason());
            } else {
                Season nextSeason = DataManager.getInstance().seasons.getNextSeason(this.season);
                if (nextSeason != null && !nextSeason.getLevels().isEmpty()) {
                    currentUser.unlockLevel(nextSeason.getLevels().get(0).getName());
                    NewsManager.addNews("New Season","You unlocked season: "+nextSeason.getName());
                }
                String minigameName = DataManager.getInstance().getRelatedMinigame(this.season.getName());
                if (minigameName != null) {
                    Season minigame = DataManager.getInstance().seasons.get(minigameName);
                    if (minigame != null && !minigame.getLevels().isEmpty()) {
                        currentUser.unlockLevel(minigame.getLevels().get(0).getName());
                        NewsManager.addNews("New Minigame","You unlocked new minigame: "+minigameName);
                    }
                }
                if ("Beghouled".equalsIgnoreCase(this.season.getName())) {
                    Season zombotany =
                        DataManager.getInstance().seasons.get("Zombotany");

                    if (zombotany != null && !zombotany.getLevels().isEmpty()) {
                        currentUser.unlockLevel(
                            zombotany.getLevels().get(0).getName()
                        );

                        NewsManager.addNews(
                            "New Minigame",
                            "You unlocked new minigame: Zombotany"
                        );
                    }
                }
            }
            QuestManager.evaluateLevelEndQuests(this, currentUser);
            if (this.level.getLevelType() == LevelType.BONUS) {
                ScoringManager.evaluateLevelEndScoring(this, currentUser);
            }
        }
        DataManager.getInstance().saveUser();
        Console.
            showMessage("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
    }

    public void triggerPlayerLoss() {
        this.gameEnded = true;
        this.playerWon = false;
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.setWinStreakAtMaxDifficulty(0);
        }
        DataManager.getInstance().saveUser();
        Console.showMessage("The zombie ate your brain; LOSER!!!");
    }

    public void incrementZombieKills() {
        this.totalZombiesKilledInLevel++;
    }

    public void incrementPlantsLost(Plant p) {
        this.totalLostPlants++;
        resetKillStreak();
        Console.showMessage("Plant "+p.getName()+" at "+p.getCol()+", "+p.getRow()+" is destroyed.");
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

    private final List<Projectile> pendingProjectiles = new ArrayList<>();

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

    public List<Plant> getActivePlants() {
        return activePlants;
    }

    public List<Plant> getAlivePlants() {
        return alivePlants;
    }

    public List<Zombie> getActiveZombies() {
        return activeZombies;
    }

    public List<Zombie> getAliveZombies() {
        return aliveZombies;
    }

    public Level getLevel() {
        return level;
    }

    public Plant[][] getPlantGrid() {
        return plantGrid;
    }

    public Grave[][] getGraveGrid() {
        return graveGrid;
    }

    public int getSunAmount() {
        return sunAmount;
    }

    public void setSunAmount(int sunAmount) {
        this.sunAmount = sunAmount;
    }

    public int getTotalZombiesKilledInLevel() {
        return totalZombiesKilledInLevel;
    }

    public int getTotalSunProducedInLevel() {
        return totalSunProducedInLevel;
    }

    public int getTotalLostPlants() {
        return totalLostPlants;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public boolean isPlayerWon() {
        return playerWon;
    }

    public boolean isSetupPhase() {
        return isSetupPhase;
    }

    public void setSetupPhase(boolean v) {
        this.isSetupPhase = v;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public PlantFactory getPlantFactory() {
        return plantFactory;
    }

    public DataManager getDataManager() {
        return dm;
    }

    public Season getSeason() {
        return season;
    }

    public SunManager getSunManager() {
        return sunManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public boolean isBattleStarted() {
        return battleStarted;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setBattleStarted(boolean battleStarted) {
        this.battleStarted = battleStarted;
    }

    // When true, GameEngine no longer auto-triggers campaign win/loss for
    // I-Zombie levels (used by the networked "I, Zombie" match, which owns
    // its own win/lose rules and must not touch single-player progression).
    private boolean externalWinLossHandling = false;

    public boolean isExternalWinLossHandling() {
        return externalWinLossHandling;
    }

    public void setExternalWinLossHandling(boolean externalWinLossHandling) {
        this.externalWinLossHandling = externalWinLossHandling;
    }

    public void setActiveWaveInProgress(boolean activeWaveInProgress) {
        this.activeWaveInProgress = activeWaveInProgress;
    }

    public GameEngine getGameEngine() {
        return this.gameEngine;
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public boolean isManualStartCommandReceived() {
        return manualStartCommandReceived;
    }

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

    public long getManualWaveStartTick() {
        return manualWaveStartTick;
    }
    public void recordPlantKill(Plant killer) {
        if (killer == null) return;
        totalKillsThisLevel++;
        plantNamesThatKilledThisLevel.add(killer.getName());
        if (killer.getFamily() != null) {
            plantFamiliesUsedToKillThisLevel.add(killer.getFamily());
        }
    }
    public void recordPlantPlaced(Plant plant, int row, int col) {
        totalPlantsPlacedThisLevel++;
        plantedRows.add(row);
        plantedColumns.add(col);

        if (plant.getFamily() == PlantFamily.EXPLOSIVE) {
            explosivePlantsPlacedThisLevel++;
        }
        if (plant.getFamily() == PlantFamily.SUN_PRODUCER) {
            sunProducerPlantsPlacedThisLevel++;
        }
        if (plant.getFamily() != null) plantFamiliesPlantedThisLevel.add(plant.getFamily());
        plant.setRow(row);
        plant.setCol(col);
    }

    public Set<String> getPlantNamesThatKilledThisLevel()
    { return plantNamesThatKilledThisLevel; }
    public Set<PlantFamily> getPlantFamiliesUsedToKillThisLevel()
    { return plantFamiliesUsedToKillThisLevel; }
    public Set<PlantFamily> getPlantFamiliesPlantedThisLevel()
    { return plantFamiliesPlantedThisLevel; }
    public int getTotalKillsThisLevel()
    { return totalKillsThisLevel; }
    public Set<Integer> getPlantedColumns()
    { return plantedColumns; }
    public Set<Integer> getPlantedRows()
    { return plantedRows; }
    public int getExplosivePlantsPlacedThisLevel()
    { return explosivePlantsPlacedThisLevel; }
    public int getSunProducerPlantsPlacedThisLevel()
    { return sunProducerPlantsPlacedThisLevel; }
    public int getTotalPlantsPlacedThisLevel()
    { return totalPlantsPlacedThisLevel; }


    public String getHeldSeed()
    { return heldSeed; }
    public void setHeldSeed(String heldSeed) { this.heldSeed = heldSeed; }

    public BeghouledManager getBeghouldManager(){
        return beghouledManager;
    }
    public  void setBeghouldManager(BeghouledManager beghouldManager){
        this.beghouledManager = beghouldManager;
    }

    public void recordFirstWaveStart() {
        if (firstWaveStartTick == -1) {
            firstWaveStartTick = timeManager.getTotalTicks();
        }
    }
    public void recordZombieKillTick() {
        earlyKillTicks.add(timeManager.getTotalTicks());
    }
    public long getFirstWaveStartTick() { return firstWaveStartTick; }
    public List<Long> getEarlyKillTicks() { return earlyKillTicks; }
    public void recordAlmostLostKill() { almostLostKillsThisLevel++; }
    public int getAlmostLostKillsThisLevel() { return almostLostKillsThisLevel; }
    public void recordLawnMowerKill() { lawnMowerKillsThisLevel++; }
    public int getLawnMowerKillsThisLevel() { return lawnMowerKillsThisLevel; }

    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    public com.workshop.controller.repository.factory.ZombieFactory getZombieFactory() { return zombieFactory; }

    public void setZombieFactory(ZombieFactory zombieFactory) {
        this.zombieFactory = zombieFactory;
    }

    public List<LootItem> getActiveLoots() {
        return activeLoots;
    }
    public void addLoot(LootItem loot) {
        if (loot != null) {
            activeLoots.add(loot);
        }
    }
    public List<PlantFoodDrop> getActivePlantFoodDrops() {
        return activePlantFoodDrops;
    }

    public void addPlantFoodDrop(PlantFoodDrop drop) {
        if (drop != null) {
            activePlantFoodDrops.add(drop);
        }
    }

    public boolean collectPlantFoodDrop(PlantFoodDrop drop) {
        if (drop == null || !activePlantFoodDrops.remove(drop)) {
            return false;
        }

        com.workshop.model.user.UserManager.getInstance().addPlantFood(1);
        return true;
    }
    public void clearLoots() {
        activeLoots.clear();
    }
    public void incrementMultiKillPattern() { multiKillPatternCount++; }
    public void incrementSimultaneousKillPattern() { simultaneousKillPatternCount++; }
    public void incrementQuickKillPattern() { quickKillPatternCount++; }
    public void incrementPrecisionFinishPattern() { precisionFinishPatternCount++; }
    public void bumpKillStreak() {
        currentKillStreak++;
        if (currentKillStreak % 5 == 0) {
            killStreakPatternCount++;
        }
    }
    public void resetKillStreak() { currentKillStreak = 0; }
    public int getMultiKillPatternCount() { return multiKillPatternCount; }
    public int getSimultaneousKillPatternCount() { return simultaneousKillPatternCount; }
    public int getQuickKillPatternCount() { return quickKillPatternCount; }
    public int getKillStreakPatternCount() { return killStreakPatternCount; }
    public int getPrecisionFinishPatternCount() { return precisionFinishPatternCount; }

    private boolean isMiniGameLevel() {
        LevelType type = level.getLevelType();

        return type == LevelType.Wallnuts_MG
            || type == LevelType.Vase_MG
            || type == LevelType.Izambie_MG
            || type == LevelType.Beghouled_MG
            || type == LevelType.Zombotany_MG;
    }

    public Map<String, Integer> getProducedSuns() {
        return producedSuns;
    }

    public SunType getProducedSunTypeAt(int x, int y) {
        return producedSunTypes.getOrDefault(
            x + ", " + y,
            SunType.NORMAL
        );
    }
}
