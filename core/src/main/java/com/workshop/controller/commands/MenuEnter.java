package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.view.Console;

public class MenuEnter implements Command {

    private MenuManager menuManager;

    public MenuEnter(MenuManager menuManager){
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args){
        String targetMenu = args[0];

        menuManager.changeMenu(targetMenu);
        Console.showMessage("You entered %s.\n",targetMenu);

    }

    //menu enter <menu_name>
}
