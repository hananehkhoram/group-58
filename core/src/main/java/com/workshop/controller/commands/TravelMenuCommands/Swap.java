package com.workshop.controller.commands.TravelMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.view.Console;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.model.MiniGame.Beghouled.BeghouldGame;
import com.workshop.model.level.Level;

import java.util.List;

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
            Console.showMessage("No active Beghouled game.");
            return;
        }

        boolean success = ctx.getBeghouldManager().trySwap(x1, y1, x2, y2);
        Console.showMessage(success
                ? "Swapped (" + x1 + "," + y1 + ") and (" + x2 + "," + y2 + ") - match found!"
                : "That swap doesn't create a match. Try another one.");

        if (success && ctx.isGameEnded()) {
            startNextBeghouledLevel(ctx);
        }
    }

    private void startNextBeghouledLevel(
        GameContext finishedContext
    ) {
        List<Level> levels =
            LevelFactory.buldBeghouledLevels();

        int currentIndex = -1;

        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).getName().equals(
                finishedContext.getLevel().getName()
            )) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex < 0) {
            return;
        }

        int nextIndex = currentIndex + 1;

        if (nextIndex >= levels.size()) {
            Console.showMessage(
                "All Beghouled levels completed!"
            );
            return;
        }

        BeghouldGame nextGame =
            new BeghouldGame();

        nextGame.start(
            menuManager,
            nextIndex + 1
        );

        if (nextGame.getCtx() != null
            && nextGame.getGameEngine() != null) {

            menuManager.setCtx(
                nextGame.getCtx()
            );

            menuManager.setGameEngine(
                nextGame.getGameEngine()
            );
        }
    }
}
