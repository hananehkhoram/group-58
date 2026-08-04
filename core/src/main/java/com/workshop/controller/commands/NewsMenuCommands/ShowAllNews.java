package com.workshop.controller.commands.NewsMenuCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.NewsMenu;
import com.workshop.view.Console;

public class ShowAllNews implements Command {
    private MenuManager menuManager;

    public ShowAllNews(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof NewsMenu){
            String result = ((NewsMenu) currentMenu).showAllNews();
            Console.showMessage("%s\n",result);

        }
    }

    //menu news show-all
}
