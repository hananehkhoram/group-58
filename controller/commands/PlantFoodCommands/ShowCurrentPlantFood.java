package controller.commands.PlantFoodCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.user.User;
import model.user.UserManager;
import view.ConsoleView;

public class ShowCurrentPlantFood implements Command {
    private MenuManager menuManager;

    public ShowCurrentPlantFood(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        User user = UserManager.getInstance().getCurrentUser();
        ConsoleView.showMessage("You have %d plant food",user.getPlantFoodCount());
    }
}
