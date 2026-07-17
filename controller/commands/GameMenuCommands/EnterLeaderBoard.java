package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;
import view.ConsoleView;

public class EnterLeaderBoard implements Command {
    private MenuManager menuManager;
    private GameContext gameContext;

    public EnterLeaderBoard(MenuManager menuManager, GameContext gameContext) {
        this.menuManager = menuManager;
        this.gameContext = gameContext;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu){
            menuManager.changeMenu("LeaderBoardMenu");
            ConsoleView.showMessage("You are now in leader board menu");
        }
    }

    //menu leaderboard
}
