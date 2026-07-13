package controller.commands;

import controller.commandHandler.Command;
import model.user.UserManager;

public class CheatClearUsers implements Command {

    @Override
    public void execute(String[] args) {
        UserManager.getInstance().clearAllUsers();
    }
}
