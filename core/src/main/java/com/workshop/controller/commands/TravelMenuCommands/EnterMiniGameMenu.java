package com.workshop.controller.commands.TravelMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
//import model.MiniGame.Beghouled.BeghouledManager;
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

import java.util.List;

public class EnterMiniGameMenu implements Command {
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
                        List<Level> beghouledLevels = LevelFactory.buldBeghouledLevels();
                        Level currentLevel = beghouledLevels.get(0);
                        Season beghouledSeason = new BeghouledSeason(beghouledLevels);

                        GameContext newCtx = new GameContext(currentLevel, beghouledSeason);
                        GameEngine newEngine = new GameEngine(newCtx, menuManager);
                        newCtx.setGameEngine(newEngine);

                        BeghouledManager beghouled = new BeghouledManager(newCtx, newEngine, 10);
                        beghouled.initBoard();

                        newCtx.setBeghouldManager(beghouled);
                        newCtx.setBattleStarted(true);

                        menuManager.setCtx(newCtx);
                        menuManager.setGameEngine(newEngine);

                        ShowMap showMapCommand = new ShowMap(menuManager);
                        showMapCommand.execute(new String[]{});
                        break;

                }
            }
        }
    }
}
