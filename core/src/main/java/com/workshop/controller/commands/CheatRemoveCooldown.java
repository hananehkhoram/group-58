package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.view.Console;

public class CheatRemoveCooldown implements Command {
    private MenuManager menuManager;

    public CheatRemoveCooldown(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        menuManager.getCtx().clearAllCooldowns();
        Console.showMessage("Removed all cooldowns.\n");
    }
}
