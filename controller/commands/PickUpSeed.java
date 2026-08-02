package controller.commands;

import controller.MenuManager;
import controller.SpecialLevelManager.ConveyorBeltManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.mechanisms.Tile;
import model.plants.Plant;
import view.ConsoleView;

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
                ConsoleView.simplePrint(seedName + " picked up and held in hand!\n");
            } catch (Exception e) {
                ConsoleView.showMessage("Error picking up seed!\n");
            }
        } else {
            ConsoleView.simplePrint("There is no seed packet here!\n");
        }
    }
}