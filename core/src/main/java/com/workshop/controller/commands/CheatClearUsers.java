package com.workshop.controller.commands;

import com.workshop.controller.commandHandler.Command;
import com.workshop.model.user.UserManager;

public class CheatClearUsers implements Command {

    @Override
    public void execute(String[] args) {
        UserManager.getInstance().clearAllUsers();
    }
}//cheat reset users
