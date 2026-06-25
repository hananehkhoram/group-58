package controller.commands.collectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import view.ConsoleView;

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
            ConsoleView.showMessage("%s\n",result);

        }

    }

    //menu collection show-plant -p <plant_name>
}
