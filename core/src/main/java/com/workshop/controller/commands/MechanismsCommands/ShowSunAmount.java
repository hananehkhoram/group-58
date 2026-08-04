package com.workshop.controller.commands.MechanismsCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.view.Console;

public class ShowSunAmount implements Command {
    private MenuManager menuManager;

    public ShowSunAmount(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Console.showMessage("Sun amount: %d",menuManager.getCtx().getSunAmount());
    }

    //show sun amount
}
