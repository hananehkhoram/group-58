package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

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
        Console.showMessage("All Zombies are killed.");
    }
}

