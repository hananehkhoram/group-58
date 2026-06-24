package controller.commands.greenHouse_commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.GreenHouseMenu;
import view.ConsoleView;

public class faster_grow implements Command {
    private MenuManager menuManager;

    public faster_grow(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GreenHouseMenu) {
            result = ((GreenHouseMenu) currentMenu).growPlant(x,y);
            ConsoleView.showMessage("%s\n",result);
        }
    }

    //grow (<x>, <y>)
}
