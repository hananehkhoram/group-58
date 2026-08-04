package com.workshop.controller.commands.LoginMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.LoginMenu;
import com.workshop.view.Console;

public class Answer implements Command {
    private MenuManager menuManager;

    public Answer(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String answer = args[0];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof LoginMenu){
            String result = ((LoginMenu) currentMenu).answerSecurityQuestion(answer);
            Console.showMessage("%s\n",result);

        }
    }

    //answer -a <answer>
}
