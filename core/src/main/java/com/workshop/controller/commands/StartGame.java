package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;

public class StartGame implements Command {//اضافی
    private MenuManager menuManager;

    public StartGame(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {

        menuManager.changeMenu("GameMenu");
        menuManager.getCtx().setBattleStarted(true);
    }

    //start game
}
