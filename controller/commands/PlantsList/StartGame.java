package controller.commands.PlantsList;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.PlantSelectionMenu;
import view.ConsoleView;

public class StartGame implements Command {
    private MenuManager menuManager;

    public StartGame(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof PlantSelectionMenu){
            if (menuManager.getCtx() == null) {
                ConsoleView.showMessage("No active battle to start.");
                return;
            }

            String result = ((PlantSelectionMenu) currentMenu).startGame();
            ConsoleView.showMessage(result);

            if (result.startsWith("Let's begin")) {
                menuManager.changeMenu("gamemenu");
            }
        }
    }
}
