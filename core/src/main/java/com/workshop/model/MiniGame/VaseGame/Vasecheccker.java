package com.workshop.model.MiniGame.VaseGame;

import com.workshop.controller.MenuManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.season.Season;
import com.workshop.model.season.miniGameSeason.VaseSeason;

import java.util.List;

public class Vasecheccker {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    public Vasecheccker() {}

    public void startMiniGame(MenuManager menuManager) {
        List<Level> bowlingLevels = LevelFactory.buildVaseLevels();
        this.currentLevel = bowlingLevels.get(0);

        Season vaseSeason = new VaseSeason(bowlingLevels);

        this.ctx = new GameContext(this.currentLevel, vaseSeason);
        ctx.setZombieFactory(new com.workshop.controller.repository.factory.ZombieFactory(DataManager.getInstance()));
        this.ctx.setLevelManager(new com.workshop.controller.SpecialLevelManager.ConveyorBeltManager());
        this.gameEngine = new GameEngine(this.ctx, menuManager);
        this.ctx.setGameEngine(this.gameEngine);

        LevelFactory.setUpVases(this.ctx);

        ctx.setBattleStarted(true); //DebugF

        System.out.print("start\n");

    }

    public GameContext getCtx(){
        return  this.ctx;
    }

    public GameEngine getGameEngine(){
        return this.gameEngine;
    }

    public void advancedTimeCommand(double sec){
        if (this.gameEngine != null) {
            this.gameEngine.update(sec);
        }else {
            System.out.println("Game engine is null\n");
        }
    }


}
