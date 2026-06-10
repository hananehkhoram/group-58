package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class NewsMenu extends BaseMenu {
    public NewsMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.NEWS);
    }
    public void showUnreadNews() {}

    public void showAllNews() {}

    public boolean hasUnreadNews() { return false; }
}
