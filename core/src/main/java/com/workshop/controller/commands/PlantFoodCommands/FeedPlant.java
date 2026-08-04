package com.workshop.controller.commands.PlantFoodCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.plants.Plant;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

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
            Console.showMessage("No active battle.");
            return;
        }

        if (UserManager.getInstance().getCurrentUser().getPlantFoodCount() <= 0) {
            Console.showMessage("You have no plant food to use.");
            return;
        }
        Plant plant = ctx.getPlantGrid()[y][x];
        if (plant == null) {
            Console.showMessage("There is no plant at (" + x + ", " + y + ").");
            return;
        }

        UserManager.getInstance().usePlantFood(1);
        plant.activatePlantFood(ctx);

        Console.showMessage("Fed %s at (%d, %d)! Its plant food ability is now active.",
                plant.getName(),x,y);
    }

    //feed plant -l (<x>, <y>)
}
