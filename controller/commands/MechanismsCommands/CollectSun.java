package controller.commands.MechanismsCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import exceptions.CommandNotFound;
import model.GameContext;
import view.ConsoleView;

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
            ConsoleView.showMessage("Collected " + fromProducer + " sun from plant.");
            return;
        }

        boolean collected = ctx.getSunManager().collectSun(x, y, menuManager.getGameEngine());
        if (!collected) {
            ConsoleView.showMessage("There is no sun to collect here.");
        }
    }

    //collect sun -l (<x>, <y>)
}
