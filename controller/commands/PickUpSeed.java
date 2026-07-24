package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.mechanisms.Tile;
import model.plants.Plant;
import controller.SpecialLevelManager.ConveyorBeltManager;
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
            Plant newPlant = ctx.getPlantFactory().create(seedName);

            if (ctx.getLevelManager() instanceof ConveyorBeltManager) {
                ConveyorBeltManager cbm = (ConveyorBeltManager) ctx.getLevelManager();
                cbm.getConveyorBelt().add(newPlant);

                tile.clearDroppedSeed();
                ConsoleView.simplePrint(seedName + " added to your conveyor belt!");
            } else {
                ConsoleView.simplePrint("System Error: No conveyor belt found to store the seed!");
            }

        } else {
            ConsoleView.simplePrint("There is no seed packet here!");
        }
    }
}