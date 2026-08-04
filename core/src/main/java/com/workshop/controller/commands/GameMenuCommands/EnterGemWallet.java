package com.workshop.controller.commands.GameMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GameMenu;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

public class EnterGemWallet implements Command {
    private MenuManager menuManager;

    public EnterGemWallet(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu){
            Console.showMessage("Your gems: %d\n", UserManager.getInstance().getCurrentUser().getGems());
        }

    }

    //menu gem-wallet
}
