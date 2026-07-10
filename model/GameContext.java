package model;

import controller.NewsManager;
import controller.SpecialLevelManager.*;
import controller.repository.DataManager;
import controller.repository.factory.PlantFactory;
import model.Projectile.Projectile;
import model.level.Level;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.season.Grave;
import model.season.Season;
import model.user.UserManager;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameContext {

    private final Level level;
    private final Season season;
    private final Plant[][] plantGrid;
    private final List<Plant> alivePlants = new ArrayList<>();//گیاهای زنده روی زمین
    private final Grave[][] graveGrid;
    private final List<Plant> activePlants = new ArrayList<>();//گیاهای انتخاب شده
    private final List<Zombie> activeZombies = new ArrayList<>();//zombies to spawn
    private final List<Zombie> aliveZombies = new ArrayList<>();//زامبیای زنده رو زمین
    private int sunAmount = 0;
    private int plantFoodCount = 0;
    private int currentWaveIndex = 0;
    private boolean gameEnded = false;
    private boolean playerWon = false;
    private int waveDurationRemaining = 0;//بر حسب تیک
    private boolean waveSpawningFinished = false;
    private int remainingZombiesToSpawn = 0;
    private List<Projectile> projectiles = new ArrayList<>();
    private TimeManager timeManager;
    private Map<String, Integer> producedSuns = new HashMap<>();

    private int totalSunProducedInLevel = 0;
    private int totalLostPlants = 0;
    private int totalZombiesKilledInLevel = 0;

    private DataManager dm;
    private PlantFactory plantFactory;
    private boolean isSetupPhase = false;
    private LevelManager levelManager;

    public GameContext(Level level, Season season) {
        this.level = level;
        this.levelManager = createManagerForLevel(level);
        this.season = season;
        this.dm = DataManager.getInstance();
        this.plantFactory = new PlantFactory(dm);
        this.plantGrid = new Plant[level.getRows()][level.getColumns()];
        this.graveGrid = new Grave[level.getRows()][level.getColumns()];
        this.plantFoodCount = UserManager.getInstance().getCurrentUser().getPlantFoodCount();
        if (this.levelManager != null) this.levelManager.onLevelStart(this);
        this.timeManager = new TimeManager();
    }

    // SUN

    public void produceSun(int x, int y, int amount) {
        String key = x + ", " + y;
        producedSuns.put(key, producedSuns.getOrDefault(key, 0) + amount);
    }

    public boolean isSunPresent(int x, int y) {
        return producedSuns.containsKey(x + ", " + y);
    }

    public int collectSunAt(int x, int y) {
        String key = x + ", " + y;
        if (producedSuns.containsKey(key)) {
            int amount = producedSuns.remove(key);
            addSun(amount);
            return amount;
        }
        return 0;
    }

    public void addSun(int amount) {
        if (amount > 0) {
            this.sunAmount += amount;
            this.totalSunProducedInLevel += amount;
        }
    }

    // EVENTS

    public String triggerPlayerWin() {
        this.gameEnded = true;
        this.playerWon = true;
        return("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
    }

    public String triggerPlayerLoss() {
        this.gameEnded = true;
        this.playerWon = false;
        return("Game Over! You lost the battle!!");
    }

    public void incrementZombieKills() { this.totalZombiesKilledInLevel++; }

    public void incrementPlantsLost() {
        this.totalLostPlants++;
        ConsoleView.showMessage("A plant was destroyed! Total lost: %d\n",totalLostPlants);
    }

    public void addZombie(Zombie z) {
        aliveZombies.add(z);
        if (!UserManager.getInstance().getCurrentUser().getSeenZombies().contains(z)){
            NewsManager.addNews("New Zombie","You unlocked: "+z.getName());
            UserManager.getInstance().getCurrentUser().getSeenZombies().add(z);
        }
    }
    public void addPlantFood(int amount)     { this.plantFoodCount += amount; }

    public void placeGrave(Grave g, int row, int col) { graveGrid[row][col] = g; }
    public void removeGrave(int row, int col)          { graveGrid[row][col] = null; }

    // WAVE STATE

    public int getWaveDurationRemaining()              { return waveDurationRemaining; }
    public void setWaveDurationRemaining(int ticks)    { this.waveDurationRemaining = ticks; }
    public void decrementWaveDelay()                   { waveDurationRemaining--; }
    public int getCurrentWaveIndex()                   { return currentWaveIndex; }
    public void incrementWaveIndex()                   { currentWaveIndex++; }
    public boolean isWaveSpawningFinished()            { return waveSpawningFinished; }
    public void setWaveSpawningFinished(boolean v)     { this.waveSpawningFinished = v; }

    // MISC

    public boolean canFreezeZombie()                   { return season.sunFallsFromSky(); }
    public boolean isNecromancyCell(int row, int col)  { return false; }

    public boolean DoesSunFall() {
        if (levelManager != null && levelManager.disableSkySun()) return false;
        return season != null && season.sunFallsFromSky();
    }

    private LevelManager createManagerForLevel(Level level) {
        switch (level.getLevelType()) {
            case CONVEYOR_BELT:      return new ConveyorBeltManager();
            case SAVE_QUR_SEEDS:     return new SaveOurSeedsManager();
            case TIMED_WAR:          return new TimedWarManager();
            case NIGHT_OPS:          return new NightOpsManager();
            case DEADLINE:           return new DeadLineManager();
            case PLANT_WHAT_YOU_GET: return new PlantWhatYouGetManager();
            case LOCKED_PLANTS:
                return new LockedPlantsManager(level.getBannedPlants(), level.getForcedPlants());
            case NORMAL: default:    return null;
        }
    }

    // GETTERS

    public List<Projectile> getProjectiles()      { return projectiles; }
    public void setNewProjectiles(Projectile p)   { this.projectiles.add(p); }
    public List<Plant> getActivePlants()           { return activePlants; }
    public List<Plant> getAlivePlants()            { return alivePlants; }
    public List<Zombie> getActiveZombies()         { return activeZombies; }
    public List<Zombie> getAliveZombies()          { return aliveZombies; }
    public Level getLevel()                        { return level; }
    public Plant[][] getPlantGrid()                { return plantGrid; }
    public Grave[][] getGraveGrid()                { return graveGrid; }
    public int getSunAmount()                      { return sunAmount; }
    public void setSunAmount(int sunAmount)        { this.sunAmount = sunAmount; }
    public int getTotalZombiesKilledInLevel()      { return totalZombiesKilledInLevel; }
    public int getTotalSunProducedInLevel()        { return totalSunProducedInLevel; }
    public int getTotalLostPlants()                { return totalLostPlants; }
    public boolean isGameEnded()                   { return gameEnded; }
    public boolean isPlayerWon()                   { return playerWon; }
    public boolean isSetupPhase()                  { return isSetupPhase; }
    public void setSetupPhase(boolean v)           { this.isSetupPhase = v; }
    public TimeManager getTimeManager()            { return timeManager; }
    public PlantFactory getPlantFactory()          { return plantFactory; }
    public DataManager getDataManager()            { return dm; }
    public Season getSeason() {return season;}
}