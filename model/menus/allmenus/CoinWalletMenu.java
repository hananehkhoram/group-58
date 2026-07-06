package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class CoinWalletMenu extends BaseMenu {

    public CoinWalletMenu(GameContext ctx) {
        super(ctx, MenuType.COIN_WALLET);
        this.name = "Coin Wallet menu";
    }

}
