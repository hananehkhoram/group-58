package controller.commands.collectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import view.ConsoleView;

public class ShowPlantsCollection implements Command {
    private MenuManager menuManager;

    public ShowPlantsCollection(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof CollectionMenu){
            String result = ((CollectionMenu) currentMenu).showAllPlants();
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu collection show-plants
    //menu collection show-all-plants
    //menu collection show-zombies
    //menu collection show-all-zombies
    //menu collection show-plant -p <plant_name>
    //menu collection show-zombie -z <zombie_name>
    //show all plants
    //show available plants
}
