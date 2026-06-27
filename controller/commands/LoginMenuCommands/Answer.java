package controller.commands.LoginMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import view.ConsoleView;

public class Answer implements Command {
    private MenuManager menuManager;

    public Answer(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String answer = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof LoginMenu){
            String result = ((LoginMenu) currentMenu).answerSecurityQuestion(answer);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //answer -a <answer>
}
