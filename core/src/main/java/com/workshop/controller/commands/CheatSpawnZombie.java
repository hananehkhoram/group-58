package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

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
        ZombieFactory factory = new ZombieFactory(ctx.getDataManager());
        Zombie z = factory.create(type);

        if (z == null) {
            Console.showMessage("No such zombie: " + type);
            return;
        }
        if (x >= Level.COLS || x < 0 || y < 0 || y >= Level.ROWS) {
            Console.showMessage("Invalid coordinates: " + x + ", " + y);
            return;
        }

        z.setX(x);
        z.setY(y);
        ctx.addZombie(z);
        ctx.getActiveZombies().add(z);
        Console.showMessage("Zombie " + z.getName() + " has been spawned.");
    }

    //cheat spawn-zombie -t <zombie-type> -l <x, y>
}
