package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.zombie.Zombie;
import view.ConsoleView;

public class CheatSpawnZombie implements Command {
    private MenuManager menuManager;

    public CheatSpawnZombie (MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        GameContext ctx = menuManager.getCtx();
        String type = args[0];
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        Zombie z = ctx.getDataManager().zombies.get(type);
        if (z == null) {
            ConsoleView.showMessage("No such zombie");
            return;
        }
        z.setX(x);
        z.setY(y);
        ctx.getAliveZombies().add(z);
        ConsoleView.showMessage("Zombie " + z.getName() + " has been spawned.");
    }

    //cheat spawn-zombie -t <zombie-type> -l <x, y>
}
