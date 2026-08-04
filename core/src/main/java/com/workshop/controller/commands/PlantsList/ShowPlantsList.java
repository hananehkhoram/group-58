package com.workshop.controller.commands.PlantsList;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.PlantSelectionMenu;
import com.workshop.view.Console;

public class ShowPlantsList implements Command {
    private MenuManager menuManager;

    public ShowPlantsList(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String allOrAvailable = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof PlantSelectionMenu){
            if (allOrAvailable.equalsIgnoreCase("all")){
                String result = ((PlantSelectionMenu) currentMenu).showAllPlants();
                Console.showMessage("%s\n",result);
            }
            else {
                String result = ((PlantSelectionMenu) currentMenu).showAvailablePlants();
                Console.showMessage("%s\n",result);
            }

        }
    }

    //show all plants
    //show available plants
}
