package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.MenuType;
import com.workshop.view.Console;

public class MenuExit implements Command {
    private MenuManager menuManager;

    public MenuExit(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu current = menuManager.getCurrentMenu();
        MenuType type = current.getMenu();

        switch (type) {
            case REGISTER:
                Console.showMessage("Goodbye!");
                System.exit(0);
                break;
            case MAIN:
                Console.showMessage("You must use 'menu logout' to leave the main menu.");
                break;
            default:
                MenuType target = MenuManager.getExitTarget(type);
                if (target == null) {
                    Console.showMessage("You can't exit from here.");
                } else {
                    menuManager.forceChangeMenu(target.name().toLowerCase() + "menu");
                    Console.showMessage("You are now in %s menu.\n",target.name());
                }
        }
    }//menu exit
}
