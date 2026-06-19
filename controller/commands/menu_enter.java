package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;

public class menu_enter implements Command {

    private MenuManager menuManager;

    public menu_enter(MenuManager menuManager){
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args){
        String targetMenu = args[0];

        menuManager.changeMenu(targetMenu);

    }

    //menu enter <menu_name>
}
