package com.workshop.model.MiniGame.WallnutsGame;

import com.workshop.controller.MenuManager;
import com.workshop.controller.SpecialLevelManager.ConveyorBeltManager;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.season.Season;
import com.workshop.model.season.miniGameSeason.WallnutsSeason;
import com.workshop.view.Console;

import java.util.List;

public class WallnutBowlingGame {
    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    public WallnutBowlingGame() {
    }

    public void start() {
        List<Level> bowlingLevels = LevelFactory.buildWallnutsLevels();
        this.currentLevel = bowlingLevels.get(0);

        Season bowlingSeason = new WallnutsSeason(bowlingLevels);

        this.ctx = new GameContext(this.currentLevel, bowlingSeason);
        this.gameEngine = new GameEngine(this.ctx, new MenuManager(ctx));
        this.ctx.setGameEngine(this.gameEngine);

        this.ctx.setLevelManager(new ConveyorBeltManager());

        this.ctx.setBattleStarted(true);

        System.out.print("start\n");
    }

    public GameContext getCtx(){
        return this.ctx;
    }

    public GameEngine getGameEngine(){
        return this.gameEngine;
    }

    public void advancedTimeCommand(double sec){
        if (this.gameEngine != null && this.ctx != null) {
            int ticks = (int) (sec * 10);

            if (this.ctx.getTimeManager() != null) {
                this.ctx.getTimeManager().advanceTime(ticks);
            }
            this.gameEngine.update(sec);
        } else {
            Console.showMessage("Game engine is null");
        }
    }
}
