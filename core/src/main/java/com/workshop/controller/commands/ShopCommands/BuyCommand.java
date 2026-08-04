package com.workshop.controller.commands.ShopCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.ShopMenu;
import com.workshop.view.Console;

public class BuyCommand implements Command {
    private MenuManager menuManager;

    public BuyCommand(MenuManager menuManager) {
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
            Console.showMessage("%s\n",result);

        }
    }

    //shop buy -i <item_id> -n <count> [-t <plant_type>]
}
