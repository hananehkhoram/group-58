package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;

public class CollectionMenu extends BaseMenu {
    public CollectionMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.COLLECTION);
    }

    public void showAllPlants() {}
    public void showAllZombies() {}
    public void showPlantDetails(String plantName) {}
    public void showZombieDetails(String zombieName) {}
    public void upgradePlant(String plantName) {}
    public void purchasePlant(String plantName) {}
}
