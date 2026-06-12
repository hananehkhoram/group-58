package model;

import model.level.Level;
import model.plants.TargetingMode;
import model.plants.Plant;
import model.season.Grave;
import model.user.User;
import model.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

//saving everything needed during the game
public class GameContext {

    private final Level level;
    private final User user;
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

    public GameContext(Level level, User user) {
        this.level = level;
        this.user = user;
        this.plantGrid = new Plant[level.getRows()][level.getColumns()];
        this.graveGrid = new Grave[level.getRows()][level.getColumns()];
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

    public boolean isWaveCompleted(){}

    private boolean isWaveCleared(){}

    private void startWaveDelay(){}

    private void startNextWave(){}
}
