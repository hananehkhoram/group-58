package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import controller.repository.factory.PlantFactory;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.level.Level;
import model.plants.Plant;
import model.zombie.Zombie;
import view.ConsoleView;

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
            ConsoleView.showMessage("No such plant: " + type);
            return;
        }
        if (x >= Level.COLS || x < 0 || y < 0 || y >= Level.ROWS) {
            ConsoleView.showMessage("Invalid coordinates: " + x + ", " + y);
            return;
        }

        p.setCol(x);
        p.setRow(y);
        ctx.getActivePlants().add(p);
        ctx.getAlivePlants().add(p);
        ctx.getPlantGrid()[y][x] = p;
        ctx.recordPlantPlaced(p, y, x);
        menuManager.getGameEngine().getTiles(x, y).setPlant(p);
        ConsoleView.showMessage("Plant " + p.getName() + " has been planted.");
//        if (planting.isValidPlacement(p, p.getName(), x, y, ctx, menuManager.getGameEngine(), ctx.getLevelManager(), false, false)) {
//            ctx.getPlantGrid()[y][x] = p;
//            ConsoleView.showMessage("Plant " + p.getName() + " has been planted.");
//        } else {
//            ConsoleView.showMessage("Cant plant here.");
//        }

    }

}
