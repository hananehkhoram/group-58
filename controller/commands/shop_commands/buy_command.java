package controller.commands.shop_commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.ProfileMenu;
import model.menus.allmenus.ShopMenu;
import view.ConsoleView;

public class buy_command implements Command {
    private MenuManager menuManager;

    public buy_command(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String id = args[0];
        int count = Integer.parseInt(args[1]);
        String plantType = null;
        if (args[2] != null) plantType = args[2];
        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ShopMenu){
            String result = ((ShopMenu) currentMenu).buyItem(Integer.parseInt(id),count,plantType);
            ConsoleView.showMessage("%s\n",result);

        }
    }

    //shop buy -i <item_id> -n <count> [-t <plant_type>]
}
