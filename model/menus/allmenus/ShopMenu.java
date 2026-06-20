package model.menus.allmenus;

import model.GameContext;
import model.ShopData.Currency;
import model.ShopData.ItemType;
import model.ShopData.Shop;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;
import model.user.UserManager;

public class ShopMenu extends BaseMenu {
    private Shop shop;
    private User currentUser;
    private UserManager um;

    public ShopMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.SHOP);
        this.um = UserManager.getInstance();
        this.currentUser = um.getCurrentUser();
    }

    public String showShopList(String itemType){
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Shop ===\n");
        for (ItemType item : ItemType.values()){
            sb.append(item.getDisplayName()).append("Price: ").append(item.getPrice());
            sb.append(item.getCurrency().equals(Currency.COIN) ? "coins.\n" : "gems.\n");
            sb.append("Description: ");
        }
        return null;
    }
    public void showDailyOffer(){}
    public void buyItem(String id, int count, String plantType){}
}
