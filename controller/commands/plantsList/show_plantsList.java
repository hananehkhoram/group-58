package controller.commands.plantsList;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.PlantSelectionMenu;
import view.ConsoleView;

public class show_plantsList implements Command {
    private MenuManager menuManager;

    public show_plantsList (MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String allOrAvailable = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof PlantSelectionMenu){
            if (allOrAvailable.equalsIgnoreCase("all")){
                String result = ((PlantSelectionMenu) currentMenu).showAllPlants();
                ConsoleView.showMessage("%s\n",result);
            }
            else {
                String result = ((PlantSelectionMenu) currentMenu).showAvailablePlants();
                ConsoleView.showMessage("%s\n",result);
            }

        }
    }

    //show all plants
    //show available plants
}
