package com.workshop.model.menus.allmenus;

import com.workshop.model.GameContext;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.user.UserManager;

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
