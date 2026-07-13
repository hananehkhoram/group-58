package controller.commands.LoginMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import view.ConsoleView;

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
            ConsoleView.showMessage("%s\n",result);
            menuManager.forceChangeMenu("mainmenu");

        }
    }

    //login -u <username> -p <password> -stay-logged-in p_12
}
