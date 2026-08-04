package com.workshop.controller.commands.ProfileMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.ProfileMenu;
import com.workshop.view.Console;

public class ShowInfo implements Command {
    private MenuManager menuManager;

    public ShowInfo(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ProfileMenu){
            String result = ((ProfileMenu) currentMenu).showInfo();
            Console.showMessage("%s\n",result);

        }
    }

    //menu profile show-info
}
