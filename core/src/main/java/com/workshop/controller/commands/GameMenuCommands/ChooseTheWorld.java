package com.workshop.controller.commands.GameMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GameMenu;
import com.workshop.model.season.Season;
import com.workshop.view.Console;

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
            Season world = DataManager.getInstance().seasons.get(worldName);
            if (world == null) {
                Console.showMessage("Invalid world name.\n");
                return;
            }

            ((GameMenu) currentMenu).switchWorld(worldName);
        }

    }//choose world -w <worldName>
}
