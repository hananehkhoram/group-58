package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class GameMenu extends BaseMenu {
    public GameMenu(GameContext ctx, MenuType menuType) {
        super(ctx, menuType);
    }

    public void enterChapter(String chapterName) {}
    public void openCollectionMenu() {}
    public void openGreenhouseMenu() {}
    public void openTravelLogMenu() {}
    public void openLeaderboardMenu() {}
    public void openCoinWallet() {}
    public void openGemWallet() {}
    public void switchWorld(String worldName) {}
    public void addCheat(String type, int amount) {}

}
