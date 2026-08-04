package com.workshop.controller.commands.MainMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.MainMenu;
import com.workshop.view.Console;

public class Logout implements Command {
    private MenuManager menuManager;

    public Logout(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof MainMenu){
            menuManager.forceChangeMenu("registermenu");
            Console.showMessage("You are now in register menu.");
        }
    }

    //menu logout
}
