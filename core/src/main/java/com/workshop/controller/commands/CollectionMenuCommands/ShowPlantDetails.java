package com.workshop.controller.commands.CollectionMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.CollectionMenu;
import com.workshop.view.Console;

public class ShowPlantDetails implements Command {

    private MenuManager menuManager;

    public ShowPlantDetails(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String plantName = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof CollectionMenu){
            String result = ((CollectionMenu) currentMenu).showPlantDetails(plantName);
            Console.showMessage("%s\n",result);

        }

    }

    //menu collection show-plant -p <plant_name>
}
