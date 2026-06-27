package controller.commands.CollectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import view.ConsoleView;

public class UpgradePlant implements Command {
    private MenuManager menuManager;

    public UpgradePlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String plantName = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof CollectionMenu){
            String result = ((CollectionMenu) currentMenu).upgradePlant(plantName);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu collection upgrade-plant -p <plant_name>
}
