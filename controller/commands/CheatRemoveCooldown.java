package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import view.ConsoleView;

public class CheatRemoveCooldown implements Command {
    private MenuManager menuManager;

    public CheatRemoveCooldown(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        menuManager.getCtx().clearAllCooldowns();
        ConsoleView.showMessage("Removed all cooldowns.\n");
    }
}
