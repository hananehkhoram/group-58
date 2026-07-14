package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import exceptions.CommandNotFound;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;
import model.menus.allmenus.LoginMenu;
import view.ConsoleView;

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
            ConsoleView.showMessage("%s\n",result);
        }
    }
}

//menu cheat add <n> <coin/diamond>
