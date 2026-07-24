package model.MiniGame.Zombotany;

import controller.MenuManager;
import controller.repository.DataManager;
import controller.repository.factory.LevelFactory;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.season.Season;
//import model.season.miniGameSeason.ZombotanySeason;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.List;

public class Zombotany {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    public Zombotany() {}

    public void startMiniGame() {
        List<Level> zombotanyLevels = LevelFactory.buildWallnutsLevels();
        this.currentLevel = zombotanyLevels.get(0);

        //Season zombotanySeason = new ZombotanySeason(zombotanyLevels);

        //this.ctx = new GameContext(this.currentLevel, zombotanySeason);
        this.gameEngine = new GameEngine(this.ctx, new MenuManager(ctx));

        spawnZombotanyZombies();

        System.out.print("zombotany start\n");
    }

    private void spawnZombotanyZombies() {
        ZombieFactory factory = new ZombieFactory(DataManager.getInstance());

        Zombie peashooterZombie = factory.create("peashooter_zombie");
        if (peashooterZombie != null) {
            peashooterZombie.setY(0);
            peashooterZombie.setX(8);
            ctx.getAliveZombies().add(peashooterZombie);
        }

        Zombie wallnutZombie = factory.create("wallnut_zombie");
        if (wallnutZombie != null) {
            wallnutZombie.setY(1);
            wallnutZombie.setX(8);
            ctx.getAliveZombies().add(wallnutZombie);
        }

        Zombie jalapenoZombie = factory.create("jalapeno_zombie");
        if (jalapenoZombie != null) {
            jalapenoZombie.setY(2);
            jalapenoZombie.setX(8);
            ctx.getAliveZombies().add(jalapenoZombie);
        }

        Zombie squashZombie = factory.create("squash_zombie");
        if (squashZombie != null) {
            squashZombie.setY(3);
            squashZombie.setX(8);
            ctx.getAliveZombies().add(squashZombie);
        }
    }

    public GameContext getCtx() {
        return this.ctx;
    }

    public GameEngine getGameEngine() {
        return this.gameEngine;
    }

    public void advancedTimeCommand(double sec) {
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