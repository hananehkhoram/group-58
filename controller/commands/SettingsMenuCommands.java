package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import exceptions.CommandNotFound;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import model.menus.allmenus.SettingsMenu;
import view.ConsoleView;

public class SettingsMenuCommands implements Command {

    private MenuManager menuManager;

    public SettingsMenuCommands(MenuManager menuManager){
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String difficultyStr = args[0];
        int difficulty;
        try {
            difficulty = Integer.parseInt(difficultyStr);
        } catch (NumberFormatException e) {
            throw new CommandNotFound("Invalid difficulty!\n");
        }
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof SettingsMenu){
            String result = ((SettingsMenu) currentMenu).changeDifficulty(difficulty);
            ConsoleView.showMessage("%s\n",result);

        }

    }

    //menu settings change-difficulty -l <difficulty_level>
}
