package com.workshop.controller.commands.ProfileMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.ProfileMenu;
import com.workshop.view.Console;

public class ChangeUsername implements Command {
    private MenuManager menuManager;

    public ChangeUsername(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String username = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ProfileMenu){
            String result = ((ProfileMenu) currentMenu).changeUsername(username);
            DataManager.getInstance().saveUser();
            Console.showMessage("%s\n",result);

        }
    }

    //menu profile change-username -u <username>
}
