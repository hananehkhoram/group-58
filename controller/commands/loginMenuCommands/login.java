package controller.commands.loginMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import model.menus.allmenus.LoginMenu;
import view.ConsoleView;

public class login implements Command {
    private MenuManager menuManager;

    public login(MenuManager menuManager) {
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
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //login -u <username> -p <password> -stay-logged-in p_12
}
