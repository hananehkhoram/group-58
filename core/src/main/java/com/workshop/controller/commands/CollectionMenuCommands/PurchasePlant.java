package com.workshop.controller.commands.CollectionMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.view.Console;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.CollectionMenu;

public class PurchasePlant implements Command {

    private MenuManager menuManager;

    public PurchasePlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String plantName = args[0];
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof CollectionMenu) {
            result = ((CollectionMenu) currentMenu).purchasePlant(plantName);
            Console.showMessage("%s\n",result);
        }
    }

    //menu collection purchase-plant -p <plant_name>
}
