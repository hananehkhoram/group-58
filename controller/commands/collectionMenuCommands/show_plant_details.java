package controller.commands.collectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import controller.repository.PlantRepository;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import model.plants.Plant;
import view.ConsoleView;

public class show_plant_details implements Command {

    private MenuManager menuManager;

    public show_plant_details(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String plantName = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof CollectionMenu){
            String result = ((CollectionMenu) currentMenu).showPlantDetails(plantName);
            ConsoleView.showMessage("%s\n",result);

        }

    }

    //menu collection show-plant -p <plant_name>
}
