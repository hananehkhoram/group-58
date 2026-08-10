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
        startMiniGame(menuManager, 1);
    }

    public void startMiniGame(MenuManager menuManager, int levelNumber) {
        List<Level> vaseLevels = LevelFactory.buildVaseLevels();

        if (levelNumber < 1 || levelNumber > vaseLevels.size()) {
            levelNumber = 1;
        }

        this.currentLevel = vaseLevels.get(levelNumber - 1);

        Season vaseSeason = new VaseSeason(vaseLevels);

        this.ctx = new GameContext(this.currentLevel, vaseSeason);
        ctx.setZombieFactory(
            new com.workshop.controller.repository.factory.ZombieFactory(
                DataManager.getInstance()
            )
        );

        this.gameEngine = new GameEngine(this.ctx, menuManager);
        this.ctx.setGameEngine(this.gameEngine);

        LevelFactory.setUpVases(this.ctx);

        ctx.setBattleStarted(true);

        System.out.print("start\n");
    }

    public GameContext getCtx(){
        return  this.ctx;
    }

    public GameEngine getGameEngine(){
        return this.gameEngine;
    }


}
