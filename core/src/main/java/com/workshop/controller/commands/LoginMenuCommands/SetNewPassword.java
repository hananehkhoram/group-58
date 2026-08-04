package com.workshop.controller.commands.LoginMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.LoginMenu;
import com.workshop.view.Console;

public class SetNewPassword implements Command {
    private MenuManager menuManager;

    public SetNewPassword(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String newPassword = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof LoginMenu) {
            LoginMenu loginMenu = (LoginMenu) currentMenu;

            String result = loginMenu.updatePassword(newPassword);
            Console.showMessage("%s\n", result);
        }
    }

    //new password -p <password>
}
