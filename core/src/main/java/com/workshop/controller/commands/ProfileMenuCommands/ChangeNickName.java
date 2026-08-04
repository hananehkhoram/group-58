package com.workshop.controller.commands.ProfileMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.ProfileMenu;
import com.workshop.view.Console;

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
            DataManager.getInstance().saveUser();
            Console.showMessage("%s\n",result);

        }
    }

    //menu profile change-username -u <username>
    //menu profile change-nickname -u <nickname>
    //menu profile change-email -e <email>
    //menu profile change-password -p <new_password> -o <old_password>
}

