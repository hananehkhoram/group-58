package controller.commands.CollectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import model.menus.allmenus.PlantSelectionMenu;
import view.ConsoleView;

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
                ConsoleView.showMessage("%s\n",result);
            }
            else {
                String result = ((CollectionMenu) currentMenu).showPlants();
                ConsoleView.showMessage("%s\n",result);
            }
        }
    }

    //menu collection show-plants
    //menu collection show-all-plants
}
