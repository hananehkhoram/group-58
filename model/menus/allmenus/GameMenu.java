package model.menus.allmenus;

import controller.repository.DataManager;
import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.season.Season;
import model.user.UserManager;
import view.ConsoleView;

public class GameMenu extends BaseMenu {
    private Season currentWorld;
    public GameMenu(GameContext ctx) {
        super(ctx, MenuType.GAME);
        this.name = "Game menu";
    }

    public void switchWorld(String worldName) {
        Season world = DataManager.getInstance().seasons.get(worldName);
        if (world == null) {
            ConsoleView.showMessage("Invalid world name.\n");
            return;
        }
        this.currentWorld = world;
        ConsoleView.showMessage("Switched to %s\n" , world.getName());
    }
    public String addCheat(String type, int amount) {
        if (type.equalsIgnoreCase("coin")) {
            UserManager.getInstance().getCurrentUser().setCoins(UserManager.getInstance().getCurrentUser().getCoins() + amount);
            return "added "+amount+"to your coins.";
        }
        else if (type.equalsIgnoreCase("diamond") || type.equalsIgnoreCase("gem")){
            UserManager.getInstance().getCurrentUser().setGems(UserManager.getInstance().getCurrentUser().getGems() + amount);
            return "added "+amount+"to your gems.";
        }
        return "Invalid currency type!";
    }

    public Season getCurrentWorld() {
        return currentWorld;
    }
}
