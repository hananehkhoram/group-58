package controller.commands.collectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import view.ConsoleView;

public class purchasePlant implements Command {

    private MenuManager menuManager;

    public purchasePlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String plantName = args[0];
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof CollectionMenu) {
            result = ((CollectionMenu) currentMenu).purchasePlant(plantName);
            ConsoleView.showMessage("%s\n",result);
        }
    }

    //menu collection purchase-plant -p <plant_name>
}
