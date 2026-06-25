package controller.commands.GreenHouseCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.GreenHouseMenu;
import view.ConsoleView;

public class PlantPot implements Command {
    private MenuManager menuManager;

    public PlantPot(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GreenHouseMenu) {
            result = ((GreenHouseMenu) currentMenu).plantPot(x,y);
            ConsoleView.showMessage("%s\n",result);
        }
    }

    //plant pot at (<x>, <y>)
}
