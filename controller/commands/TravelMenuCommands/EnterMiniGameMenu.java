package controller.commands.TravelMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.MiniGame.Beghouled.Beghouled;
import model.MiniGame.Izambi.Izambi;
import model.MiniGame.VaseGame.Vase;
import model.MiniGame.WallnutsGame.WallnutBowlingGame;
import model.menus.allmenus.TravelMenu;
import view.ConsoleView;

public class EnterMiniGameMenu implements Command {
    private MenuManager menuManager;

    public EnterMiniGameMenu(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String whichCommand = args[0];

        if (!(menuManager.getCurrentMenu() instanceof TravelMenu)) {
            return;
        }

        if (whichCommand.equals("enter")) {
            ConsoleView.simplePrint("Choose your miniGame :\n1: Vasebreaker\n2: Wallnut Bowling\n3: I, Zombie\n4: Beghouled\n");
            return;
        }

        int number = Integer.parseInt(whichCommand);
        switch (number) {
            case 1 -> {
                Vase vase = new Vase();
                vase.startMiniGame();
                menuManager.setCtx(vase.getCtx());
                menuManager.setGameEngine(vase.getGameEngine());
            }
            case 2 -> {
                WallnutBowlingGame wBGame = new WallnutBowlingGame();
                wBGame.start();
                menuManager.setCtx(wBGame.getCtx());
                menuManager.setGameEngine(wBGame.getGameEngine());
            }
            case 3 -> {
                Izambi izambiModel = new Izambi();
                izambiModel.startMiniGame();
                menuManager.setCtx(izambiModel.getCtx());
                menuManager.setGameEngine(izambiModel.getGameEngine());
            }
            case 4 -> {
                Beghouled beghouled = new Beghouled();
                beghouled.startMiniGame();
                menuManager.setCtx(beghouled.getCtx());
                menuManager.setGameEngine(beghouled.getGameEngine());
            }
            default -> ConsoleView.showMessage("Invalid minigame number.");
        }
    }
}