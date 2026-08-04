package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

public class CheatAddPlant implements Command {
    private MenuManager menuManager;

    public CheatAddPlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        GameContext ctx = menuManager.getCtx();
        String type = args[0];
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        PlantFactory factory = new PlantFactory(ctx.getDataManager());
        Plant p = factory.create(type);

        if (p == null) {
            Console.showMessage("No such plant: " + type);
            return;
        }
        if (x >= Level.COLS || x < 0 || y < 0 || y >= Level.ROWS) {
            Console.showMessage("Invalid coordinates: " + x + ", " + y);
            return;
        }

        p.setCol(x);
        p.setRow(y);
        ctx.getActivePlants().add(p);
        ctx.getAlivePlants().add(p);
        ctx.getPlantGrid()[y][x] = p;
        ctx.recordPlantPlaced(p, y, x);
        menuManager.getGameEngine().getTiles(x, y).setPlant(p);
        Console.showMessage("Plant " + p.getName() + " has been planted.");
//        if (planting.isValidPlacement(p, p.getName(), x, y, ctx, menuManager.getGameEngine(), ctx.getLevelManager(), false, false)) {
//            ctx.getPlantGrid()[y][x] = p;
//            Consolecom.workshop.view.showMessage("Plant " + p.getName() + " has been planted.");
//        } else {
//            Consolecom.workshop.view.showMessage("Cant plant here.");
//        }

    }

}
