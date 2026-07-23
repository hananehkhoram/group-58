package controller.commands.PlantFoodCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.plants.Plant;
import model.user.UserManager;
import view.ConsoleView;

public class FeedPlant implements Command {
    private MenuManager menuManager;

    public FeedPlant(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);

        GameContext ctx = menuManager.getCtx();
        if (ctx == null) {
            ConsoleView.showMessage("No active battle.");
            return;
        }

        if (UserManager.getInstance().getCurrentUser().getPlantFoodCount() <= 0) {
            ConsoleView.showMessage("You have no plant food to use.");
            return;
        }
        Plant plant = ctx.getPlantGrid()[x][y];
        if (plant == null) {
            ConsoleView.showMessage("There is no plant at (" + x + ", " + y + ").");
            return;
        }

        ctx.usePlantFood(1);
        plant.activatePlantFood(ctx);

        ConsoleView.showMessage("Fed %s at (%d, %d)! Its plant food ability is now active.",
                plant.getName(),x,y);
    }

    //feed plant -l (<x>, <y>)
}
