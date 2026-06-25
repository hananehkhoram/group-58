package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;
import model.user.UserManager;

public class SettingsMenu extends BaseMenu {
    private UserManager um;
    public SettingsMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.SETTINGS);
        this.um = UserManager.getInstance();
    }

    public String changeDifficulty(int newLevel){
        um.getCurrentUser().setDifficultyLevel(newLevel);
        return "Successfully changed difficulty to "+newLevel;
    }
}
