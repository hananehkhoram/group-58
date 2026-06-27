package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import model.menus.allmenus.SettingsMenu;
import view.ConsoleView;

public class MenuSettings implements Command {
    private MenuManager menuManager;

    public MenuSettings(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        int difficulty = Integer.parseInt(args[0]);
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof SettingsMenu){
            String result = ((SettingsMenu) currentMenu).changeDifficulty(difficulty);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu settings change-difficulty -l <difficulty_level>
}
