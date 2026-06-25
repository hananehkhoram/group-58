package controller.commands.GreenHouseCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.GreenHouseMenu;
import view.ConsoleView;

public class ShowGreenHouse implements Command {
    private MenuManager menuManager;

    public ShowGreenHouse(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String result = null;

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GreenHouseMenu) {
            result = ((GreenHouseMenu) currentMenu).showGreenHouse();
            ConsoleView.showMessage("%s\n",result);
        }
    }

    //show greenhouse
}
