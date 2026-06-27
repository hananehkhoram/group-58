package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import view.ConsoleView;

public class MenuShowCurrent implements Command {
    private MenuManager menuManager;

    public MenuShowCurrent(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] atgs ){

        Menu currentMenu = this.menuManager.getCurrentMenu();
        ConsoleView.showMessage(currentMenu.toString());

    }

    //menu show current
}
