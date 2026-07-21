package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import controller.repository.DataManager;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;
import model.season.Season;
import view.ConsoleView;

public class ChooseTheWorld implements Command {

    private MenuManager menuManager;

    public ChooseTheWorld(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String worldName = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu){
            Season world = DataManager.getInstance().seasons.get(worldName);
            if (world == null) {
                ConsoleView.showMessage("Invalid world name.\n");
                return;
            }

            ((GameMenu) currentMenu).switchWorld(worldName);
        }

    }//menu enter chapter -c <chaptername> (-l <levelNumber>)
}
