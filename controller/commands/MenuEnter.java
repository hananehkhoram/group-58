package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;

public class MenuEnter implements Command {

    private MenuManager menuManager;

    public MenuEnter(MenuManager menuManager){
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args){
        String targetMenu = args[0];

        menuManager.changeMenu(targetMenu);

    }

    //menu enter <menu_name>
}
