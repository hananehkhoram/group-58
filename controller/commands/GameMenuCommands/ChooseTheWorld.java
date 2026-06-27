package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;

public class ChooseTheWorld implements Command {

    private MenuManager menuManager;

    public ChooseTheWorld(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String worldName = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu){
            //((GameMenu) currentMenu).ChooseTheWorld(worldName);  the method in the menu
        }

    }
}
