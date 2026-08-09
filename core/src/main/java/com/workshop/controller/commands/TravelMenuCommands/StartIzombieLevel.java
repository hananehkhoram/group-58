package com.workshop.controller.commands.TravelMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.MiniGame.Izambi.Izambi;
import com.workshop.view.Console;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.model.level.Level;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;

import java.util.List;

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

            List<Level> levels = LevelFactory.buildIzombieLevels();

            if (levelNumber < 1 || levelNumber > levels.size()) {
                Console.showMessage("Invalid I-Zombie level number.");
                return;
            }

            User user = UserManager.getInstance().getCurrentUser();

            if (user == null
                || !user.isLevelUnlocked(
                levels.get(levelNumber - 1).getName()
            )) {
                Console.showMessage("This I-Zombie level is locked.");
                return;
            }

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
