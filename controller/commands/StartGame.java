package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;

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