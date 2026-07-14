package controller.commands.MechanismsCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import view.ConsoleView;

public class ShowSunAmount implements Command {
    private MenuManager menuManager;

    public ShowSunAmount(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        ConsoleView.showMessage("Sun amount: %d",menuManager.getCtx().getSunAmount());
    }

    //show sun amount
}
