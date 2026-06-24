package controller.commands.profileMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import model.menus.allmenus.ProfileMenu;
import view.ConsoleView;

public class change_email implements Command {
    private MenuManager menuManager;

    public change_email(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String email = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ProfileMenu){
            String result = ((ProfileMenu) currentMenu).changeEmail(email);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu profile change-email -e <email>
}
