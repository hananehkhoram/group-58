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

    public void start(MenuManager menuManager) {
        start(menuManager, 1);
    }

    public void start(MenuManager menuManager, int levelNumber) {
        List<Level> bowlingLevels = LevelFactory.buildWallnutsLevels();

        if (levelNumber < 1 || levelNumber > bowlingLevels.size()) {
            levelNumber = 1;
        }

        this.currentLevel = bowlingLevels.get(levelNumber - 1);

        Season bowlingSeason = new WallnutsSeason(bowlingLevels);

        this.ctx = new GameContext(this.currentLevel, bowlingSeason);
        this.gameEngine = new GameEngine(this.ctx, menuManager);
        this.ctx.setGameEngine(this.gameEngine);

        this.ctx.setBattleStarted(true);

        System.out.print("start\n");
    }

    public GameContext getCtx(){
        return this.ctx;
    }

    public GameEngine getGameEngine(){
        return this.gameEngine;
    }

}
