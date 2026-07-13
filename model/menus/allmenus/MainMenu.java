package model.menus.allmenus;

import controller.NewsManager;
import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;

public class MainMenu extends BaseMenu {
    public MainMenu(GameContext ctx) {
    super(ctx, MenuType.MAIN);
        this.name = "Main menu";
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

    @Override
    public MenuType getMenu() {
        return null;
    }
}
