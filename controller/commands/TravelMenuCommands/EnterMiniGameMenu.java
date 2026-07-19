package controller.commands.TravelMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.MiniGame.Beghouled.Beghouled;
import model.MiniGame.Izambi.Izambi;
import model.MiniGame.VaseGame.Vasecheccker;
import model.MiniGame.WallnutsGame.WallnutBowlingGame;
import model.menus.allmenus.TravelMenu;

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
                        Vasecheccker vasecheccker = new Vasecheccker();
                        vasecheccker.startMiniGame();
                        menuManager.setCtx(vasecheccker.getCtx());
                        menuManager.setGameEngine(vasecheccker.getGameEngine());
                        break;

                    case 2:
                        WallnutBowlingGame wBGame = new WallnutBowlingGame();
                        wBGame.start();
                        menuManager.setCtx(wBGame.getCtx());
                        menuManager.setGameEngine(wBGame.getGameEngine());
                        break;

                    case 3:
                        Izambi izambiModel = new Izambi();
                        izambiModel.startMiniGame();

                    case 4:
                        Beghouled beghouled = new Beghouled();
                        beghouled.startMiniGame();

                }
            }
        }
    }
}
