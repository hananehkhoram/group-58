package controller.commands.TravelMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.MiniGame.Izambi.Izambi;
import view.ConsoleView;

public class Place implements Command {
    private final MenuManager menuManager;

    public Place(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        if (args == null || args.length < 2) {
            ConsoleView.showMessage("Usage: place zombie (row, col)");
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
                ConsoleView.showMessage("I-Zombie mini-game is not currently active!");
            }

        } catch (NumberFormatException e) {
            ConsoleView.showMessage("Invalid row or column numbers.");
        }
    }
}