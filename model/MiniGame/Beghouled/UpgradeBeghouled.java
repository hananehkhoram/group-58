package controller.commands.TravelMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import view.ConsoleView;

public class UpgradeBeghouled implements Command {

    private MenuManager menuManager;

    public UpgradeBeghouled(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String plantName = args[0];

        GameContext ctx = menuManager.getCtx();
        if (ctx == null || ctx.getBeghouldManager() == null) {
            ConsoleView.showMessage("No active Beghouled game.");
            return;
        }

        String result = ctx.getBeghouldManager().upgradeAll(plantName);
        ConsoleView.showMessage(result);
    }

    //upgrade beghouled -p <plantName>
}