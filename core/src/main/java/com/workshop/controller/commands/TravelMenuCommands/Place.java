package com.workshop.controller.commands.TravelMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.MiniGame.Izambi.Izambi;
import com.workshop.view.Console;

public class Place implements Command {
    private final MenuManager menuManager;

    public Place(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        if (args == null || args.length < 3) {
            Console.showMessage(
                "Usage: place zombie -t <type> -l (<x>, <y>)"
            );
            return;
        }

        try {
            String zombieType = args[0].trim();

            int column = Integer.parseInt(
                args[args.length - 2].trim()
            );

            int row = Integer.parseInt(
                args[args.length - 1].trim()
            );

            Izambi currentIzambi =
                Izambi.getActiveInstance();

            if (currentIzambi == null) {
                Console.showMessage(
                    "I-Zombie mini-game is not currently active!"
                );
                return;
            }

            currentIzambi.placeZombie(
                zombieType,
                row,
                column
            );

        } catch (NumberFormatException exception) {
            Console.showMessage(
                "Invalid row or column numbers."
            );
        }
    }
}
