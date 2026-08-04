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
        if (args == null || args.length < 2) {
            Console.showMessage("Usage: place zombie (row, col)");
            return;
        }

        try {
            int col = Integer.parseInt(args[0]);
            int row = Integer.parseInt(args[1]);

            Izambi currentIzambi = Izambi.getActiveInstance();

            if (currentIzambi != null) {
                String zombieType = "Ra";
                currentIzambi.placeZombie(zombieType, row, col);
            } else {
                Console.showMessage("I-Zombie mini-game is not currently active!");
            }

        } catch (NumberFormatException e) {
            Console.showMessage("Invalid row or column numbers.");
        }
    }
}
