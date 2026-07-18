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
        String type = args[0];
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);

        GameContext ctx = menuManager.getCtx();
        GameEngine engine = menuManager.getGameEngine();

        if (ctx == null || engine == null) {
            ConsoleView.showMessage("No active battle.");
            return;
        }

        Plant template = ctx.getPlantGrid()[x][y];
        for (Plant p : ctx.getAlivePlants()) {
            if (p.getName().equalsIgnoreCase(type)) { template = p; break; }
        }
        if (template == null) {
            ConsoleView.showMessage("This plant is not currently on the ground!");
            return;
        }
        ctx.getAlivePlants().remove(template);
        ctx.getPlantGrid()[x][y] = null;
        ConsoleView.showMessage("Plucked %s at (%d, %d).",template.getName(),x,y);

    }

    //pluck plant -l (<x>, <y>)
}
