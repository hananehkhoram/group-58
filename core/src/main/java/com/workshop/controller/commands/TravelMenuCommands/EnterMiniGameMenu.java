package com.workshop.controller.commands.TravelMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.commands.Status.ShowMap;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Beghouled.BeghouledManager;
import com.workshop.model.MiniGame.Izambi.Izambi;
import com.workshop.model.MiniGame.VaseGame.Vasecheccker;
import com.workshop.model.MiniGame.WallnutsGame.WallnutBowlingGame;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.menus.allmenus.TravelMenu;
import com.workshop.model.season.Season;
import com.workshop.model.season.miniGameSeason.BeghouledSeason;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;
import com.workshop.controller.repository.DataManager;

import java.util.List;

public class EnterMiniGameMenu implements Command {
    private static final int[] BEGHOULED_TARGETS = {
        10,
        15,
        20
    };

    private MenuManager menuManager;

    public EnterMiniGameMenu(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {

        String whichCommand = args[0];

        if (menuManager.getCurrentMenu() instanceof TravelMenu){
            if (whichCommand.equals("enter")){
                com.workshop.view.Console.simplePrint("Choose your miniGame :\n1: Vasebreaker\n2: Wallnut Bowling\n3: (i, zombie\n4:Beghouled\n");

            }else {
                int number = Integer.parseInt(whichCommand);

                switch (number){
                    case 1:
                        Vasecheccker vaseGame = new Vasecheccker();
                        vaseGame.startMiniGame(menuManager);
                        menuManager.setCtx(vaseGame.getCtx());
                        menuManager.setGameEngine(vaseGame.getGameEngine());
                        break;

                    case 2:
                        WallnutBowlingGame wBGame = new WallnutBowlingGame();
                        wBGame.start();
                        menuManager.setCtx(wBGame.getCtx());
                        menuManager.setGameEngine(wBGame.getGameEngine());
                        break;

                    case 3:
                        Izambi izambiModel = new Izambi();
                        izambiModel.startMiniGame(menuManager);
                        menuManager.setCtx(izambiModel.getCtx());
                        menuManager.setGameEngine(izambiModel.getGameEngine());
                        break;


                    case 4:
                        startBeghouled();
                        break;

                }
            }
        }
    }

    private void startBeghouled() {
        List<Level> levels =
            LevelFactory.buldBeghouledLevels();

        User currentUser =
            UserManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            Console.showMessage(
                "You must login before starting Beghouled."
            );
            return;
        }

        if (levels.isEmpty()) {
            Console.showMessage(
                "No Beghouled levels were found."
            );
            return;
        }

        String firstLevelName =
            levels.get(0).getName();

        if (!currentUser.isLevelUnlocked(firstLevelName)) {
            currentUser.unlockLevel(firstLevelName);
            DataManager.getInstance().saveUser();
        }

        int levelIndex =
            findLatestUnlockedBeghouledLevel(
                currentUser,
                levels
            );

        if (levelIndex < 0) {
            Console.showMessage(
                "Beghouled is still locked."
            );
            return;
        }

        Level currentLevel = levels.get(levelIndex);

        Season beghouledSeason =
            new BeghouledSeason(levels);

        GameContext newCtx =
            new GameContext(
                currentLevel,
                beghouledSeason
            );

        GameEngine newEngine =
            new GameEngine(
                newCtx,
                menuManager
            );

        newCtx.setGameEngine(newEngine);

        BeghouledManager manager =
            new BeghouledManager(
                newCtx,
                newEngine,
                BEGHOULED_TARGETS[levelIndex]
            );

        newCtx.setBeghouldManager(manager);

        manager.initBoard();

        newCtx.setBattleStarted(true);

        menuManager.setCtx(newCtx);
        menuManager.setGameEngine(newEngine);

        Console.showMessage(
            "Beghouled level "
                + (levelIndex + 1)
                + " started. Target matches: "
                + BEGHOULED_TARGETS[levelIndex]
                + "."
        );

        ShowMap showMapCommand =
            new ShowMap(menuManager);

        showMapCommand.execute(new String[]{});
    }

    private int findLatestUnlockedBeghouledLevel(
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
}
