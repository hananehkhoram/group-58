package com.workshop.controller.commands.LoginMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.LoginMenu;
import com.workshop.view.Console;

public class Login implements Command {
    private MenuManager menuManager;

    public Login(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String stayLoggedIn = null;
        String username = args[0];
        String password = args[1];
        if (args[2] != null){
            stayLoggedIn = args[2];
        }
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof LoginMenu){
            String result = ((LoginMenu) currentMenu).login(username,password,stayLoggedIn);
            Console.showMessage("%s\n",result);
            if (result.startsWith("Logged in")) {
                menuManager.forceChangeMenu("mainmenu");
                Console.showMessage("You are now in Main menu.\n");
            }

        }
    }

    //login -u <username> -p <password> -stay-logged-in p_12
}
