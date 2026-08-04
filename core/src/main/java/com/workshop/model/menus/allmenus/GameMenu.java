package com.workshop.model.menus.allmenus;

import com.workshop.controller.repository.DataManager;
import com.workshop.model.GameContext;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.season.Season;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

public class GameMenu extends BaseMenu {
    private Season currentWorld;
    public GameMenu(GameContext ctx) {
        super(ctx, MenuType.GAME);
        this.name = "Game menu";
    }

    public void switchWorld(String worldName) {
        Season world = DataManager.getInstance().seasons.get(worldName);
        if (world == null) {
            Console.showMessage("Invalid world name.\n");
            return;
        }
        this.currentWorld = world;
        Console.showMessage("Switched to %s\n" , world.getName());
    }
    public String addCheat(String type, int amount) {
        if (type.equalsIgnoreCase("coin")) {
            UserManager.getInstance().getCurrentUser().setCoins(UserManager.getInstance().getCurrentUser().
                    getCoins() + amount);
            return "added "+amount+" to your coins.";
        }
        else if (type.equalsIgnoreCase("diamond") || type.equalsIgnoreCase("gem")){
            UserManager.getInstance().getCurrentUser().setGems(UserManager.getInstance().getCurrentUser().
                    getGems() + amount);
            return "added "+amount+"to your gems.";
        }
        return "Invalid currency type!";
    }

    public Season getCurrentWorld() {
        return currentWorld;
    }
}
