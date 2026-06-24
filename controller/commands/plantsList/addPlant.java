package controller.commands.plantsList;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import model.menus.allmenus.PlantSelectionMenu;
import view.ConsoleView;

public class addPlant implements Command {
    private MenuManager menuManager;

    public addPlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String plantType = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof PlantSelectionMenu){
            String result = ((PlantSelectionMenu) currentMenu).addPlant(plantType);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //add plant -t <type>
}
