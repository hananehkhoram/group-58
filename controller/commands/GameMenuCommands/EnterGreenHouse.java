package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.menus.MenuType;
import model.menus.allmenus.GreenHouseMenu;
import view.ConsoleView;

public class EnterGreenHouse implements Command {
    private MenuManager menuManager;

    public EnterGreenHouse(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {

        menuManager.changeMenu("GreenHouseMenu");
        ConsoleView.showMessage("You are now in greenhouse");


    }

    //p_14
}
