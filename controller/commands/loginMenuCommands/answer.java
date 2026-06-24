package controller.commands.loginMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import view.ConsoleView;

public class answer implements Command {
    private MenuManager menuManager;

    public answer(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String answer = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof LoginMenu){
            String result = ((LoginMenu) currentMenu).answerSecurityQuestion()
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //answer -a <answer>
}
