package model.menus.allmenus;

import controller.NewsManager;
import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;

public class MainMenu extends BaseMenu {
    public MainMenu(GameContext ctx, MenuType menuType) {
    super(ctx, MenuType.MAIN);
    }
    public void play() {}
    public void openSettings() {}
    public void openNews() {}
    public void openProfile() {}
    public void logout() {}

    public boolean shouldShowRedDot(User currentUser) {
        int totalNewsCount = NewsManager.getAllNews().size();

        return totalNewsCount > currentUser.getLastReadNewsId();
    }

}
