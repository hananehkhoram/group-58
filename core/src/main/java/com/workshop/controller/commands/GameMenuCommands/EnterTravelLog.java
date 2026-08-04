package com.workshop.controller.commands.GameMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GameMenu;
import com.workshop.view.Console;

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
            Console.showMessage("You are now in travel menu");
        }
    }

    //menu travel-log
}
