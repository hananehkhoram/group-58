package controller.commands.TravelMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
//import model.MiniGame.Beghouled.BeghouledManager;
import controller.commands.Status.ShowMap;
import controller.repository.factory.LevelFactory;
import model.GameContext;
import model.MiniGame.Beghouled.BeghouledManager;
import model.MiniGame.Izambi.Izambi;
import model.MiniGame.VaseGame.Vasecheccker;
import model.MiniGame.WallnutsGame.WallnutBowlingGame;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.menus.allmenus.TravelMenu;
import model.season.Season;
import model.season.miniGameSeason.BeghouledSeason;

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
                view.ConsoleView.simplePrint("Choose your miniGame :\n1: Vasebreaker\n2: Wallnut Bowling\n3: (i, zombie\n4:Beghouled\n");

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
                        Izambi izambiModel = new Izambi(menuManager);
                        izambiModel.startMiniGame();
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