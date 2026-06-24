package controller.commands.collectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import view.ConsoleView;

public class showZombiesCollection implements Command {
    private MenuManager menuManager;

    public showZombiesCollection(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof CollectionMenu){
            String result = ((CollectionMenu) currentMenu).showAllZombies();
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu collection show-zombies
    //menu collection show-all-zombies

}
