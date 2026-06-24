package controller.commands.plantsList;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.PlantSelectionMenu;
import view.ConsoleView;

public class boostPlant implements Command {
    private MenuManager menuManager;

    public boostPlant (MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String plantType = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof PlantSelectionMenu){
            String result = ((PlantSelectionMenu) currentMenu).boostPlant(plantType);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //boost plant -t <type>
}
