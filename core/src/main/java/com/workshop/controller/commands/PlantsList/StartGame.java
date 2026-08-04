package com.workshop.controller.commands.PlantsList;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.PlantSelectionMenu;
import com.workshop.view.Console;

public class StartGame implements Command {
    private MenuManager menuManager;

    public StartGame(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof PlantSelectionMenu){
            if (menuManager.getCtx() == null) {
                Console.showMessage("No active battle to start.");
                return;
            }

            String result = ((PlantSelectionMenu) currentMenu).startGame();
            Console.showMessage(result);

            if (result.startsWith("Let's begin")) {
                menuManager.forceChangeMenu("gamemenu");
            }
        }
    }//start game
}
