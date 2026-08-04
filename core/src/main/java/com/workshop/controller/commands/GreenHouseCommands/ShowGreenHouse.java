package com.workshop.controller.commands.GreenHouseCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GreenHouseMenu;
import com.workshop.view.Console;

public class ShowGreenHouse implements Command {
    private MenuManager menuManager;

    public ShowGreenHouse(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GreenHouseMenu) {
            result = ((GreenHouseMenu) currentMenu).showGreenHouse();
            Console.showMessage("%s\n",result);
        }
    }

    //show greenhouse
}
