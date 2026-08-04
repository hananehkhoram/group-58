package com.workshop.controller.commands.GreenHouseCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GreenHouseMenu;
import com.workshop.view.Console;

public class PlantPot implements Command {
    private MenuManager menuManager;

    public PlantPot(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GreenHouseMenu) {
            result = ((GreenHouseMenu) currentMenu).plantPot(x,y);
            Console.showMessage("%s\n",result);
        }
    }

    //plant pot at (<x>, <y>)
}
