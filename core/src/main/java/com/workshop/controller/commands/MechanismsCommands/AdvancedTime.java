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
        if (menuManager.getCtx() != null
            && menuManager.getCtx().isPaused()) {
            Console.showMessage("Game is paused.");
            return;
        }

        String ticksStr = args[0];
        int ticks;
        try {
            ticks = Integer.parseInt(ticksStr);
        } catch (NumberFormatException e) {
            throw new CommandNotFound("Invalid tick amount!");
        }
        int advancedTicks = 0;

        for (int i = 0; i < ticks; i++) {
            if (menuManager.getCtx().isGameEnded()) {
                break;
            }

            menuManager.getCtx().getTimeManager().advanceTime(1);
            advancedTicks++;

            menuManager.getGameEngine().update(0.1);

            if (menuManager.getCtx().isGameEnded()) {
                break;
            }
        }

        Console.showMessage("Advanced time %d ticks.", advancedTicks);

        if (menuManager.getCtx().getLevelManager() instanceof TimedWarManager){
            double timeRemaining = ((TimedWarManager) menuManager.getCtx().getLevelManager()).getTimeRemaining();
            Console.showMessage("Time remaining: %.1fs", timeRemaining);
        }

    }
    //advance time -t <count> ticks
}
