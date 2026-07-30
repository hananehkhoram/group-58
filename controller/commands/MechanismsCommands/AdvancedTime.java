package controller.commands.MechanismsCommands;

import controller.MenuManager;
import controller.SpecialLevelManager.TimedWarManager;
import controller.commandHandler.Command;
import exceptions.CommandNotFound;
import view.ConsoleView;

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
        }        ConsoleView.showMessage("Advanced time %d ticks.",ticks);
        if (menuManager.getCtx().getLevelManager() instanceof TimedWarManager){
            double timeRemaining = ((TimedWarManager) menuManager.getCtx().getLevelManager()).getTimeRemaining();
            ConsoleView.showMessage("Time remaining: %.1fs", timeRemaining);
        }

    }

    //advance time -t <count> ticks
}
