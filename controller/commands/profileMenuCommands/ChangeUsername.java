package controller.commands.ProfileMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.ProfileMenu;
import view.ConsoleView;

public class ChangeUsername implements Command {
    private MenuManager menuManager;

    public ChangeUsername(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String username = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ProfileMenu){
            String result = ((ProfileMenu) currentMenu).changeUsername(username);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu profile change-username -u <username>
}
