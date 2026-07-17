package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;
import view.ConsoleView;

public class EnterTravelLog implements Command {

    private MenuManager menuManager;
    public EnterTravelLog(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu) {
            menuManager.changeMenu("travelmenu");
            ConsoleView.showMessage("You are now in travel menu");
        }
    }

    //menu travel-log
}
