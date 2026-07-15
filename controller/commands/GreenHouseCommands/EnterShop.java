package controller.commands.GreenHouseCommands;

import controller.MenuManager;
import controller.commandHandler.Command;

public class EnterShop implements Command {

    private MenuManager menuManager;

    public EnterShop(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {

        menuManager.changeMenu("ShopMenu");

    }

    //enter shop
}
