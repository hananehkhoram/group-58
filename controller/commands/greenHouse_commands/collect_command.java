package controller.commands.greenHouse_commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.CollectionMenu;
import model.menus.allmenus.GreenHouseMenu;
import view.ConsoleView;

public class collect_command implements Command {
    private MenuManager menuManager;

    public collect_command(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GreenHouseMenu) {
            result = ((GreenHouseMenu) currentMenu).collectPlant(x,y);
            ConsoleView.showMessage("%s\n",result);
        }
    }

    //collect (<x>, <y>)
}
