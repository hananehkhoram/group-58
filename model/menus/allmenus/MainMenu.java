package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class MainMenu extends BaseMenu {
    public MainMenu(GameContext ctx, MenuType menuType) {
    super(ctx, MenuType.MAIN);
    }
    public void play() {}
    public void openSettings() {}
    public void openNews() {}
    public void openProfile() {}
    public void logout() {}

}
