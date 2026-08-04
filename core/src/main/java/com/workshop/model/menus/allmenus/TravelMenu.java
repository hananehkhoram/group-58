package com.workshop.model.menus.allmenus;

import com.workshop.model.GameContext;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;

public class TravelMenu extends BaseMenu {
    public TravelMenu(GameContext ctx) {
        super(ctx, MenuType.TRAVEL);
        this.name = "Travel menu";
    }
}
