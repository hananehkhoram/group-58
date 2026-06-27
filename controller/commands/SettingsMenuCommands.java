package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.allmenus.SettingsMenu;

public class SettingsMenuCommands implements Command {

    private MenuManager menuManager;

    public SettingsMenuCommands(MenuManager menuManager){
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        menuManager.changeMenu("SettingsMenu");

    }

    //p_13
}
