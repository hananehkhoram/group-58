package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.mechanisms.GameEngine;
import model.plants.Plant;
import view.ConsoleView;

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
            ConsoleView.showMessage("No active battle.");
            return;
        }

        if (y < 0 || y >= model.level.Level.ROWS || x < 0 || x >= model.level.Level.COLS) {
            ConsoleView.showMessage("Invalid location.");
            return;
        }

        Plant template = ctx.getPlantGrid()[y][x];
        if (template == null) {
            ConsoleView.showMessage("This plant is not currently on the ground!");
            return;
        }
        ctx.getAlivePlants().remove(template);
        ctx.getPlantGrid()[y][x] = null;
        ConsoleView.showMessage("Plucked %s at (%d, %d).", template.getName(), x, y);
    }

    //pluck plant -l (<x>, <y>)
}