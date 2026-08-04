package com.workshop.controller.commands.GameMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.GameMenu;
import com.workshop.view.Console;

public class EnterLeaderBoard implements Command {
    private MenuManager menuManager;
    private GameContext gameContext;

    public EnterLeaderBoard(MenuManager menuManager, GameContext gameContext) {
        this.menuManager = menuManager;
        this.gameContext = gameContext;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu){
            menuManager.changeMenu("LeaderBoardMenu");
            Console.showMessage("You are now in leader board menu");
        }
    }

    //menu leaderboard
}
