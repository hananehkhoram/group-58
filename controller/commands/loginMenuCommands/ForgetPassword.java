package controller.commands.loginMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import view.ConsoleView;

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

        if (currentMenu instanceof LoginMenu){
            String result = ((LoginMenu) currentMenu).forgetPassword(username,email);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //forget password -u <username> -e <email>
}
