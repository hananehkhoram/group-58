package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

public class Plucking implements Command {
    private MenuManager menuManager;

    public Plucking(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]); // ستون
        int y = Integer.parseInt(args[1]); // سطر

        GameContext ctx = menuManager.getCtx();
        GameEngine engine = menuManager.getGameEngine();

        if (ctx == null || engine == null) {
            Console.showMessage("No active battle.");
            return;
        }

        if (y < 0 || y >= com.workshop.model.level.Level.ROWS || x < 0 || x >= com.workshop.model.level.Level.COLS) {
            Console.showMessage("Invalid location.");
            return;
        }

        Plant template = ctx.getPlantGrid()[y][x];
        if (template == null) {
            Console.showMessage("This plant is not currently on the ground!");
            return;
        }
        ctx.getAlivePlants().remove(template);
        ctx.getPlantGrid()[y][x] = null;
        Console.showMessage("Plucked %s at (%d, %d).", template.getName(), x, y);
    }

    //pluck plant -l (<x>, <y>)
}
