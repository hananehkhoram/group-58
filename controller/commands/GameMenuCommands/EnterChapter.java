package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;

public class EnterChapter implements Command {

    private MenuManager menuManager;

    public EnterChapter(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        String chapterName = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu) {

        }

    }

    //menu enter chapter -c <chaptername>
}
