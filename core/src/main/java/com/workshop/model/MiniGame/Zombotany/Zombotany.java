package com.workshop.model.MiniGame.Zombotany;

import com.workshop.controller.MenuManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
//import model.season.miniGameSeason.ZombotanySeason;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

import java.util.List;

public class Zombotany {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    public Zombotany() {}

    public void startMiniGame() {
        List<Level> zombotanyLevels = LevelFactory.buildWallnutsLevels();
        this.currentLevel = zombotanyLevels.get(0);

        this.gameEngine = new GameEngine(this.ctx, new MenuManager(ctx));
        this.ctx.setGameEngine(this.gameEngine);

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
            Console.showMessage("Game engine is null");
        }
    }

    private void checkWinLossConditions() {
    }
}
