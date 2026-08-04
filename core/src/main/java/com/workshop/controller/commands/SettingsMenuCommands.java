package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.exceptions.CommandNotFound;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.SettingsMenu;
import com.workshop.view.Console;

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
            Console.showMessage("%s\n",result);

        }

    }

    //menu settings change-difficulty -l <difficulty_level>
}
