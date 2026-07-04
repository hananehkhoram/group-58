package model;

import model.Projectile.Projectile;
import model.level.Level;
import model.plants.Plant;
import model.plants.TargetingMode;
import model.season.Grave;
import model.user.User;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//saving everything needed during the game
public class GameContext {

    private final Level level;
    private final Plant[][] plantGrid;
    private final Grave[][] graveGrid;
    private final List<Plant> activePlants = new ArrayList<>();
    private final List<Zombie> activeZombies = new ArrayList<>();
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

    public GameContext(Level level) {
        this.level = level;
        this.plantGrid = new Plant[level.getRows()][level.getColumns()];
        this.graveGrid = new Grave[level.getRows()][level.getColumns()];
        this.timeManager = new TimeManager();
    }

    public void produceSun(int x, int y, int amount) {
        String coordinateKey = x + ", " + y;
        int currentAmount = producedSuns.getOrDefault(coordinateKey, 0);
        producedSuns.put(coordinateKey, currentAmount + amount);
    }

    public boolean isSunPresent(int x, int y){
        return producedSuns.containsKey(x + ", " + y);
    }

    public int collectSunAt(int x, int y) {
        String coordinateKey = x + ", " + y;

        if (producedSuns.containsKey(coordinateKey)) {
            int amount = producedSuns.remove(coordinateKey);

            this.addSun(amount);
            return amount;
        }
        return 0;
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

    public boolean canFreezeZombie() {
        return false;
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

    public List<Zombie> getActiveZombies() {
        return activeZombies;
    }

    public TimeManager getTimeManager() {
        return this.timeManager;
    }
}
