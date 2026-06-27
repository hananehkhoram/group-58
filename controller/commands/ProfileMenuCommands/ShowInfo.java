package controller.commands.ProfileMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.ProfileMenu;
import view.ConsoleView;

public class ShowInfo implements Command {
    private MenuManager menuManager;

    public ShowInfo(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ProfileMenu){
            String result = ((ProfileMenu) currentMenu).showInfo();
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu profile show-info
}
