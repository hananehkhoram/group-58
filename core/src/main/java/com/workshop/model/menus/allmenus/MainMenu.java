package com.workshop.model.menus.allmenus;

import com.workshop.controller.NewsManager;
import com.workshop.model.GameContext;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.user.User;

public class MainMenu extends BaseMenu {
    public MainMenu(GameContext ctx) {
    super(ctx, MenuType.MAIN);
        this.name = "Main menu";
    }

    public boolean shouldShowRedDot(User currentUser) {
        int totalNewsCount = NewsManager.getAllNews().size();

        return totalNewsCount > currentUser.getLastReadNewsId();
    }

    @Override
    public String showMenu() {
        User currentUser = com.workshop.model.user.UserManager.getInstance().getCurrentUser();
        if (currentUser != null && shouldShowRedDot(currentUser)) {
            return name + " (🔴New News!)";
        }
        return name;
    }

}
