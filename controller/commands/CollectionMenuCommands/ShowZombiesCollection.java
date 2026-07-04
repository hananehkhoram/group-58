package controller.commands.CollectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import view.ConsoleView;

public class ShowZombiesCollection implements Command {
    private MenuManager menuManager;

    public ShowZombiesCollection(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        String allOrAvailable = null;
        if (args[0] != null) allOrAvailable = args[0];

        if (currentMenu instanceof CollectionMenu){
            if (allOrAvailable != null){
                String result = ((CollectionMenu) currentMenu).showAllZombies();
                ConsoleView.showMessage("%s\n",result);
            }
            else {
                String result = ((CollectionMenu) currentMenu).showZombies();
                ConsoleView.showMessage("%s\n",result);
            }
        }
    }

    //menu collection show-zombies
    //menu collection show-all-zombies

}
