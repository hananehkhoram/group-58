package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.zombie.Zombie;
import view.ConsoleView;

public class ShowZombiesInfo implements Command {
    private MenuManager menuManager;
    public ShowZombiesInfo(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        GameContext ctx = menuManager.getCtx();
        for (Zombie z : ctx.getAliveZombies()){
            ConsoleView.showMessage(z.zombieInfo());
        }
    }

    //zombies info
}
