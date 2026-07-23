package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.zombie.Zombie;
import view.ConsoleView;

public class ReleaseTheNuke implements Command {
    private MenuManager menuManager;

    public ReleaseTheNuke(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        GameContext ctx = menuManager.getCtx();
        for (Zombie z : ctx.getAliveZombies()) {
            z.setHp(0);
            ctx.getAliveZombies().remove(z);
        }
        ConsoleView.showMessage("All Zombies are killed.");
    }
}

