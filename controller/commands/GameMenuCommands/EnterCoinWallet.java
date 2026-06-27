package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;

public class EnterCoinWallet implements Command {
    private MenuManager menuManager;

    public EnterCoinWallet(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu){

        }

    }

    //menu coin-wallet
}
