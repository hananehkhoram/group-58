package controller.commands.ProfileMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.ProfileMenu;
import view.ConsoleView;

public class ChangeNickName implements Command {
    private MenuManager menuManager;

    public ChangeNickName(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String nickName = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ProfileMenu){
            String result = ((ProfileMenu) currentMenu).changeNickname(nickName);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu profile change-username -u <username>
    //menu profile change-nickname -u <nickname>
    //menu profile change-email -e <email>
    //menu profile change-password -p <new_password> -o <old_password>
}

