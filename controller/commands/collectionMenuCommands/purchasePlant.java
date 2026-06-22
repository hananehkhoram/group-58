package controller.commands.collectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;

public class purchasePlant implements Command {

    private MenuManager menuManager;

    public purchasePlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String plantName = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof CollectionMenu) {
            ((CollectionMenu) currentMenu).purchasePlant(plantName);
        }
    }

    //menu collection purchase-plant -p <plant_name>
}
