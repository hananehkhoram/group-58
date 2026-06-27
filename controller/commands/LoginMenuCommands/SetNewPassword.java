package controller.commands.LoginMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import view.ConsoleView;

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
            ConsoleView.showMessage("%s\n", result);
        }
    }

    //new password -p <password>
}
