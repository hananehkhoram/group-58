package controller.commands.TravelMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;

public class smashVase implements Command {

    private MenuManager menuManager;

    public smashVase(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);

        GameContext ctx = menuManager.getCtx();
        ctx.getGameEngine().smashVase(x, y);
    }
}
