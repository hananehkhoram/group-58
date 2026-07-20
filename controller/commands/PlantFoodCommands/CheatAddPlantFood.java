package controller.commands.PlantFoodCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.user.UserManager;
import view.ConsoleView;

public class CheatAddPlantFood implements Command {
    private MenuManager menuManager;

    public CheatAddPlantFood(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        UserManager.getInstance().getCurrentUser().setPlantFoodCount(
                UserManager.getInstance().getCurrentUser().getPlantFoodCount() + 1);
        ConsoleView.showMessage("Cheat added 1 plant food.");
    }
}//cheat add-plant-food
