package controller.commands.RegisterMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import model.menus.allmenus.RegisterMenu;
import view.ConsoleView;

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
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //register -u <username> -p <password> <password_confirm> -n <nickname> -e <email>
    //g <gender> p_9
}
