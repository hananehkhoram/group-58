package model;

import controller.LevelManager.ConveyorBeltManager;
import controller.LevelManager.LevelManager;
import controller.repository.DataManager;
import controller.repository.factory.PlantFactory;
import controller.repository.factory.ZombieFactory;
import model.Projectile.Projectile;
import model.level.Level;
import model.level.LevelType;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.season.Grave;
import model.season.Season;
import model.user.User;
import model.user.UserManager;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

//saving everything needed during the game
public class GameContext {

    private final Level level;
    private final Season season;
    private final Plant[][] plantGrid;
    private final List<Plant> alivePlants = new ArrayList<>();//گیاهای زنده روی زمین
    private final Grave[][] graveGrid;
    private final List<Plant> activePlants = new ArrayList<>();//گیاهای انتخاب شده
    private final List<Zombie> activeZombies = new ArrayList<>();//zombies to spawn
    private final List<Zombie> aliveZombies = new ArrayList<>();
    private int sunAmount = 0;
    private int plantFoodCount = 0;
    private int currentWaveIndex = 0;
    private boolean gameEnded = false;
    private boolean playerWon = false;
    private int waveDurationRemaining = 0;//بر حسب تیک
    private boolean waveSpawningFinished = false;
    private int remainingZombiesToSpawn = 0;
    private List<Projectile> projectiles = new ArrayList<>();

    private DataManager dm;
    private PlantFactory plantFactory;

    private LevelManager levelManager;

    public GameContext(Level level,Season season) {
        this.level = level;
        this.levelManager = createManagerForLevel(level);
        this.season = season;
        this.dm = DataManager.getInstance();
        this.plantFactory = new PlantFactory(dm);

        this.plantGrid = new Plant[level.getRows()][level.getColumns()];
        this.graveGrid = new Grave[level.getRows()][level.getColumns()];
        this.plantFoodCount = UserManager.getInstance().getCurrentUser().getPlantFoodCount();

        if (this.levelManager != null) {
            this.levelManager.onLevelStart(this);
        }
    }



    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public void setNewProjectiles(Projectile projectile) {
        this.projectiles.add(projectile);
    }

    public void addSun(int amount) {
    }

    public void addZombie(Zombie z) {
    }

    public void placeGrave(Grave g, int row, int col) {
    }
    public void removeGrave(int row, int col){}

    public boolean canFreezeZombie() {
        return season.sunFallsFromSky();
    }

    public boolean doesSunFall() {
        return false;
    }

    public boolean isNecromancyCell(int row, int col) {
        return false;
    }

    public List<Zombie> findTargets(int row, int col, TargetingMode mode) {
        return null;
    }

    public void update(double deltaTime) {
    }

    private void checkGameEnd() {
    }

    public void onWaveStart(int waveNumber, int waveDelay) {
    }

    public boolean isWaveCompleted() {
        return false;
    }

    private boolean isWaveCleared() {
        return false;
    }

    private void startWaveDelay() {
    }

    private void startNextWave() {
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

    public void addPlantFood(int amount){
        this.plantFoodCount += amount;
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

    private LevelManager createManagerForLevel(Level level) {
        switch (level.getLevelType()) {
            case CONVEYOR_BELT:
                return new ConveyorBeltManager();

            case NORMAL:
            default:
                return null;
        }
    }
}
