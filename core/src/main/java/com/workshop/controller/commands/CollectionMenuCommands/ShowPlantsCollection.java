package com.workshop.controller.commands.CollectionMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.view.Console;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.CollectionMenu;

public class ShowPlantsCollection implements Command {
    private MenuManager menuManager;

    public ShowPlantsCollection(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();
        String allOrAvailable = null;
        if (args[0] != null) allOrAvailable = args[0];

        if (currentMenu instanceof CollectionMenu){
            if (allOrAvailable != null){
                String result = ((CollectionMenu) currentMenu).showAllPlants();
                Console.showMessage("%s\n",result);
            }
            else {
                String result = ((CollectionMenu) currentMenu).showPlants();
                Console.showMessage("%s\n",result);
            }
        }
    }

    //menu collection show-plants
    //menu collection show-all-plants
}
