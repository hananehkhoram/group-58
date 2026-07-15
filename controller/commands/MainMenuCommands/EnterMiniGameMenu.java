package controller.commands.MainMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.MiniGame.Beghouled.Beghouled;
import model.MiniGame.Izambi.IzambiModel;
import model.MiniGame.VaseGame.Vase;
import model.MiniGame.WallnutsGame.WallnutBowlingGame;
import model.menus.BaseMenu;
import model.menus.Menu;
import model.menus.allmenus.MainMenu;
import model.menus.allmenus.TravelMenu;

public class EnterMiniGameMenu implements Command {
    private MenuManager menuManager;
    private GameContext gameContext;

    public EnterMiniGameMenu(MenuManager menuManager, GameContext gameContext) {
        this.menuManager = menuManager;
        this.gameContext = gameContext;
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
                        Vase vase = new Vase();
                        vase.startMiniGame();

                    case 2:
                        WallnutBowlingGame wBGame = new WallnutBowlingGame();
                        wBGame.start();

                    case 3:
                        IzambiModel izambiModel = new IzambiModel();
                        izambiModel.startMiniGame();

                    case 4:
                        Beghouled beghouled = new Beghouled();
                        beghouled.startMiniGame();

                }
            }
        }
    }
}
