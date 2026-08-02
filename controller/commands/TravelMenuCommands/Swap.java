package controller.commands.TravelMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;

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
        boolean success = ctx.getBeghouldManager().trySwap(x1, y1, x2, y2);
    }
}
