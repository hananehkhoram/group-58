package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;

public class MenuShowCurrent implements Command {
    private MenuManager menuManager;

    public MenuShowCurrent(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] atgs ){

        Menu currentMenu = this.menuManager.getCurrentMenu();
        System.out.println("Current Menu: " + currentMenu);

    }

    //menu show current
}
