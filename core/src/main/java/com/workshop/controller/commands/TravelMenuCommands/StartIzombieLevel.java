package com.workshop.controller.commands.TravelMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.MiniGame.Izambi.Izambi;
import com.workshop.view.Console;

public class StartIzombieLevel implements Command {
    private final MenuManager menuManager;

    public StartIzombieLevel(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        if (args == null || args.length == 0) {
            Console.showMessage(
                "Usage: start izombie -l <1|2|3>"
            );
            return;
        }

        try {
            int levelNumber =
                Integer.parseInt(args[0]);

            Izambi game = new Izambi();

            game.startMiniGame(
                menuManager,
                levelNumber
            );

            if (game.getCtx() != null
                && game.getGameEngine() != null) {

                menuManager.setCtx(game.getCtx());

                menuManager.setGameEngine(
                    game.getGameEngine()
                );
            }
        } catch (NumberFormatException exception) {
            Console.showMessage(
                "Invalid I-Zombie level number."
            );
        }
    }
}
