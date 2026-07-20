package controller.commands.MechanismsCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import exceptions.CommandNotFound;
import view.ConsoleView;

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
        ConsoleView.showMessage("Added %d to your suns.\n",amount);
    }
}//cheat add -n <count> suns
