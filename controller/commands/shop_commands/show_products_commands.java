package controller.commands.shop_commands;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.menus.Menu;
import model.menus.allmenus.ShopMenu;
import view.ConsoleView;

public class show_products_commands implements Command {
    private MenuManager menuManager;

    public show_products_commands(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    @Override
    public void execute(String[] args) {
        String listOrDaily = args[0];

        Menu currentMenu = menuManager.getCurrentMenu();

        if (currentMenu instanceof ShopMenu){
            if (listOrDaily.equalsIgnoreCase("list")){
                String result = ((ShopMenu) currentMenu).showShopList();
                ConsoleView.showMessage("%s\n",result);
            }
            else {
                String result = ((ShopMenu) currentMenu).showDailyOffer();
                ConsoleView.showMessage("%s\n",result);
            }
        }
    }

    //shop list
    //shop daily
}
