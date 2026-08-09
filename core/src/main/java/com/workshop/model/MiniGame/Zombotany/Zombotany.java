package com.workshop.model.MiniGame.Zombotany;

import com.workshop.controller.MenuManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.season.Season;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

import java.util.List;

public class Zombotany {

    private GameEngine gameEngine;
    private GameContext ctx;

    public void startMiniGame(MenuManager menuManager) {
        User currentUser =
            UserManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            Console.showMessage(
                "You must login before starting Zombotany."
            );
            return;
        }

        Season season =
            DataManager.getInstance()
                .seasons
                .get("Zombotany");

        if (season == null) {
            Console.showMessage(
                "Zombotany season was not found."
            );
            return;
        }

        List<Level> levels = season.getLevels();

        if (levels == null || levels.isEmpty()) {
            Console.showMessage(
                "No Zombotany levels were found."
            );
            return;
        }

        int levelIndex =
            findLatestUnlockedLevel(
                currentUser,
                levels
            );

        if (levelIndex < 0) {
            Console.showMessage(
                "Zombotany is locked."
            );
            return;
        }

        startLevel(
            menuManager,
            levelIndex
        );
    }

    public void startLevel(
        MenuManager menuManager,
        int levelIndex) {

        Season season =
            DataManager.getInstance()
                .seasons
                .get("Zombotany");

        if (season == null) {
            Console.showMessage(
                "Zombotany season was not found."
            );
            return;
        }

        List<Level> levels = season.getLevels();

        if (levelIndex < 0
            || levelIndex >= levels.size()) {
            Console.showMessage(
                "Invalid Zombotany level."
            );
            return;
        }

        User currentUser =
            UserManager.getInstance().getCurrentUser();

        if (currentUser == null
            || !currentUser.isLevelUnlocked(
            levels.get(levelIndex).getName()
        )) {
            Console.showMessage(
                "This Zombotany level is locked."
            );
            return;
        }

        Level level = levels.get(levelIndex);

        ctx = new GameContext(
            level,
            season
        );

        gameEngine =
            new GameEngine(
                ctx,
                menuManager
            );

        ctx.setGameEngine(gameEngine);

        menuManager.setCtx(ctx);
        menuManager.setGameEngine(gameEngine);

        menuManager.forceChangeMenu(
            "plantselectionmenu"
        );

        Console.showMessage(
            "Entering Zombotany level "
                + (levelIndex + 1)
                + ". Choose your plants."
        );
    }

    private int findLatestUnlockedLevel(
        User user,
        List<Level> levels
    ) {
        for (int index = levels.size() - 1;
             index >= 0;
             index--) {

            String levelName =
                levels.get(index).getName();

            if (user.isLevelUnlocked(levelName)) {
                return index;
            }
        }

        return -1;
    }

    public GameContext getCtx() {
        return ctx;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }
}
