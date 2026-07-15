package controller.commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.MenuType;
import view.ConsoleView;

public class MenuExit implements Command {
    private MenuManager menuManager;

    public MenuExit(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        Menu current = menuManager.getCurrentMenu();
        MenuType type = current.getMenu();

        switch (type) {
            case REGISTER:
                ConsoleView.showMessage("Goodbye!");
                System.exit(0);
                break;
            case MAIN:
                ConsoleView.showMessage("You must use 'menu logout' to leave the main menu.");
                break;
            default:
                MenuType target = MenuManager.getExitTarget(type);
                if (target == null) {
                    ConsoleView.showMessage("You can't exit from here.");
                } else {
                    menuManager.forceChangeMenu(target.name().toLowerCase() + "menu");
                    ConsoleView.showMessage("You are now in %s menu.\n",target.name());
                }
        }
    }//menu exit
}