package controller.commands.GameMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.GameMenu;
import model.user.UserManager;
import view.ConsoleView;

public class EnterGemWallet implements Command {
    private MenuManager menuManager;

    public EnterGemWallet(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof GameMenu){
            ConsoleView.showMessage("Your gems: %d\n", UserManager.getInstance().getCurrentUser().getGems());
        }

    }

    //menu gem-wallet
}
