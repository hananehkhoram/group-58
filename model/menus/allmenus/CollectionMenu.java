package model.menus.allmenus;

import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.plants.plantsKinds.Plant;
import model.user.User;
import model.user.UserManager;

public class CollectionMenu extends BaseMenu {
    private UserManager um;
    protected User currentUser;
    public CollectionMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.COLLECTION);
        currentUser = um.getCurrentUser();
    }

    public String showAllPlants() {
        StringBuilder sb = new StringBuilder();
    }
    public void showAllZombies() {}
    public void showPlantDetails(String plantName) {}
    public void showZombieDetails(String zombieName) {}
    public void upgradePlant(String plantName) {}
    public void purchasePlant(String plantName) {}
}
