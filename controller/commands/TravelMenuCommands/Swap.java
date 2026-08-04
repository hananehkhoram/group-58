package controller.commands.TravelMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import view.ConsoleView;

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
            ConsoleView.showMessage("No active Beghouled game.");
            return;
        }

        boolean success = ctx.getBeghouldManager().trySwap(x1, y1, x2, y2);
        ConsoleView.showMessage(success
                ? "Swapped (" + x1 + "," + y1 + ") and (" + x2 + "," + y2 + ") - match found!"
                : "That swap doesn't create a match. Try another one.");
    }
}