package controller.commands.MainMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.MainMenu;

public class EnterSettingsMenu implements Command {
    private MenuManager menuManager;

    public EnterSettingsMenu(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof MainMenu){
            menuManager.changeMenu("settingsmenu");
        }
    }
}
