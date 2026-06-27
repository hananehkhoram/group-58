package controller.commands.PlantsList;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.PlantSelectionMenu;
import view.ConsoleView;

public class RemovePlant implements Command {
    private MenuManager menuManager;

    public RemovePlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String plantType = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof PlantSelectionMenu){
            String result = ((PlantSelectionMenu) currentMenu).removePlant(plantType);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //remove plant -t <type>
}
