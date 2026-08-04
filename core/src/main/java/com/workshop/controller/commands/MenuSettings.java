package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.SettingsMenu;
import com.workshop.view.Console;

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
            Console.showMessage("%s\n",result);

        }
    }

    //menu settings change-difficulty -l <difficulty_level>
}
