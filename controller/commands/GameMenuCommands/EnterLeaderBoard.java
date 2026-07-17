package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;

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
        }
    }

    //menu leaderboard
}
