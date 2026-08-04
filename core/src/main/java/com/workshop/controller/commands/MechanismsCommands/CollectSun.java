package com.workshop.controller.commands.MechanismsCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.exceptions.CommandNotFound;
import com.workshop.model.GameContext;
import com.workshop.view.Console;

public class CollectSun implements Command {
    private MenuManager menuManager;

    public CollectSun(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String xStr = args[0];
        String yStr = args[1];

        int x, y;
        try {
            x = Integer.parseInt(xStr);
            y = Integer.parseInt(yStr);
        } catch (NumberFormatException e) {
            throw new CommandNotFound("Invalid coordinates!");
        }

        GameContext ctx = menuManager.getCtx();

        int fromProducer = ctx.collectSunAt(x, y);
        if (fromProducer > 0) {
            Console.showMessage("Collected " + fromProducer + " sun from plant.");
            return;
        }

        boolean collected = ctx.getSunManager().collectSun(x, y, menuManager.getGameEngine());
        if (!collected) {
            Console.showMessage("There is no sun to collect here.");
        }
        else {
            Console.showMessage("Sun collected.You currently have %d suns.", menuManager.getCtx().getSunAmount());
        }
    }

    //collect sun -l (<x>, <y>)
}
