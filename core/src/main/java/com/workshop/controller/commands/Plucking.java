package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

public class Plucking implements Command {

    private final MenuManager menuManager;

    public Plucking(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {

        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);

        GameContext ctx = menuManager.getCtx();
        GameEngine engine = menuManager.getGameEngine();

        if (ctx == null || engine == null) {
            Console.showMessage("No active battle.");
            return;
        }

        if (y < 0
            || y >= ctx.getLevel().getRows()
            || x < 0
            || x >= ctx.getLevel().getColumns()) {

            Console.showMessage("Invalid location.");
            return;
        }

        Plant plant = ctx.getPlantGrid()[y][x];

        if (plant == null || plant.isDead()) {
            Console.showMessage(
                "This plant is not currently on the ground!"
            );
            return;
        }

        engine.removePlant(y, x);

        Console.showMessage(
            "Plucked %s at (%d, %d).",
            plant.getName(),
            x,
            y
        );
    }
}
