package controller.commands.MainMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import model.menus.allmenus.MainMenu;
import view.ConsoleView;

public class Logout implements Command {
    private MenuManager menuManager;

    public Logout(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof MainMenu){
            menuManager.forceChangeMenu("registermenu");
            ConsoleView.showMessage("You are now in register menu.");
        }
    }

    //menu logout
}
