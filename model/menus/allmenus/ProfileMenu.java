package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class ProfileMenu extends BaseMenu {
    public ProfileMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.PROFILE);
    }

    public void changeUsername(String newUsername) {
    }

    public void changeNickname(String newNickname) {
    }

    public void changeEmail(String newEmail) {
    }

    public void changePassword(String oldPassword, String newPassword) {
    }

    public void showInfo() {
    }
}
