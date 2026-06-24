package controller.commands.NewsMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.LoginMenu;
import model.menus.allmenus.NewsMenu;
import view.ConsoleView;

public class showAllNews implements Command {
    private MenuManager menuManager;

    public showAllNews(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof NewsMenu){
            String result = ((NewsMenu) currentMenu).showAllNews();
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //menu news show-all
}
