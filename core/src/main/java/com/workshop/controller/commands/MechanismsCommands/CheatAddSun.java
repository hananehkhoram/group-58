package com.workshop.controller.commands.MechanismsCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.exceptions.CommandNotFound;
import com.workshop.view.Console;

public class CheatAddSun implements Command {
    private MenuManager menuManager;

    public CheatAddSun(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String amountStr = args[0];
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            throw new CommandNotFound("Invalid amount!");
        }
        menuManager.getCtx().addSun(amount);
        Console.showMessage("Added %d to your suns.\n",amount);
    }
}//cheat add -n <count> suns
