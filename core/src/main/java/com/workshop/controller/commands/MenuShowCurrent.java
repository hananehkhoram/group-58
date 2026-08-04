package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.view.Console;

public class MenuShowCurrent implements Command {
    private MenuManager menuManager;

    public MenuShowCurrent(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] atgs ){

        Menu currentMenu = this.menuManager.getCurrentMenu();
        Console.showMessage(currentMenu.showMenu());

    }

    //menu show current
}
