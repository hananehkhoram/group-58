package com.workshop.controller.commands.LoginMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.LoginMenu;
import com.workshop.view.Console;

public class ForgetPassword implements Command {
    private MenuManager menuManager;

    public ForgetPassword(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String username = args[0];
        String email = args[1];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof LoginMenu) {
            LoginMenu loginMenu = (LoginMenu) currentMenu;

            String result = loginMenu.startForgetPasswordProcess(username, email);
            Console.showMessage("%s\n", result);
        }
    }

    //forget password -u <username> -e <email>
}
