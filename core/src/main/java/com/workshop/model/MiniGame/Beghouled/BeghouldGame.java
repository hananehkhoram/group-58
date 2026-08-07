package com.workshop.model.MiniGame.Beghouled;

import com.workshop.controller.MenuManager;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.season.Season;
import com.workshop.model.season.miniGameSeason.BeghouledSeason;
import com.workshop.view.Console;

import java.util.List;

public class BeghouldGame {
    private static final int[] TARGET_MATCHES = {10, 15, 20};

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;
    private BeghouledManager beghouledManager;

    public void start(MenuManager menuManager) {
        start(menuManager, 1);
    }

    public void start(MenuManager menuManager, int levelNumber) {
        List<Level> beghouledLevels =
            LevelFactory.buldBeghouledLevels();

        if (levelNumber < 1
            || levelNumber > beghouledLevels.size()) {

            Console.showMessage(
                "Beghouled level must be between 1 and "
                    + beghouledLevels.size()
                    + "."
            );
            return;
        }

        int levelIndex = levelNumber - 1;

        currentLevel = beghouledLevels.get(levelIndex);

        Season beghouledSeason =
            new BeghouledSeason(beghouledLevels);

        ctx = new GameContext(
            currentLevel,
            beghouledSeason
        );

        gameEngine = new GameEngine(
            ctx,
            menuManager
        );

        ctx.setGameEngine(gameEngine);

        int targetMatches =
            TARGET_MATCHES[levelIndex];

        beghouledManager = new BeghouledManager(
            ctx,
            gameEngine,
            targetMatches
        );

        ctx.setBeghouldManager(beghouledManager);

        beghouledManager.initBoard();

        ctx.setBattleStarted(true);

        Console.showMessage(
            "Beghouled level "
                + levelNumber
                + " started. Target matches: "
                + targetMatches
                + "."
        );
    }

    public void advancedTimeCommand(double seconds) {
        if (gameEngine == null || ctx == null) {
            Console.showMessage(
                "Game engine is null"
            );
            return;
        }

        int ticks = (int) (seconds * 10);

        if (ctx.getTimeManager() != null) {
            ctx.getTimeManager().advanceTime(ticks);
        }

        gameEngine.update(seconds);
    }

    public GameContext getCtx() {
        return ctx;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public BeghouledManager getBeghouledManager() {
        return beghouledManager;
    }
}
