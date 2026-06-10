package model.menus.allmenus;

import model.ShopData.Shop;
import model.menus.BaseMenu;
import model.user.User;

public class ShopMenu extends BaseMenu {
    private Shop shop;
    private User currentUser;

    public void showShopList(){}
    public void showDailyOffer(){}
    public void buyItem(String id, int count, String plantType){}
}
