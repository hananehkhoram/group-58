package com.workshop.controller.commands.PlantsList;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.PlantSelectionMenu;
import com.workshop.view.Console;

public class BoostPlant implements Command {
    private MenuManager menuManager;

    public BoostPlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String plantType = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof PlantSelectionMenu){
            String result = ((PlantSelectionMenu) currentMenu).boostPlant(plantType);
            Console.showMessage("%s\n",result);

        }
    }

    //boost plant -t <type>
}
