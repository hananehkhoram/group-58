package model;

import controller.NewsManager;
import controller.QuestManager;
import controller.SpecialLevelManager.*;
import controller.repository.DataManager;
import controller.repository.factory.PlantFactory;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.mechanisms.SunManager;
import model.plants.Plant;
import model.plants.PlantFamily;
import model.projectile.Projectile;
import model.season.Grave;
import model.season.Season;
import model.user.User;
import model.user.UserManager;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.*;

public class GameContext {

    private final Level level;
    private final Season season;
    private final Plant[][] plantGrid;
    private final List<Plant> alivePlants = new ArrayList<>();//گیاهای زنده روی زمین
    private final Grave[][] graveGrid;
    private final List<Plant> activePlants = new ArrayList<>();//گیاهای انتخاب شده
    private final List<Zombie> activeZombies = new ArrayList<>();//zombies to spawn
    private final List<Zombie> aliveZombies = new ArrayList<>();//زامبیای زنده رو زمین
    private final Map<String, Long> plantCooldowns = new HashMap<>();
    private int sunAmount = 0;
    private int currentWaveIndex = 0;
    private boolean gameEnded = false;
    private boolean playerWon = false;
    private int waveDurationRemaining = 0;//بر حسب تیک
    private boolean waveSpawningFinished = false;
    private int remainingZombiesToSpawn = 0;
    private List<Projectile> projectiles = new ArrayList<>();
    private TimeManager timeManager;
    private Map<String, Integer> producedSuns = new HashMap<>();
    private SunManager sunManager;
    private int totalSunProducedInLevel = 0;
    private int totalLostPlants = 0;
    private int totalZombiesKilledInLevel = 0;

    private DataManager dm;
    private PlantFactory plantFactory;
    private boolean isSetupPhase = false;
    private LevelManager levelManager;
    private boolean activeWaveInProgress = false;
    private boolean manualStartCommandReceived = false;
    private boolean battleStarted = false;

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

    private int zombiesKilledByLawnMowerThisLevel = 0;

    private GameEngine gameEngine;

    public GameContext(Level level, Season season) {
        this.level = level;
        this.levelManager = createManagerForLevel(level);
        this.season = season;
        this.dm = DataManager.getInstance();
        this.plantFactory = new PlantFactory(dm);
        this.plantGrid = new Plant[level.getRows()][level.getColumns()];
        this.graveGrid = new Grave[level.getRows()][level.getColumns()];
        if (this.levelManager != null) this.levelManager.onLevelStart(this);
        this.timeManager = new TimeManager();
        this.sunManager = new SunManager(this.timeManager, level.getRows(), level.getColumns());

    }

    // SUN

    public boolean isOnCooldown(String plantName) {
        long availableAt = plantCooldowns.getOrDefault(plantName, 0L);
        return timeManager.getTotalTicks() < availableAt;
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
        String key = x + ", " + y;
        producedSuns.put(key, producedSuns.getOrDefault(key, 0) + amount);
    }

    public boolean isSunPresent(int x, int y) {
        return producedSuns.containsKey(x + ", " + y);
    }

    // EVENTS

    public int collectSunAt(int x, int y) {
        String key = x + ", " + y;
        if (producedSuns.containsKey(key)) {
            int amount = producedSuns.remove(key);
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
            List<Level> levelsInSeason = this.season.getLevels();
            int levelIndex = levelsInSeason.indexOf(this.level);

            currentUser.setLastLevel(levelIndex + 1);

            currentUser.setLastSeason(DataManager.getInstance().seasons.getChapterNumber(this.season));

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
                String minigameName = getRelatedMinigame(this.season.getName());
                if (minigameName != null) {
                    Season minigame = DataManager.getInstance().seasons.get(minigameName);
                    if (minigame != null && !minigame.getLevels().isEmpty()) {
                        currentUser.unlockLevel(minigame.getLevels().get(0).getName());
                        NewsManager.addNews("New Minigame","You unlocked new minigame: "+minigameName);
                    }
                }
            }
            QuestManager.evaluateLevelEndQuests(this, currentUser);
        }
        DataManager.getInstance().saveUser();
        ConsoleView.showMessage("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
    }
    private String getRelatedMinigame(String seasonName) {
        return switch (seasonName) {
            case "Ancient Egypt" -> "Vasebreaker";
            case "Frozen Caves" -> "Wallnut Bowling";
            case "Big Wave Beach" -> "I, Zombie";
            case "Dark Ages" -> "Beghouled";
            default -> null;
        };
    }

    public void triggerPlayerLoss() {
        this.gameEnded = true;
        this.playerWon = false;
//        UserManager.getInstance().saveToFile();
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.setWinStreakAtMaxDifficulty(0);
        }
        DataManager.getInstance().saveUser();
        ConsoleView.showMessage("The zombie ate your brain; LOSER!!!");
    }

    public void incrementZombieKills() {
        this.totalZombiesKilledInLevel++;
    }

    public void incrementPlantsLost() {
        this.totalLostPlants++;
        ConsoleView.showMessage("A plant was destroyed! Total lost: %d\n", totalLostPlants);
    }

    public void addZombie(Zombie z) {
        aliveZombies.add(z);
        if (!UserManager.getInstance().getCurrentUser().getSeenZombies().contains(z)) {
            NewsManager.addNews("New Zombie", "You unlocked: " + z.getName());
            UserManager.getInstance().getCurrentUser().getSeenZombies().add(z);
        }
    }

    // WAVE STATE

    public void addPlantFood(int amount) {
        UserManager.getInstance().getCurrentUser().setPlantFoodCount(UserManager.getInstance().getCurrentUser().getPlantFoodCount() + amount);
    }

    public boolean usePlantFood(int amount) {
        if (UserManager.getInstance().getCurrentUser().getPlantFoodCount() - amount < 0) return false;
        UserManager.getInstance().getCurrentUser().setPlantFoodCount(UserManager.getInstance().getCurrentUser().getPlantFoodCount() - amount);
        return true;
    }

    public void placeGrave(Grave g, int row, int col) {
        graveGrid[row][col] = g;
    }

    public void removeGrave(int row, int col) {
        graveGrid[row][col] = null;
    }

    public int getWaveDurationRemaining() {
        return waveDurationRemaining;
    }

    public void setWaveDurationRemaining(int ticks) {
        this.waveDurationRemaining = ticks;
    }

    public void decrementWaveDelay() {
        waveDurationRemaining--;
    }

    public int getCurrentWaveIndex() {
        return currentWaveIndex;
    }

    public  void setCurrentWaveIndex(int currentWaveIndex) {this.currentWaveIndex = currentWaveIndex;}

    // MISC

    public void incrementWaveIndex() {
        currentWaveIndex++;
    }

    public boolean isWaveSpawningFinished() {
        return waveSpawningFinished;
    }

    public void setWaveSpawningFinished(boolean v) {
        this.waveSpawningFinished = v;
    }

    public boolean canFreezeZombie() {
        return season.sunFallsFromSky();
    }

    // GETTERS

    public boolean isNecromancyCell(int row, int col) {
        return false;
    }

    public boolean DoesSunFall() {
        if (levelManager != null && levelManager.disableSkySun()) return false;
        return season != null && season.sunFallsFromSky();
    }

    private LevelManager createManagerForLevel(Level level) {
        switch (level.getLevelType()) {
            case CONVEYOR_BELT:
                return new ConveyorBeltManager();
            case SAVE_QUR_SEEDS:
                return new SaveOurSeedsManager();
            case TIMED_WAR:
                return new TimedWarManager();
            case NIGHT_OPS:
                return new NightOpsManager();
            case DEADLINE:
                return new DeadLineManager();
            case PLANT_WHAT_YOU_GET:
                return new PlantWhatYouGetManager();
            case LOCKED_PLANTS:
                return new LockedPlantsManager(level.getBannedPlants(), level.getForcedPlants());
            case NORMAL:
                return null;
            default:
                return null;
        }
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void setNewProjectiles(Projectile p) {
        this.projectiles.add(p);
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

    public void setBattleStarted(boolean battleStarted) {
        this.battleStarted = battleStarted;
    }

    public boolean isActiveWaveInProgress() {
        return activeWaveInProgress;
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
    }

    public Set<String> getPlantNamesThatKilledThisLevel() { return plantNamesThatKilledThisLevel; }
    public Set<PlantFamily> getPlantFamiliesUsedToKillThisLevel() { return plantFamiliesUsedToKillThisLevel; }
    public Set<PlantFamily> getPlantFamiliesPlantedThisLevel() { return plantFamiliesPlantedThisLevel; }
    public int getTotalKillsThisLevel() { return totalKillsThisLevel; }
    public Set<Integer> getPlantedColumns() { return plantedColumns; }
    public Set<Integer> getPlantedRows() { return plantedRows; }
    public int getExplosivePlantsPlacedThisLevel() { return explosivePlantsPlacedThisLevel; }
    public int getSunProducerPlantsPlacedThisLevel() { return sunProducerPlantsPlacedThisLevel; }
    public int getTotalPlantsPlacedThisLevel() { return totalPlantsPlacedThisLevel; }
    public int getZombiesKilledByLawnMowerThisLevel() { return zombiesKilledByLawnMowerThisLevel; }


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

}