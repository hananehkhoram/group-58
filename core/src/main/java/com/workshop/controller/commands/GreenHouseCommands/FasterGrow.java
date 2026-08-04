package com.workshop.controller.commands.GreenHouseCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GreenHouseMenu;
import com.workshop.view.Console;

public class FasterGrow implements Command {
    private MenuManager menuManager;

    public FasterGrow(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GreenHouseMenu) {
            result = ((GreenHouseMenu) currentMenu).growPlant(x,y);
            Console.showMessage("%s\n",result);
        }
    }

    //grow (<x>, <y>)
}
