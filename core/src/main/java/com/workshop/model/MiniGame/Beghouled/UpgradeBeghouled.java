package com.workshop.model.MiniGame.Beghouled;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.view.Console;

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
            Console.showMessage("No active Beghouled game.");
            return;
        }

        String result = ctx.getBeghouldManager().upgradeAll(plantName);
        Console.showMessage(result);
    }

    //upgrade beghouled -p <plantName>
}
