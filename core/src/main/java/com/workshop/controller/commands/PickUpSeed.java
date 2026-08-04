package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.SpecialLevelManager.ConveyorBeltManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.Tile;
import com.workshop.model.plants.Plant;
import com.workshop.view.Console;

public class PickUpSeed implements Command {
    private MenuManager menuManager;

    public PickUpSeed(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);

        GameContext ctx = menuManager.getCtx();
        if (ctx == null || ctx.getGameEngine() == null) return;

        Tile tile = ctx.getGameEngine().getTiles(x, y);

        if (tile != null && tile.hasDroppedSeed()) {
            String seedName = tile.getDroppedSeed();
            seedName = seedName.substring(0, 1).toUpperCase() + seedName.substring(1);

            try {
                ctx.setHeldSeed(seedName);
                if (ctx.getLevelManager() instanceof ConveyorBeltManager){
                    Plant plantToAdd = ctx.getPlantFactory().create(seedName);
                    ((ConveyorBeltManager) ctx.getLevelManager()).getConveyorBelt().add(plantToAdd);
                }
                tile.clearDroppedSeed();
                Console.simplePrint(seedName + " picked up and held in hand!\n");
            } catch (Exception e) {
                Console.showMessage("Error picking up seed!\n");
            }
        } else {
            Console.simplePrint("There is no seed packet here!\n");
        }
    }
}
