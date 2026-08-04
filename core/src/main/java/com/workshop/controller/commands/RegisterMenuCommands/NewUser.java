package com.workshop.controller.commands.RegisterMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.RegisterMenu;
import com.workshop.view.Console;

public class NewUser implements Command {
    private MenuManager menuManager;

    public NewUser(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String username = args[0];
        String password = args[1];
        String passwordConfirm = args[2];
        String nickName = args[3];
        String email = args[4];
        String gender = args[5];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof RegisterMenu){
            String result = ((RegisterMenu) currentMenu).register(username,password,
                    passwordConfirm, nickName,email,gender);
            Console.showMessage("%s\n",result);

        }
    }

    //register -u <username> -p <password> <password_confirm> -n <nickname> -e <email>
    //g <gender> p_9
}
