package com.workshop.controller.commands.PlantFoodCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

public class CheatAddPlantFood implements Command {
    private MenuManager menuManager;

    public CheatAddPlantFood(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        UserManager.getInstance().getCurrentUser().setPlantFoodCount(
                UserManager.getInstance().getCurrentUser().getPlantFoodCount() + 1);
        Console.showMessage("Cheat added 1 plant food.");
    }
}//cheat add-plant-food
