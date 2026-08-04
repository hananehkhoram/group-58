package com.workshop.controller.commands.ShopCommands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.menus.Menu;
import com.workshop.model.menus.allmenus.ShopMenu;
import com.workshop.view.Console;

public class ShowProductsCommands implements Command {
    private MenuManager menuManager;

    public ShowProductsCommands(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String listOrDaily = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ShopMenu){
            if (listOrDaily.equalsIgnoreCase("list")){
                String result = ((ShopMenu) currentMenu).showShopList();
                Console.showMessage("%s\n",result);
            }
            else {
                String result = ((ShopMenu) currentMenu).showDailyOffer();
                Console.showMessage("%s\n",result);
            }
        }
    }

    //shop list
    //shop daily
}
