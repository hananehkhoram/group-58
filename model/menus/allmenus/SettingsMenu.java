package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;

public class SettingsMenu extends BaseMenu {
    public SettingsMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.SETTINGS);}

    private User currentUser;
    public void changeDifficulty(int newLevel){}
}
