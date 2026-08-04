package com.workshop.controller.commands.GameMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.view.Console;

public class EnterGreenHouse implements Command {
    private MenuManager menuManager;

    public EnterGreenHouse(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {

        menuManager.changeMenu("GreenHouseMenu");
        Console.showMessage("You are now in greenhouse");


    }

    //p_14
}
