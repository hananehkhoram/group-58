package com.workshop.controller.commands.ProfileMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.ProfileMenu;
import com.workshop.view.Console;

public class ChangeEmail implements Command {
    private MenuManager menuManager;

    public ChangeEmail(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String email = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ProfileMenu){
            String result = ((ProfileMenu) currentMenu).changeEmail(email);
            DataManager.getInstance().saveUser();
            Console.showMessage("%s\n",result);

        }
    }

    //menu profile change-email -e <email>
}
