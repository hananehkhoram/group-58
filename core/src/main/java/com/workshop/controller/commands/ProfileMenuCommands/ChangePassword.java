package com.workshop.controller.commands.ProfileMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.ProfileMenu;
import com.workshop.view.Console;

public class ChangePassword implements Command {
    private MenuManager menuManager;

    public ChangePassword(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String newPassword = args[0];
        String oldPassword = args[1];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ProfileMenu){
            String result = ((ProfileMenu) currentMenu).changePassword(oldPassword,newPassword);
            DataManager.getInstance().saveUser();
            Console.showMessage("%s\n",result);

        }
    }

    //menu profile change-password -p <new_password> -o <old_password>
}
