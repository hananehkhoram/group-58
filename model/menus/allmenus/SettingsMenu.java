package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.user.User;
import model.user.UserManager;

public class SettingsMenu extends BaseMenu {
    private UserManager um;
    public SettingsMenu(GameContext ctx) {
        super(ctx, MenuType.SETTINGS);
        this.um = UserManager.getInstance();
        this.name = "Settings menu";
    }

    public String changeDifficulty(int newLevel){
        if (newLevel < 1 || newLevel > 5) {
            return "Difficulty level must be between 1 and 5.";
        }
        um.getCurrentUser().setDifficultyLevel(newLevel);
        return "Successfully changed difficulty to "+newLevel;
    }
}
