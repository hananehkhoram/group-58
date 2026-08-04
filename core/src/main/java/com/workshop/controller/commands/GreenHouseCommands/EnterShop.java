package com.workshop.controller.commands.GreenHouseCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;

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
