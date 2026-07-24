package model.MiniGame.Izambi;

import controller.MenuManager;
import controller.repository.DataManager;
import controller.repository.factory.LevelFactory;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.season.Season;
import model.season.miniGameSeason.IzombieSeason;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.List;

public class Izambi {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;
    private int sunAmount;

    public Izambi() {}

    public void startMiniGame() {
        List<Level> izombieLevels = LevelFactory.buildWallnutsLevels();
        this.currentLevel = izombieLevels.get(0);

        Season izombieSeason = new IzombieSeason(izombieLevels);

        this.ctx = new GameContext(this.currentLevel, izombieSeason);
        this.gameEngine = new GameEngine(this.ctx, new MenuManager(ctx));

        this.sunAmount = 150;
        ctx.setSunAmount(this.sunAmount);

        initSunProducerZombies();
        initBrains();

        System.out.print("start\n");
    }

    public boolean placeZombie(String zombieType, int row, int col) {
        int cost = getZombieCost(zombieType);

        if (this.ctx.getSunAmount() >= cost) {
            this.ctx.setSunAmount(this.ctx.getSunAmount() - cost);

            ZombieFactory factory = new ZombieFactory(DataManager.getInstance());
            Zombie newZombie = factory.create(zombieType);
            if (newZombie != null) {
                newZombie.setY(row);
                newZombie.setX(col);
                ctx.getAliveZombies().add(newZombie);
                return true;
            }
        } else {
            ConsoleView.showMessage("Not enough sun to place this zombie!");
        }
        return false;
    }

    private int getZombieCost(String type) {
        return switch (type.toLowerCase()) {
            case "normal" -> 50;
            case "conehead" -> 100;
            case "buckethead" -> 125;
            case "football" -> 175;
            case "gargantuar" -> 350;
            default -> 50;
        };
    }

    private void initSunProducerZombies() {
        int rows = currentLevel.getRows();
        ZombieFactory factory = new ZombieFactory(DataManager.getInstance());
        for (int r = 0; r < rows; r++) {
            Zombie sunZombie = factory.create("normal");
            if (sunZombie != null) {
                sunZombie.setY(r);
                sunZombie.setX(0);
                ctx.getAliveZombies().add(sunZombie);
            }
        }
    }

    private void initBrains() {
    }

    public GameContext getCtx(){
        return this.ctx;
    }

    public GameEngine getGameEngine(){
        return this.gameEngine;
    }

    public void advancedTimeCommand(double sec){
        if (this.gameEngine != null) {
            this.gameEngine.update(sec);
            checkWinLossConditions();
        } else {
            ConsoleView.showMessage("Game engine is null");
        }
    }

    private void checkWinLossConditions() {
    }
}