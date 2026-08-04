package com.workshop.controller.commands.PlantFoodCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

public class ShowCurrentPlantFood implements Command {
    private MenuManager menuManager;

    public ShowCurrentPlantFood(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        User user = UserManager.getInstance().getCurrentUser();
        Console.showMessage("You have %d plant food",user.getPlantFoodCount());
    }
}//show current plant food
