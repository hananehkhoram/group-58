package com.workshop.controller.commands.MechanismsCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.SpecialLevelManager.TimedWarManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.exceptions.CommandNotFound;
import com.workshop.view.Console;

public class AdvancedTime implements Command {
    private MenuManager menuManager;

    public AdvancedTime(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String ticksStr = args[0];
        int ticks;
        try {
            ticks = Integer.parseInt(ticksStr);
        } catch (NumberFormatException e) {
            throw new CommandNotFound("Invalid tick amount!");
        }
        for (int i = 0; i < ticks; i++) {
            menuManager.getCtx().getTimeManager().advanceTime(1);
            menuManager.getGameEngine().update(0.1);
        }        Console.showMessage("Advanced time %d ticks.",ticks);
        if (menuManager.getCtx().getLevelManager() instanceof TimedWarManager){
            double timeRemaining = ((TimedWarManager) menuManager.getCtx().getLevelManager()).getTimeRemaining();
            Console.showMessage("Time remaining: %.1fs", timeRemaining);
        }

    }
    //advance time -t <count> ticks
}
