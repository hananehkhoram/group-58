package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import view.ConsoleView;

public class MenuEnter implements Command {

    private MenuManager menuManager;

    public MenuEnter(MenuManager menuManager){
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args){
        String targetMenu = args[0];

        menuManager.changeMenu(targetMenu);
        ConsoleView.showMessage("You entered %s.\n",targetMenu);

    }

    //menu enter <menu_name>
}
