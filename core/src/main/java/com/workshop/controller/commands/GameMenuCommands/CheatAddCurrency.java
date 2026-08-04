package com.workshop.controller.commands.GameMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.exceptions.CommandNotFound;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GameMenu;
import com.workshop.view.Console;

public class CheatAddCurrency implements Command {
    private MenuManager menuManager;

    public CheatAddCurrency(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();
        String amountStr = args[0];
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            throw new CommandNotFound("Invalid number!");
        }
        String currency = args[1];


        if (currentMenu instanceof GameMenu){
            String result = ((GameMenu) currentMenu).addCheat(currency,amount);
            Console.showMessage(result);
        }
        else {
            Console.showMessage("You have to be in game menu for this action!\n");
        }
    }
}

//menu cheat add <n> <coin/diamond>
