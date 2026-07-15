package controller.commands.ProfileMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import controller.repository.DataManager;
import model.menus.Menu;
import model.menus.allmenus.ProfileMenu;
import view.ConsoleView;

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
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu profile change-password -p <new_password> -o <old_password>
}
