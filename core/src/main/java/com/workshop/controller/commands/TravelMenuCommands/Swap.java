package com.workshop.controller.commands.TravelMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.view.Console;

public class Swap implements Command {

    private MenuManager menuManager;

    public Swap(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        int x1 = Integer.parseInt(args[0]);
        int y1 = Integer.parseInt(args[1]);
        int x2 = Integer.parseInt(args[2]);
        int y2 = Integer.parseInt(args[3]);

        GameContext ctx = menuManager.getCtx();
        if (ctx == null || ctx.getBeghouldManager() == null) {
            Console.showMessage("No active Beghouled game.");
            return;
        }

        boolean success = ctx.getBeghouldManager().trySwap(x1, y1, x2, y2);
        Console.showMessage(success
                ? "Swapped (" + x1 + "," + y1 + ") and (" + x2 + "," + y2 + ") - match found!"
                : "That swap doesn't create a match. Try another one.");
    }
}
