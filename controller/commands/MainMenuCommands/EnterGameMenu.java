package controller.commands.MainMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.MainMenu;

public class EnterGameMenu implements Command {
    private MenuManager menuManager;

    public EnterGameMenu(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof MainMenu){
            menuManager.changeMenu("gamemenu");
        }
    }
}
